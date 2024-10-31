package io.mrpowers.tsumugi

import org.apache.spark.sql.SparkSession

// inspired by
// https://github.com/mrpowers-io/spark-fast-tests/blob/main/src/test/scala/com/github/mrpowers/spark/fast/tests/SparkSessionTestWrapper.scala
trait SparkSessionTestWrapper {
  lazy val spark: SparkSession = {
    val session = SparkSession
      .builder()
      .master("local")
      .appName("spark session")
      .config("spark.sql.shuffle.partitions", "1")
      .getOrCreate()
    session.sparkContext.setLogLevel("ERROR")
    session
  }
}
