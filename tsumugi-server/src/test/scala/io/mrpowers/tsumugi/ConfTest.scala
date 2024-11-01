package io.mrpowers.tsumugi

import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.scalatest.funsuite.AnyFunSuiteLike

trait ConfTest extends AnyFunSuiteLike with SparkSessionTestWrapper {
  def createData(sparkSession: SparkSession): DataFrame = {
    sparkSession.createDataFrame(
      sparkSession.sparkContext.parallelize(
        List(
          Row(1L, "Thingy A", "awesome thing.", "high", 0L),
          Row(2L, "Thingy B", "available at https://thingb.com", null, 0L),
          Row(3L, null, null, "low", 5L),
          Row(4L, "Thingy D", "checkout https://thingb.ca", "low", 10L),
          Row(5L, "Thingy E", null, "high", 12L)
        )
      ),
      StructType(
        List(
          StructField("id", LongType),
          StructField("productName", StringType),
          StructField("description", StringType),
          StructField("priority", StringType),
          StructField("numViews", LongType)
        )
      )
    )
  }
}
