// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.1")

// https://github.com/jrudolph/sbt-dependency-graph
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / "src" / "main" / "scala"
libraryDependencies ++= Seq(
  "commons-collections" % "commons-collections" % "3.2.2",
  "org.owasp" % "dependency-check-core" % "2.1.0",
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)
