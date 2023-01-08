// See https://github.com/albuch/sbt-dependency-check/issues/95

name := "global-and-project-settings"
version := "0.1"
scalaVersion := "2.11.12"

Global / dependencyCheckCvePassword := Some("Global")
Global / dependencyCheckCveUser := Some("Global")
ThisBuild / dependencyCheckCveUser := Some("ThisBuild")

lazy val root = (project in file("."))
  .aggregate(inscope, alsoinscope)
  .settings(
    dependencyCheckCvePassword := Some("root"),
  )

lazy val inscope = (project in file("inscope")).settings(
  dependencyCheckCvePassword := Some("inscope")
)
lazy val alsoinscope = (project in file("alsoinscope")).settings(
  TaskKey[Unit]("depCheckAssert") := {
    val thisBuildUser = dependencyCheckCveUser.value
    assert( thisBuildUser.contains("ThisBuild") )
    val thisBuildPassword = dependencyCheckCvePassword.value
    assert( thisBuildPassword.contains("Global") )
  }
)

TaskKey[Unit]("depCheckAssert") := {
  val rootPassword = dependencyCheckCvePassword.value
  val rootInThisBuildUser = ( ThisBuild / dependencyCheckCveUser).value
  val rootInThisBuildPassword = (ThisBuild / dependencyCheckCvePassword).value
  val inscopePassword = (inscope / dependencyCheckCvePassword).value
  val alsoinscopePassword = (alsoinscope / dependencyCheckCvePassword).value
  assert( rootPassword.contains("root") )
  assert( rootInThisBuildUser.contains("ThisBuild") )
  assert( rootInThisBuildPassword.contains("Global") )
  assert( inscopePassword.contains("inscope") )
  assert( alsoinscopePassword.contains("Global") )
}