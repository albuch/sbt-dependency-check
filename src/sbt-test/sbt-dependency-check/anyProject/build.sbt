lazy val commonSettings = Seq(
  organization := "net.vonbuchholtz",
  version := "0.1.0",
  scalaVersion := "2.10.7"
)

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(commonSettings: _*)
  .settings(
    dependencyCheckFailBuildOnCVSS := 0
  )

lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"
  )

lazy val inScope = (project in file("inScope"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind"  % "2.9.9"
  )