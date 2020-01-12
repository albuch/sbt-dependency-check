// See https://github.com/albuch/sbt-dependency-check/issues/95

name := "dependency-check-repro"

version := "0.1"

scalaVersion := "2.11.12"


lazy val root = (project in file("."))
  .aggregate(inscope, alsoinscope)


lazy val inscope = (project in file("inscope"))

lazy val alsoinscope = (project in file("alsoinscope"))

lazy val outofscope = (project in file("outofscope")).settings(update := {
  throw new RuntimeException
})
