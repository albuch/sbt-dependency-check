lazy val commonSettings = Seq(
  organization := "net.vonbuchholtz",
  version := "0.1.0",
  scalaVersion := "2.10.6"
)

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "org.eclipse.jetty" % "jetty-runner" % "9.2.4.v20141103" % "provided",
    dependencyCheckSkipTestScope := false
  )

lazy val util = (project in file("util"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies +=   "commons-beanutils" % "commons-beanutils" % "1.9.1" % "test"
  )

lazy val core = project.dependsOn(util)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"
  )
