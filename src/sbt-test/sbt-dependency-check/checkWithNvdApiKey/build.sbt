version := "0.1"
lazy val root = project in file(".")
scalaVersion := "2.10.7"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-collections4" % "4.1",
)

dependencyCheckNvdApiKey := sys.props.get("nvdApiKey")