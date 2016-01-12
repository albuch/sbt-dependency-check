sbtPlugin := true

name := "dependency-check-sbt"
organization := "com.github.albuch"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.6"

libraryDependencies ++= Seq(
	"org.owasp" % "dependency-check-core" % "1.3.3",
	"org.owasp" % "dependency-check-maven" % "1.3.3",
	"org.slf4j" % "slf4j-log4j12" % "1.7.13"
)

publishMavenStyle := false