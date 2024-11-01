package io.mrpowers.tsumugi

import com.amazon.deequ.checks.{Check, CheckResult}
import com.amazon.deequ.metrics.{DoubleMetric, HistogramMetric}
import com.amazon.deequ.{VerificationResult, VerificationRunBuilder}
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession, functions => F}

private[mrpowers] case class MetricsAndChecks(
    level: String,
    checkDescription: String,
    constraintMessage: String,
    metricName: String,
    metricInstance: String,
    metricEntity: String,
    metricValue: Double,
    status: String,
    constraint: String
)

object DeequUtils {
  private val ARRAY_COL: String = "_c_array_col"
  private val STRUCT_COL: String = "_c_struct_col"
  private def checkResultToCaseClass(pair: (Check, CheckResult)): Seq[MetricsAndChecks] = {
    val checkLevel = pair._1.level
    val checkDescription = pair._1.description
    val constraintResult = pair._2.constraintResults

    constraintResult.map(constraint => {
      MetricsAndChecks(
        level = checkLevel.toString,
        checkDescription = checkDescription,
        constraintMessage = constraint.message.getOrElse(""),
        metricName = constraint.metric.map(_.name).getOrElse(""),
        metricInstance = constraint.metric.map(_.instance).getOrElse(""),
        metricEntity = constraint.metric.map(_.entity.toString).getOrElse(""),
        metricValue = constraint.metric
          .map {
            case DoubleMetric(_, _, _, value, _) => value.getOrElse(-999.0)
            case HistogramMetric(_, value)       => value.map(_.numberOfBins.asInstanceOf[Double]).getOrElse(-999.0)
            case _                               => -999.0 // TODO: fix it in the future
          }
          .getOrElse(-999.0),
        status = constraint.status.toString,
        constraint = constraint.constraint.toString
      )
    })
  }

  private def dataFrameToCol(df: DataFrame): DataFrame = {
    df.withColumn(STRUCT_COL, F.struct(df.columns.map(F.col): _*))
      .agg(F.collect_list(STRUCT_COL).alias(ARRAY_COL))
  }

  private def withColumnFrom(
      df: DataFrame,
      from: DataFrame,
      column: String,
      alias: Option[String] = None
  ): DataFrame = {
    df.crossJoin(from.select(column)).select(df.columns.map(F.col) :+ F.col(column).alias(alias.getOrElse(column)): _*)
  }

  /**
   * Collect results of all the Check and combine with metrics
   * @param deequSuite
   *   results of verification
   * @return
   *   DataFrame with a schema < level: string, checkDescription: string, constraintMessage: string, metricName: string,
   *   metricInstance: string, metricEntity: string, metricValue: string, status: string, constraint: string >
   */
  private[mrpowers] def checkResultAsDataFramePatched(
      deequSuite: VerificationResult,
      sparkSession: SparkSession
  ): DataFrame = {
    sparkSession.createDataFrame(deequSuite.checkResults.flatMap(checkResultToCaseClass).toSeq).toDF()
  }

  /**
   * Collect all the results to a single DataFrame object
   * @param deequSuite
   *   results of verification
   * @param returnRows
   *   should row-level results be returned?
   * @param sparkSession
   *   spark session or None
   * @param dataFrame
   *   data for computing row-level results
   * @return
   */
  private[mrpowers] def allResultsAsDataFrame(
      deequSuite: VerificationResult,
      returnRows: Boolean = false,
      sparkSession: SparkSession,
      dataFrame: DataFrame
  ): DataFrame = {
    val metrics = VerificationResult.successMetricsAsDataFrame(sparkSession = sparkSession, verificationResult = deequSuite)
    val checks = VerificationResult.checkResultsAsDataFrame(sparkSession = sparkSession, verificationResult = deequSuite)
    val checkResults = checkResultAsDataFramePatched(deequSuite = deequSuite, sparkSession = sparkSession)

    val oneRowDf =
      sparkSession.createDataFrame(java.util.List.of[Row](Row(1)), StructType(Seq(StructField("status", IntegerType))))

    val baseDf = Map
      .apply(
        "metrics" -> dataFrameToCol(metrics),
        "checks" -> dataFrameToCol(checks),
        "checkResults" -> dataFrameToCol(checkResults)
      )
      .foldLeft(oneRowDf) { case (df: DataFrame, (alias: String, from: DataFrame)) =>
        withColumnFrom(df, from, ARRAY_COL, Option(alias))
      }
    if (returnRows) {
     val rowResults = VerificationResult.rowLevelResultsAsDataFrame(
        sparkSession = sparkSession,
        verificationResult = deequSuite,
        data = dataFrame
      )

      val singleLineRowLevelResults = dataFrameToCol(rowResults)
      withColumnFrom(baseDf, singleLineRowLevelResults, ARRAY_COL, Option("rowLevelResults"))
    } else {
      baseDf
    }
  }

  /**
   * Run the suite and return all the results as a DataFrame object
   * @param suite
   *   Deequ suite (builder
   * @param sparkSession
   *   optional SparkSession
   * @param returnRows
   *   should row-level results be returned?
   * @param dataFrame
   *   data to compute row-level results
   * @return
   */
  def runAndCollectResults(
      suite: VerificationRunBuilder,
      sparkSession: SparkSession,
      returnRows: Boolean = false,
      dataFrame: DataFrame
  ): DataFrame = {
    val results = suite.run()
    allResultsAsDataFrame(results, returnRows, sparkSession, dataFrame)
  }
}
