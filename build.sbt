import sbt.{Project, _}
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._

organization := "net.vonbuchholtz"
name := "sbt-dependency-check"

scalaVersion := "2.10.6"
sbtPlugin := true

libraryDependencies ++= Seq(
	"org.owasp" % "dependency-check-core" % "1.4.5",
	"org.slf4j" % "slf4j-simple" % "1.7.25"
)

dependencyUpdatesExclusions := moduleFilter(organization = "org.scala-lang") | moduleFilter(organization = "org.scala-sbt")
dependencyUpdatesFailBuild := true

ScriptedPlugin.scriptedSettings
scriptedLaunchOpts := { scriptedLaunchOpts.value ++
	Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false

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
			<url>github.com/albuch/sbt-dependency-check</url>
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
	runTest,
	releaseStepInputTask(scripted),
	setReleaseVersion,
	commitReleaseVersion,
	setReleaseVersionInReadme,
	tagRelease,
	ReleaseStep(action = Command.process("publishSigned", _)),
	setNextVersion,
	commitNextVersion,
	ReleaseStep(action = Command.process("sonatypeReleaseAll", _))
	//,pushChanges
)

lazy val setReleaseVersionInReadme: ReleaseStep = ReleaseStep(action = { st: State =>

	val extracted = Project.extract(st)
	val currentV = extracted.get(version)
	st.log.info("Setting version to '%s' in README." format currentV)
	val file: String = "README.md"
	var readme: String = read(file)
	readme = readme.replaceAll("(addSbtPlugin\\(\"net.vonbuchholtz\" % \"sbt-dependency-check\" % \")[^\\\"]+", "$1" + currentV)
	write(file, readme)
	st
})

def write(path: String, txt: String): Unit = {
	import java.nio.charset.StandardCharsets
	import java.nio.file.{Files, Paths}

	Files.write(Paths.get(path), txt.getBytes(StandardCharsets.UTF_8))
}

def read(path: String): String =
	scala.io.Source.fromFile(path, "UTF-8").mkString

