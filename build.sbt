
organization := "net.vonbuchholtz"
name := "sbt-dependency-check"

scalaVersion := "2.10.6"
sbtPlugin := true

libraryDependencies ++= Seq(
	"org.owasp" % "dependency-check-core" % "1.3.6",
	"org.slf4j" % "slf4j-simple" % "1.7.21"
)
