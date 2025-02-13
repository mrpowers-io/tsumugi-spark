ThisBuild / scalaVersion := "2.12.20"
ThisBuild / organization := "io.mrpowers"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val protobufVersion = "3.23.4"
lazy val sparkVersion = "3.5.4"
lazy val sparkVersionShort = sparkVersion.dropRight(2)
lazy val deequVersion = "2.0.9"
lazy val scalaTestVersion = "3.2.19"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature", "-Xfatal-warnings"),
  Test / fork := true,
  Test / parallelExecution := false,
  assembly / test := {},
  assembly / assemblyShadeRules := Seq(
    ShadeRule.rename("com.google.protobuf.**" -> "org.sparkproject.connect.protobuf.@1").inAll),
  Compile / PB.targets := Seq(PB.gens.java -> (Compile / sourceManaged).value),
  Compile / PB.includePaths ++= Seq(file("src/main/protobuf")),
  PB.protocVersion := protobufVersion
)

lazy val commonDependencies = Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-connect" % sparkVersion % "provided",
  "com.amazon.deequ" % "deequ" % s"${deequVersion}-spark-${sparkVersionShort}" excludeAll(
    ExclusionRule(organization = "org.apache.spark"),
    ExclusionRule(organization = "org.scala-lang"),
    ExclusionRule(organization = "commons-io"),
    ExclusionRule(organization = "commons-codec"),
    ExclusionRule(organization = "commons-lang"),
    ExclusionRule(organization = "org.json4s"),
    ExclusionRule(organization = "joda-time")
  ),
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

lazy val commonMergeStrategy = assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "tsumugi",
    libraryDependencies ++= commonDependencies,
    assembly / assemblyJarName := s"${name.value}-shaded-only-${version.value}.jar",
    commonMergeStrategy,
    assembly / assemblyExcludedJars := {
      val cp = (assembly / fullClasspath).value
      cp filter { f =>
        f.data.getName.contains("spark-") ||
        f.data.getName.contains("deequ") ||
        f.data.getName.contains("protobuf")
      }
    })

lazy val withDeequ = (project in file("withDeequ"))
  .dependsOn(root)
  .settings(commonSettings)
  .settings(
    name := "tsumugi",
    libraryDependencies ++= commonDependencies,
    assembly / assemblyJarName := s"${name.value}-server-${version.value}.jar",
    commonMergeStrategy,
    assembly / assemblyExcludedJars := {
      val cp = (assembly / fullClasspath).value
      cp filter { dep =>
        val name = dep.data.getName
        val path = dep.data.getPath
        val keepPatterns = Seq("deequ", "tsumugi")
        !keepPatterns.exists(pattern => name.contains(pattern) || path.contains(pattern))
      }
    },
    assembly / assemblyShadeRules ++= Seq(
      ShadeRule.rename("com.amazon.deequ.**" -> "shaded.com.amazon.deequ.@1").inAll
    ))

addCommandAlias("assemblyShadeOnly", "assembly")
addCommandAlias("assemblyWithDeequ", "withDeequ/assembly")