import os
from dataclasses import asdict, dataclass

import pandas as pd
from py4j.java_gateway import JavaObject
from pyspark.sql import DataFrame, SQLContext
from pyspark.sql import functions as F
from pyspark.sql.connect.client import SparkConnectClient
from pyspark.sql.connect.dataframe import DataFrame as ConnectDataFrame
from pyspark.sql.connect.plan import LogicalPlan
from pyspark.sql.connect.proto import Relation
from typing_extensions import Self

from tsumugi.analyzers import (
    AbstractAnalyzer,
)

from tsumugi.repository import MetricRepository

from tsumugi.utils import (
    CHECK_RESULTS_SUB_DF,
    CHECKS_SUB_DF,
    METRICS_SUB_DF,
    ROW_LEVEL_RESULTS_SUB_DF,
    CheckResult,
    MetricAndCheckResult,
    MetricResult,
)

from .proto import suite_pb2 as suite
from .proto import repository_pb2 as repository


@dataclass
class VerificationResult:
    """Results of verification."""

    def __init__(self, df: DataFrame | ConnectDataFrame) -> None:
        """This constructor is internal and is not recommended to use."""
        self._has_row_results = ROW_LEVEL_RESULTS_SUB_DF in df.columns
        self._checks = self._get_checks(df)
        self._metrics = self._get_metrics(df)
        self._check_results = self._get_check_results(df)
        if self._has_row_results:
            self._row_level_results = self._get_row_level_results(df)

    @property
    def checks(self) -> tuple[CheckResult]:
        """Results of checks.

        Returns results of all the checks as a collection of dataclasses.
        Each check can contain multiple Constraints.

        Each check has the following attributes:
        - check (str): the name of the check
        - check level (str): the level of the check (Warning, Error)
        - check status (str): the overall check status (depends of level)
        - constraint (str): the description of constraint
        - contraint status (str): the status (Success, Failure)
        - contraint message (str): resulting message
        """
        return self._checks

    @property
    def metrics(self) -> tuple[MetricResult]:
        """Computed metrics.

        Returns all the metrics as a collection of dataclasses.

        Each metric contains:
        - enitity (str): type of the metric (Dataset, Column)
        - instance (str): "*" in case of Dataset-level metric,
                          name of the column otherwise
        - name (str): name of the metric
        - value (float): the value of the metric
        """
        return self._metrics

    @property
    def check_results(self) -> tuple[MetricAndCheckResult]:
        """Results of checks with values of the corresponded metric and constraint.

        Combines results of contraints with the corresponding values
        and descriptons of metrics. Collection of dataclasses.

        Each element of the collection contains:
        - level (str): the same as in Check
        - check description (str): the same as in Check
        - conatraint message (str): the same as in Check
        - metric name (str): the name of the related metric
        - metric instance (str): column name or "*"
        - metric entity (str): Dataset or Column
        - metric value (str): the value of the related mtric
        - status (str): Success / Failure
        - constraint (str): the description of the constraint
        """
        return self._check_results

    @property
    def row_level_results(self) -> DataFrame | None:
        """Row-level results as it would be returned by Deequ.

        The original DataFrame and a boolean status column per each Check.
        """
        if self._has_row_results:
            return self._row_level_results
        else:
            return None

    def checks_as_pandas(self) -> pd.DataFrame:
        """Return checks as a Pandas DataFrame.

        This method construct the new DataFrame each time!
        If you need it in a loop, it is recommended to cahce an output.
        """
        return pd.DataFrame.from_records([asdict(val) for val in self.checks])

    def metrics_as_pandas(self) -> pd.DataFrame:
        """Return metrics as a Pandas DataFrame.

        This method construct the new DataFrame each time!
        If you need it in a loop, it is recommended to cahce an output.
        """
        return pd.DataFrame.from_records([asdict(val) for val in self.metrics])

    def check_results_as_pandas(self) -> pd.DataFrame:
        """Return check results as a Pandas DataFrame.

        This method construct the new DataFrame each time!
        If you need it in a loop, it is recommended to cahce an output.
        """
        return pd.DataFrame.from_records([asdict(val) for val in self.check_results])

    def _get_checks(self, df: DataFrame) -> tuple[CheckResult]:
        sub_df = df.select(F.explode(F.col(CHECKS_SUB_DF)).alias("sub_col"))
        collected = sub_df.collect()
        checks = []
        for row in collected:
            sub_row = row.sub_col
            checks.append(CheckResult._from_row(sub_row))

        return tuple(c for c in checks)

    def _get_metrics(self, df: DataFrame) -> tuple[MetricResult]:
        sub_df = df.select(F.explode(F.col(METRICS_SUB_DF)).alias("sub_col"))
        collected = sub_df.collect()
        metrics = []
        for row in collected:
            sub_row = row.sub_col
            metrics.append(MetricResult._from_row(sub_row))

        return tuple(m for m in metrics)

    def _get_check_results(self, df: DataFrame) -> tuple[MetricAndCheckResult]:
        sub_df = df.select(F.explode(F.col(CHECK_RESULTS_SUB_DF)).alias("sub_col"))
        collected = sub_df.collect()
        metrics_and_checks = []
        for row in collected:
            sub_row = row.sub_col
            metrics_and_checks.append(MetricAndCheckResult._from_row(sub_row))

        return tuple(mc for mc in metrics_and_checks)

    def _get_row_level_results(self, df: DataFrame) -> DataFrame:
        sub_df = df.select(F.explode(F.col(ROW_LEVEL_RESULTS_SUB_DF)).alias("sub_col"))
        return sub_df.select("sub_col.*")


