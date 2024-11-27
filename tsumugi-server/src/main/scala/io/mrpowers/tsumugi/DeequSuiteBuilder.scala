package io.mrpowers.tsumugi

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import com.amazon.deequ.{
  AnomalyCheckConfig,
  VerificationRunBuilder,
  VerificationRunBuilderWithRepository,
  VerificationSuite
}
import com.amazon.deequ.analyzers._
import com.amazon.deequ.anomalydetection._
import com.amazon.deequ.checks.{Check, CheckLevel}
import com.amazon.deequ.constraints.Constraint
import com.amazon.deequ.repository.{MetricsRepository, ResultKey}
import com.amazon.deequ.repository.fs.FileSystemMetricsRepository
import com.amazon.deequ.repository.sparktable.SparkTableMetricsRepository

object DeequSuiteBuilder {
  private[mrpowers] def parseCheckLevel(checkLevel: proto.CheckLevel): CheckLevel.Value = {
    checkLevel match {
      case proto.CheckLevel.Error   => CheckLevel.Error
      case proto.CheckLevel.Warning => CheckLevel.Warning
      case _                        => throw new RuntimeException("Unknown check level type!")
    }
  }

  private[mrpowers] def parseSign[T: Numeric](reference: T, sign: proto.Check.ComparisonSign): T => Boolean = {
    sign match {
      case proto.Check.ComparisonSign.GET => (x: T) => implicitly[Numeric[T]].gteq(x, reference)
      case proto.Check.ComparisonSign.GT  => (x: T) => implicitly[Numeric[T]].gt(x, reference)
      case proto.Check.ComparisonSign.EQ  => (x: T) => implicitly[Numeric[T]].equiv(x, reference)
      case proto.Check.ComparisonSign.LT  => (x: T) => implicitly[Numeric[T]].lt(x, reference)
      case proto.Check.ComparisonSign.LET => (x: T) => implicitly[Numeric[T]].lteq(x, reference)
      case _                              => throw new RuntimeException("Unknown comparison type!")
    }
  }

  private[mrpowers] def parseAnalyzerOptions(maybeOptions: Option[proto.AnalyzerOptions]): Option[AnalyzerOptions] = {
    maybeOptions.map(opt =>
      AnalyzerOptions(
        opt.getNullBehaviour match {
          case proto.AnalyzerOptions.NullBehaviour.Fail        => NullBehavior.Fail
          case proto.AnalyzerOptions.NullBehaviour.Ignore      => NullBehavior.Ignore
          case proto.AnalyzerOptions.NullBehaviour.EmptyString => NullBehavior.EmptyString
          case _ => throw new RuntimeException("Unknown NullBehaviour Type!")
        },
        opt.getFilteredRowOutcome match {
          case proto.AnalyzerOptions.FilteredRowOutcome.NULL => FilteredRowOutcome.NULL
          case proto.AnalyzerOptions.FilteredRowOutcome.TRUE => FilteredRowOutcome.TRUE
          case _ => throw new RuntimeException("Unknown FilteredRowOutcome Type!")
        }
      )
    )
  }

