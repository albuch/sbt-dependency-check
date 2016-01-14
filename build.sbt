sbtPlugin := true

name := "dependency-check-sbt"
organization := "com.github.albuch"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.6"

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.3")

libraryDependencies ++= Seq(
	"org.owasp" % "dependency-check-core" % "1.3.3",
	"org.owasp" % "dependency-check-maven" % "1.3.3",
	"org.slf4j" % "slf4j-log4j12" % "1.7.13"
)

publishMavenStyle := false

// Settings to build a nice looking plugin site
site.settings
com.typesafe.sbt.SbtSite.SiteKeys.siteMappings <+= baseDirectory map { dir =>
	val nojekyll = dir / "src" / "site" / ".nojekyll"
	nojekyll -> ".nojekyll"
}
site.sphinxSupport()
site.includeScaladoc()