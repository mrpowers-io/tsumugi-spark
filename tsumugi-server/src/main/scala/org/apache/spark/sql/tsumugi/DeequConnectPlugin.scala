package org.apache.spark.sql.tsumugi

// Dataset.ofRows is private[sql] because Dataset itself is private[sql]
// Is there any way to create a DataFrame from LogicalPlan except using spark package?

import com.google.protobuf.Any
import io.mrpowers.tsumugi.proto.VerificationSuite
import io.mrpowers.tsumugi.{DeequSuiteBuilder, DeequUtils}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.connect.planner.SparkConnectPlanner
import org.apache.spark.sql.connect.plugin.RelationPlugin

class DeequConnectPlugin extends RelationPlugin {
  override def transform(relation: Any, planner: SparkConnectPlanner): Option[LogicalPlan] = {
    if (relation.is(classOf[VerificationSuite])) {
      val protoSuite = relation.unpack(classOf[VerificationSuite])
      val spark = planner.sessionHolder.session
      val protoPlan = org.apache.spark.connect.proto.Plan.parseFrom(protoSuite.getData.toByteArray)
      val data = Dataset.ofRows(spark, planner.transformRelation(protoPlan.getRoot))
      val verificationSuiteBuilder = DeequSuiteBuilder
        .protoToVerificationSuite(
          data,
          protoSuite
        )

      // TODO: pass returnRows here
      val resultDf =
        DeequUtils.runAndCollectResults(
          verificationSuiteBuilder.get,
          spark,
          returnRows = protoSuite.getComputeRowLevelResults,
          dataFrame = data
        )
      Some(resultDf.logicalPlan)
    } else {
      None
    }
  }
}
