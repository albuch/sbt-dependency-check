
organization := "net.vonbuchholtz"
name := "sbt-dependency-check"

version := "1.0-SNAPSHOT"
scalaVersion := "2.10.6"
sbtPlugin := true

publishMavenStyle := true
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

libraryDependencies ++= Seq(
"org.owasp" % "dependency-check-core" % "1.3.6",
"org.slf4j" % "slf4j-log4j12" % "1.7.13"
)
