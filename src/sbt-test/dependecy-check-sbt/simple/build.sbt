version := "0.1"
lazy val root = (project in file("."))
scalaVersion := "2.10.6"

libraryDependencies ++= Seq(
  "commons-beanutils" % "commons-beanutils" % "1.9.1"

)