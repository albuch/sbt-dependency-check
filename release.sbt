import sbt.{Project, _}
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._

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
