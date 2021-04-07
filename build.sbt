import sbt.{Project, _}
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations.setNextVersion

val sbtDependencyCheck = (project in file("."))
	.enablePlugins(SbtPlugin)

organization := "net.vonbuchholtz"
name := "sbt-dependency-check"

crossSbtVersions := Vector("1.2.8")
sbtPlugin := true

libraryDependencies ++= Seq(
	"commons-collections" % "commons-collections" % "3.2.2",
	"org.owasp" % "dependency-check-core" % "6.1.5",
	"com.google.guava" % "guava" % "30.1.1-jre"
)

dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang") | moduleFilter(organization = "org.scala-sbt")
dependencyUpdatesFailBuild := true

dependencyCheckFailBuildOnCVSS := 0
dependencyCheckSkipProvidedScope := true
dependencyCheckFormat := "ALL"
dependencyCheckSuppressionFiles := Seq(new File("dependency-check-suppressions.xml"))

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
	Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false

publishTo := sonatypePublishToBundle.value
publishMavenStyle := true
sonatypeProfileName := "net.vonbuchholtz"

// To sync with Maven central, you need to supply the following information:
pomExtra in Global := {
	<url>https://github.com/albuch/sbt-dependency-check</url>
		<licenses>
			<license>
				<name>Apache License Version 2.0</name>
				<url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
			</license>
		</licenses>
		<scm>
			<connection>scm:git:github.com/albuch/sbt-dependency-check</connection>
			<developerConnection>scm:git:git@github.com:albuch/sbt-dependency-check</developerConnection>
			<url>https://github.com/albuch/sbt-dependency-check</url>
		</scm>
		<developers>
			<developer>
				<id>albuch</id>
				<name>Alexander v. Buchholtz</name>
				<url>https://github.com/albuch/</url>
			</developer>
		</developers>
}



releaseProcess := Seq[ReleaseStep](
	checkSnapshotDependencies,
	inquireVersions,
	runClean,
	releaseStepCommandAndRemaining("^ test"),
	releaseStepCommandAndRemaining("^ scripted"),
	setReleaseVersion,
	commitReleaseVersion,
	setReleaseVersionInReadme,
	tagRelease,
	releaseStepCommandAndRemaining("^ publishSigned"),
	releaseStepCommandAndRemaining("sonatypeBundleRelease"),
	setNextVersion,
	commitNextVersion
	//,pushChanges
)

lazy val setReleaseVersionInReadme: ReleaseStep = ReleaseStep(action = { st: State =>

	val extracted = Project.extract(st)
	val currentV = extracted.get(version)
	st.log.info("Setting version to '%s' in README." format currentV)
	val file: String = "README.md"
	var readme: String = read(file)
	readme = readme.replaceAll("(addSbtPlugin\\(\"net.vonbuchholtz\" % \"sbt-dependency-check\" % \")[^\"]+", "$1" + currentV)
	write(file, readme)
	st
})

def write(path: String, txt: String): Unit = {
	import java.nio.charset.StandardCharsets
	import java.nio.file.{Files, Paths}

	Files.write(Paths.get(path), txt.getBytes(StandardCharsets.UTF_8))
}

def read(path: String): String = {
	val source = scala.io.Source.fromFile(path, "UTF-8")
	val content = source.mkString
	source.close()
	content
}
