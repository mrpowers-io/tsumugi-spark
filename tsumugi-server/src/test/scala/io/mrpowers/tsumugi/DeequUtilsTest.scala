package io.mrpowers.tsumugi

class DeequUtilsTest extends ConfTest {

  test("testRunAndCollectResultsOneRow") {
    val data = createData(spark)

    val protoSuiteBuilder = proto.VerificationSuite.newBuilder()
    protoSuiteBuilder.addRequiredAnalyzers(proto.Analyzer.newBuilder().setSize(proto.Size.newBuilder().build()))
    protoSuiteBuilder.addChecks(
      proto.Check
        .newBuilder()
        .setCheckLevel(proto.CheckLevel.Error)
        .setDescription("integrity checks")
        .addConstraints(
          proto.Check.Constraint
            .newBuilder()
            .setAnalyzer(proto.Analyzer.newBuilder().setSize(proto.Size.newBuilder().build()))
            .setSign(proto.Check.ComparisonSign.EQ)
            .setLongExpectation(5L)
        )
        .addConstraints(
          proto.Check.Constraint
            .newBuilder()
            .setAnalyzer(proto.Analyzer.newBuilder().setCompleteness(proto.Completeness.newBuilder().setColumn("id")))
            .setSign(proto.Check.ComparisonSign.EQ)
            .setDoubleExpectation(1.0)
        )
    )

    val metricRepo = proto.Repository
      .newBuilder()
      .setFileSystem(
        proto.FileSystemRepository
          .newBuilder()
          .setPath("test-file.json")
      )
    protoSuiteBuilder.setRepository(metricRepo)
    protoSuiteBuilder.addAnomalyDetections(
      proto.AnomalyDetection
        .newBuilder()
        .setAnalyzer(
          proto.Analyzer.newBuilder().setSize(proto.Size.newBuilder().build())
        )
        .setAnomalyDetectionStrategy(
          proto.AnomalyDetectionStrategy
            .newBuilder()
            .setRelativeRateOfChangeStrategy(
              proto.RelativeRateOfChangeStrategy
                .newBuilder()
                .setMaxRateIncrease(1.2)
                .setMaxRateDecrease(0.8)
                .setOrder(1)
            )
        )
        .setConfig(
          proto.AnomalyDetection.AnomalyCheckConfig
            .newBuilder()
            .setLevel(proto.CheckLevel.Warning)
            .setDescription("My best description")
            .setBeforeDate(1000)
            .setAfterDate(0)
        )
    )
    val deequSuite = DeequSuiteBuilder.protoToVerificationSuite(data, protoSuiteBuilder.build())
    val deequResults = DeequUtils.runAndCollectResults(deequSuite.get, spark, returnRows = false, dataFrame = data)
    assert(deequResults.count() == 1)
    assert(deequResults.columns.length == 4)
  }

}
