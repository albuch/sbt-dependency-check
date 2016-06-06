version := "0.1"
lazy val root = project in file(".")
scalaVersion := "2.10.6"

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-runner" % "9.2.4.v20141103"
)

dependencyCheckFailBuildOnCVSS := 1