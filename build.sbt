
organization := "net.vonbuchholtz"
name := "sbt-dependency-check"

version := "1.0-SNAPSHOT"
scalaVersion := "2.10.6"
sbtPlugin := true

publishMavenStyle := false
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

libraryDependencies ++= Seq(
"org.owasp" % "dependency-check-core" % "1.3.6",
// TODO check if we need this at all
"org.owasp" % "dependency-check-maven" % "1.3.6",
"org.slf4j" % "slf4j-log4j12" % "1.7.13"
)

coverageHighlighting := false

// Settings to build a nice looking plugin site
site.settings
com.typesafe.sbt.SbtSite.SiteKeys.siteMappings <+= baseDirectory map { dir =>
	val nojekyll = dir / "src" / "site" / ".nojekyll"
	nojekyll -> ".nojekyll"
}
site.sphinxSupport()
site.includeScaladoc()