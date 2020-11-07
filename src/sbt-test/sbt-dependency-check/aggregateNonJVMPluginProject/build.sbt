// See https://github.com/albuch/sbt-dependency-check/issues/145

name := "dependency-check-repro"

version := "0.1"

scalaVersion := "2.11.12"

dependencyCheckAutoUpdate := Some(false)

val foo = project
  .disablePlugins(sbt.plugins.JvmPlugin)
  .settings(
    dependencyCheckSkip := false,
    Compile / products := Nil
  )