class VerificationRunBuilder:
    """Helper class that simplify building of the Verification Run object."""

    def __init__(self, df: DataFrame | ConnectDataFrame) -> None:
        self._data = df
        self._checks: list[suite.Check] = list()
        self._required_analyzers: list[AbstractAnalyzer] = list()
        self._repository: MetricRepository | None = None
        self._dataset_date: int | None = None
        self._dataset_tags: dict[str, str] = dict()
        self._anomaly_detectons: list[suite.AnomalyDetection] = list()
        self._compute_row_results: bool = False

    def with_row_level_results(self) -> Self:
        """Mark that row-level results should be returned."""
        self._compute_row_results = True
        return self

    def add_required_analyzer(self, analyzer: AbstractAnalyzer) -> Self:
        """Add a required analyzer metric of that will be computed anyway."""
        self._required_analyzers.append(analyzer)
        return self

    def add_required_analyzers(self, analyzers: list[AbstractAnalyzer]) -> Self:
        """Set required analyzers. Override existing!"""
        self._required_analyzers = analyzers
        return self

    def add_check(self, check: suite.Check) -> Self:
        """Add a Check object.

        It is recommended to use CheckBuilder!
        """
        self._checks.append(check)
        return self

    def add_checks(self, checks: list[suite.Check]) -> Self:
        """Set checks. Override exisitng!"""
        self._checks = checks
        return self

    def with_fs_repository_and_key(
        self,
        filepath: str,
        dataset_date: int,
        dataset_tags: dict[str, str] | None = None,
    ) -> Self:
        """Add a FileSystem repository and date and tags for the ResultKey."""
        self._table_name = None
        self._path = filepath
        self._dataset_date = dataset_date
        self._dataset_tags = dataset_tags if dataset_tags is not None else dict()
        return self

    def with_table_repository_and_key(
        self,
        table_name: str,
        dataset_date: int,
        dateset_tags: dict[str, str] | None = None,
    ) -> Self:
        """Add a Table repository and date and tags for the ResultKey."""
        self._path = None
        self._table_name = table_name
        self._dataset_date = dataset_date
        self._dataset_tags = dateset_tags if dateset_tags is not None else dict()
        return self

    def add_anomaly_detection(self, ad: suite.AnomalyDetection) -> Self:
        """Add an anomaly detection check.

        It is recommended to use AnomalyDetectionBuilder!
        """
        self._anomaly_detectons.append(ad)
        return self

    def add_anomaly_detections(self, ads: list[suite.AnomalyDetection]) -> Self:
        """Set anomaly detection checks. Override existing!"""
        self._anomaly_detectons = ads
        return self

    def _validate(self) -> None:
        if len(self._anomaly_detectons) > 0:
            if not (self._path or self._table_name):
                raise ValueError("Anomaly detection requires repository and key")

    def _build(self) -> suite.VerificationSuite:
        self._validate()

        pb_suite = suite.VerificationSuite(
            checks=self._checks,
            required_analyzers=[al._to_proto() for al in self._required_analyzers],
            compute_row_level_results=self._compute_row_results,
        )

        if self._repository:
            pb_suite.repository = self._repository._to_proto()
            pb_suite.result_key = repository.ResultKey(
                dataset_date=self._dataset_date, tags=self._dataset_tags
            )

            for ad in self._anomaly_detectons:
                pb_suite.anomaly_detections.append(ad)

        return pb_suite

    def run(self) -> VerificationResult:
        """Run the suite.

        The type of runtime is determined by the session attached to the provided DataFrame.
        For a Spark Connect session, it will add a serialized plan to the Suite and send the message to the Connect Server.
        For a Spark Classic session, it will directly call the JVM with the suite and a Java DataFrame.
        """
        spark = self._data.sparkSession
        pb_suite = self._build()
        is_classic = (os.environ.get("SPARK_CONNECT_MODE_ENABLED") is None) or hasattr(
            self._data, "_jdf"
        )

        if is_classic:
            jvm = spark._jvm
            jdf = self._data._jdf
            result_jdf: JavaObject = (
                jvm.io.mrpowers.tsumugi.DeequSuiteBuilder.entryPoint(
                    jdf, pb_suite.SerializeToString()
                )
            )
            return VerificationResult(
                DataFrame(result_jdf, SQLContext(spark.sparkContext))
            )
        else:
            data_plan = self._data._plan
            assert data_plan is not None
            assert isinstance(data_plan, LogicalPlan)
            pb_suite.data = data_plan.to_proto(spark.client).SerializeToString()

            class DeequSuite(LogicalPlan):
                def __init__(self, pb_suite: suite.VerificationSuite) -> None:
                    super().__init__(None)
                    self._pb_suite = pb_suite

                def plan(self, session: SparkConnectClient) -> Relation:
                    plan = self._create_proto_relation()
                    plan.extension.Pack(self._pb_suite)
                    return plan

            return VerificationResult(
                ConnectDataFrame.withPlan(DeequSuite(pb_suite=pb_suite), spark)
            )


class VerificationSuite:
    """Python-deequ compatibility class."""

    @staticmethod
    def on_data(data: DataFrame | ConnectDataFrame) -> VerificationRunBuilder:
        """Return a VerificationRunBuilder for the given DataFrame object."""
        return VerificationRunBuilder(data)