  private[mrpowers] def parseAnalyzer(analyzer: proto.Analyzer) = {
    analyzer.getAnalyzerCase match {
      case proto.Analyzer.AnalyzerCase.APPROX_COUNT_DISTINCT =>
        val protoAnalyzer = analyzer.getApproxCountDistinct
        ApproxCountDistinct(
          protoAnalyzer.getColumn,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.APPROX_QUANTILE =>
        val protoAnalyzer = analyzer.getApproxQuantile
        ApproxQuantile(
          protoAnalyzer.getColumn,
          protoAnalyzer.getQuantile,
          if (protoAnalyzer.hasRelativeError) protoAnalyzer.getRelativeError else 0.01,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.APPROX_QUANTILES =>
        val protoAnalyzer = analyzer.getApproxQuantiles
        ApproxQuantiles(
          protoAnalyzer.getColumn,
          protoAnalyzer.getQuantilesList.asScala
            .map(_.toDouble)
            .toSeq, // Why an implicit conversion fails here?
          if (protoAnalyzer.hasRelativeError) protoAnalyzer.getRelativeError else 0.01
        )
      case proto.Analyzer.AnalyzerCase.COLUMN_COUNT =>
        ColumnCount()
      case proto.Analyzer.AnalyzerCase.COMPLETENESS =>
        val protoAnalyzer = analyzer.getCompleteness
        Completeness(
          protoAnalyzer.getColumn,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case proto.Analyzer.AnalyzerCase.COMPLIANCE =>
        val protoAnalyzer = analyzer.getCompliance
        Compliance(
          protoAnalyzer.getInstance,
          protoAnalyzer.getPredicate,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          protoAnalyzer.getColumnsList.asScala.toList,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case proto.Analyzer.AnalyzerCase.CORRELATION =>
        val protoAnalyzer = analyzer.getCorrelation
        Correlation(
          protoAnalyzer.getFirstColumn,
          protoAnalyzer.getSecondColumn,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.COUNT_DISTINCT =>
        CountDistinct(analyzer.getCountDistinct.getColumnsList.asScala.toSeq)
      case proto.Analyzer.AnalyzerCase.CUSTOM_SQL =>
        CustomSql(analyzer.getCustomSql.getExpressions)
      case proto.Analyzer.AnalyzerCase.DATA_TYPE =>
        val protoAnalyzer = analyzer.getDataType
        DataType(protoAnalyzer.getColumn, if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty)
      case proto.Analyzer.AnalyzerCase.DISTINCTNESS =>
        val protoAnalyzer = analyzer.getDistinctness
        Distinctness(
          protoAnalyzer.getColumnsList.asScala.toSeq,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.ENTROPY =>
        val protoAnalyzer = analyzer.getEntropy
        Entropy(protoAnalyzer.getColumn, if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty)
      case proto.Analyzer.AnalyzerCase.EXACT_QUANTILE =>
        val protoAnalyzer = analyzer.getExactQuantile
        ExactQuantile(
          protoAnalyzer.getColumn,
          protoAnalyzer.getQuantile,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.HISTOGRAM =>
        val protoAnalyzer = analyzer.getHistogram
        Histogram(
          protoAnalyzer.getColumn,
          Option.empty, // TODO: binningUDF is not supported; see analyzers.proto
          if (protoAnalyzer.hasMaxDetailBins) protoAnalyzer.getMaxDetailBins
          else Histogram.MaximumAllowedDetailBins,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          if (protoAnalyzer.hasComputeFrequenciesAsRatio)
            protoAnalyzer.getComputeFrequenciesAsRatio
          else true,
          if (protoAnalyzer.hasAggregateFunction) {
            val protoAggregate = protoAnalyzer.getAggregateFunction
            protoAggregate.getAggregateFunctionCase match {
              case proto.Histogram.AggregateFunction.AggregateFunctionCase.COUNT_AGGREGATE =>
                Histogram.Count
              case proto.Histogram.AggregateFunction.AggregateFunctionCase.SUM_AGGREGATE =>
                Histogram.Sum(protoAggregate.getSumAggregate.getAggColumn)
              case _ => throw new RuntimeException("Unknown AggregateFunction type!")
            }
          } else Histogram.Count
        )
      case proto.Analyzer.AnalyzerCase.KLL_SKETCH =>
        val protoAnalyzer = analyzer.getKllSketch
        KLLSketch(
          protoAnalyzer.getColumn,
          if (protoAnalyzer.hasKllParameters)
            Some(
              KLLParameters(
                protoAnalyzer.getKllParameters.getSketchSize,
                protoAnalyzer.getKllParameters.getShrinkingFactor,
                protoAnalyzer.getKllParameters.getNumberOfBuckets
              )
            )
          else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.MAX_LENGTH =>
        val protoAnalyzer = analyzer.getMaxLength
        MaxLength(
          protoAnalyzer.getColumn,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case proto.Analyzer.AnalyzerCase.MAXIMUM =>
        val protoAnalyzer = analyzer.getMaximum
        Maximum(
          protoAnalyzer.getColumn,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case proto.Analyzer.AnalyzerCase.MEAN =>
        val protoAnalyzer = analyzer.getMean
        Mean(protoAnalyzer.getColumn, if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty)
      case proto.Analyzer.AnalyzerCase.MIN_LENGTH =>
        val protoAnalyzer = analyzer.getMinLength
        MinLength(
          protoAnalyzer.getColumn,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case proto.Analyzer.AnalyzerCase.MINIMUM =>
        val protoAnalyzer = analyzer.getMinimum
        Minimum(
          protoAnalyzer.getColumn,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case proto.Analyzer.AnalyzerCase.MUTUAL_INFORMATION =>
        val protoAnalyzer = analyzer.getMutualInformation
        MutualInformation(
          protoAnalyzer.getColumnsList.asScala.toSeq,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.PATTERN_MATCH =>
        val protoAnalyzer = analyzer.getPatternMatch
        PatternMatch(
          protoAnalyzer.getColumn,
          protoAnalyzer.getPattern.r,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case proto.Analyzer.AnalyzerCase.RATIO_OF_SUMS =>
        val protoAnalyzer = analyzer.getRatioOfSums
        RatioOfSums(
          protoAnalyzer.getNumerator,
          protoAnalyzer.getDenominator,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.SIZE =>
        Size(if (analyzer.getSize.hasWhere) Some(analyzer.getSize.getWhere) else Option.empty)
      case proto.Analyzer.AnalyzerCase.STANDARD_DEVIATION =>
        val protoAnalyzer = analyzer.getStandardDeviation
        StandardDeviation(
          protoAnalyzer.getColumn,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty
        )
      case proto.Analyzer.AnalyzerCase.SUM =>
        val protoAnalyzer = analyzer.getSum
        Sum(protoAnalyzer.getColumn, if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty)
      case proto.Analyzer.AnalyzerCase.UNIQUE_VALUE_RATIO =>
        val protoAnalyzer = analyzer.getUniqueValueRatio
        UniqueValueRatio(
          protoAnalyzer.getColumnsList.asScala.toSeq,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case proto.Analyzer.AnalyzerCase.UNIQUENESS =>
        val protoAnalyzer = analyzer.getUniqueness
        Uniqueness(
          protoAnalyzer.getColumnsList.asScala.toSeq,
          if (protoAnalyzer.hasWhere) Some(protoAnalyzer.getWhere) else Option.empty,
          parseAnalyzerOptions(Option(protoAnalyzer.getOptions))
        )
      case _ =>
        throw new RuntimeException(s"Unsupported Analyzer Type ${analyzer.getAnalyzerCase.name}")
    }
  }

  private[mrpowers] def parseCheck(check: proto.Check): Check = {
    val constraints = check.getConstraintsList.asScala.map { constraint: proto.Check.Constraint =>
      {
        val analyzer = parseAnalyzer(constraint.getAnalyzer)
        val hint = if (constraint.hasHint) Some(constraint.getHint) else Option.empty
        analyzer match {
          case al: ApproxCountDistinct =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: ApproxQuantile =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: ColumnCount =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getLongExpectation, constraint.getSign),
              hint = hint
            )
          case al: Completeness =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: Correlation =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: Distinctness =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: Entropy =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: ExactQuantile =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          // TODO: Think about how Histogram may be added here; at the moment there is no fromAnalyzer for it
          // TODO: It is impossible to add KLLSketch at the moment because of assertion signature
          case al: MaxLength =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: Maximum =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: Mean =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: MinLength =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: Minimum =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: MutualInformation =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: PatternMatch =>
            Constraint.fromAnalyzer(
              al,
              al.pattern,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              name = if (constraint.hasName) Some(constraint.getName) else Option.empty,
              hint = hint
            )
          case al: Size =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getLongExpectation, constraint.getSign),
              hint = hint
            )
          case al: StandardDeviation =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: Sum =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: UniqueValueRatio =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case al: Uniqueness =>
            Constraint.fromAnalyzer(
              al,
              assertion = parseSign(constraint.getDoubleExpectation, constraint.getSign),
              hint = hint
            )
          case _ =>
            throw new RuntimeException(s"Analyzer ${analyzer.getClass.getSimpleName} cannot be used in Check!")
        }
      }
    }

    constraints.foldLeft(Check(parseCheckLevel(check.getCheckLevel), description = check.getDescription)) {
      (check: Check, constraint: Constraint) => check.addConstraint(constraint)
    }
  }

  private def parseAnomalyDetectionStrategy(strategy: proto.AnomalyDetectionStrategy) = {
    strategy.getStrategyCase match {
      case proto.AnomalyDetectionStrategy.StrategyCase.ABSOLUTE_CHANGE_STRATEGY =>
        val protoStrategy = strategy.getAbsoluteChangeStrategy
        AbsoluteChangeStrategy(
          if (protoStrategy.hasMaxRateDecrease) Some(protoStrategy.getMaxRateDecrease)
          else Option.empty,
          if (protoStrategy.hasMaxRateIncrease) Some(protoStrategy.getMaxRateIncrease)
          else Option.empty,
          if (protoStrategy.hasOrder) protoStrategy.getOrder else 1
        )
      case proto.AnomalyDetectionStrategy.StrategyCase.BATCH_NORMAL_STRATEGY =>
        val protoStrategy = strategy.getBatchNormalStrategy
        BatchNormalStrategy(
          if (protoStrategy.hasLowerDeviationFactor) Some(protoStrategy.getLowerDeviationFactor)
          else Option.empty,
          if (protoStrategy.hasUpperDeviationFactor) Some(protoStrategy.getUpperDeviationFactor)
          else Option.empty,
          if (protoStrategy.hasIncludeInterval) protoStrategy.getIncludeInterval else false
        )
      case proto.AnomalyDetectionStrategy.StrategyCase.ONLINE_NORMAL_STRATEGY =>
        val protoStrategy = strategy.getOnlineNormalStrategy
        OnlineNormalStrategy(
          if (protoStrategy.hasLowerDeviationFactor) Some(protoStrategy.getLowerDeviationFactor)
          else Option.empty,
          if (protoStrategy.hasUpperDeviationFactor) Some(protoStrategy.getUpperDeviationFactor)
          else Option.empty,
          if (protoStrategy.hasIgnoreStartPercentage) protoStrategy.getIgnoreStartPercentage
          else 0.1,
          if (protoStrategy.hasIgnoreAnomalies) protoStrategy.getIgnoreAnomalies else true
        )
      case proto.AnomalyDetectionStrategy.StrategyCase.RELATIVE_RATE_OF_CHANGE_STRATEGY =>
        val protoStrategy = strategy.getRelativeRateOfChangeStrategy
        RelativeRateOfChangeStrategy(
          if (protoStrategy.hasMaxRateDecrease) Some(protoStrategy.getMaxRateDecrease)
          else Option.empty,
          if (protoStrategy.hasMaxRateIncrease) Some(protoStrategy.getMaxRateIncrease)
          else Option.empty,
          if (protoStrategy.hasOrder) protoStrategy.getOrder else 1
        )
      case proto.AnomalyDetectionStrategy.StrategyCase.SIMPLE_THRESHOLDS_STRATEGY =>
        val protoStrategy = strategy.getSimpleThresholdsStrategy
        SimpleThresholdStrategy(
          if (protoStrategy.hasLowerBound) protoStrategy.getLowerBound else Double.MinValue,
          protoStrategy.getUpperBound
        )
      case _ =>
        throw new RuntimeException(s"Unsupported Strategy ${strategy.getStrategyCase.name}")
    }
  }
  private[mrpowers] def parseMetricRepository(
      spark: SparkSession,
      verificationSuite: proto.VerificationSuite
  ): Try[MetricsRepository] = {
    if (!verificationSuite.hasRepository) {
      Failure(
        new RuntimeException("For anomaly detection one of FS repository or Table repository should be provided!")
      )
    } else {
      verificationSuite.getRepository.getRepositoryCase match {
        case proto.Repository.RepositoryCase.FILE_SYSTEM =>
          Success(FileSystemMetricsRepository(spark, verificationSuite.getRepository.getFileSystem.getPath))
        case proto.Repository.RepositoryCase.SPARK_TABLE =>
          Success(new SparkTableMetricsRepository(spark, verificationSuite.getRepository.getSparkTable.getTableName))
        case _ =>
          Failure(
            new RuntimeException("For anomaly detection one of FS repository or Table repository should be provided!")
          )
      }
    }
  }

  def protoToVerificationSuite(
      data: DataFrame,
      verificationSuite: proto.VerificationSuite
  ): Try[VerificationRunBuilder] = {
    val spark = data.sparkSession
    val builder = new VerificationSuite()
      .onData(data)
      .addChecks(verificationSuite.getChecksList.asScala.map(parseCheck))
      .addRequiredAnalyzers(verificationSuite.getRequiredAnalyzersList.asScala.map(parseAnalyzer))

    // Anomaly detection branch

    if (verificationSuite.hasResultKey) {
      for {
        repository <- parseMetricRepository(spark, verificationSuite)
        builderWithRepo <- verificationSuiteToRunBuilderWithRepo(builder, repository, verificationSuite)
      } yield builderWithRepo
    } else {
      Success(builder)
    }
  }

  private def verificationSuiteToRunBuilderWithRepo(
      builder: VerificationRunBuilder,
      repository: MetricsRepository,
      verificationSuite: proto.VerificationSuite
  ): Try[VerificationRunBuilderWithRepository] =
    Try {
      val adBuilder = builder
        .useRepository(repository)
        .saveOrAppendResult(
          ResultKey(
            verificationSuite.getResultKey.getDatasetDate,
            verificationSuite.getResultKey.getTagsMap.asScala.toMap
          )
        )
      Range(0, verificationSuite.getAnomalyDetectionsCount).foldLeft(adBuilder)((builder, adIdx) => {
        val ad = verificationSuite.getAnomalyDetections(adIdx)
        val analyzer = parseAnalyzer(ad.getAnalyzer)
        val strategy = parseAnomalyDetectionStrategy(ad.getAnomalyDetectionStrategy)
        val options =
          if (ad.hasConfig)
            Some(
              AnomalyCheckConfig(
                parseCheckLevel(ad.getConfig.getLevel),
                ad.getConfig.getDescription,
                ad.getConfig.getWithTagValuesMap.asScala.toMap,
                if (ad.getConfig.hasAfterDate) Some(ad.getConfig.getAfterDate) else None,
                if (ad.getConfig.hasBeforeDate) Some(ad.getConfig.getBeforeDate) else None
              )
            )
          else None
        // TODO: How to filter only Analyzer[S, Metric[Double]] instead of this ugly code? Good first issue
        analyzer match {
          case al: ApproxCountDistinct => builder.addAnomalyCheck(strategy, al, options)
          case al: ApproxQuantile      => builder.addAnomalyCheck(strategy, al, options)
          case al: ColumnCount         => builder.addAnomalyCheck(strategy, al, options)
          case al: Completeness        => builder.addAnomalyCheck(strategy, al, options)
          case al: Compliance          => builder.addAnomalyCheck(strategy, al, options)
          case al: Correlation         => builder.addAnomalyCheck(strategy, al, options)
          case al: CountDistinct       => builder.addAnomalyCheck(strategy, al, options)
          case al: Distinctness        => builder.addAnomalyCheck(strategy, al, options)
          case al: Entropy             => builder.addAnomalyCheck(strategy, al, options)
          case al: ExactQuantile       => builder.addAnomalyCheck(strategy, al, options)
          case al: MaxLength           => builder.addAnomalyCheck(strategy, al, options)
          case al: Maximum             => builder.addAnomalyCheck(strategy, al, options)
          case al: Mean                => builder.addAnomalyCheck(strategy, al, options)
          case al: MinLength           => builder.addAnomalyCheck(strategy, al, options)
          case al: Minimum             => builder.addAnomalyCheck(strategy, al, options)
          case al: MutualInformation   => builder.addAnomalyCheck(strategy, al, options)
          case al: PatternMatch        => builder.addAnomalyCheck(strategy, al, options)
          case al: RatioOfSums         => builder.addAnomalyCheck(strategy, al, options)
          case al: Size                => builder.addAnomalyCheck(strategy, al, options)
          case al: StandardDeviation   => builder.addAnomalyCheck(strategy, al, options)
          case al: Sum                 => builder.addAnomalyCheck(strategy, al, options)
          case al: UniqueValueRatio    => builder.addAnomalyCheck(strategy, al, options)
          case al: Uniqueness          => builder.addAnomalyCheck(strategy, al, options)
          case _ =>
            throw new RuntimeException(
              s"AD is supported only for Analyzers with Double metric! Got ${analyzer.getClass.getSimpleName}"
            )
        }
      })
    }

  def entryPoint(data: DataFrame, protoSuite: proto.VerificationSuite): DataFrame = {
    val spark = data.sparkSession
    protoToVerificationSuite(data, protoSuite) match {
      case Success(runBuilder) =>
        DeequUtils.runAndCollectResults(runBuilder, spark, protoSuite.getComputeRowLevelResults, data)
      case Failure(exception) =>
        spark.createDataFrame(
          java.util.Collections.singletonList(Row(exception.toString)),
          schema = StructType(Seq(StructField("error", StringType)))
        )
    }
  }

  def entryPoint(data: DataFrame, protoSuite: Array[Byte]): DataFrame = {
    val suite = proto.VerificationSuite.parseFrom(protoSuite)
    entryPoint(data, suite)
  }
}
