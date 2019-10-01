// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.0")

// https://github.com/jrudolph/sbt-dependency-graph
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")


unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / "src" / "main" / "scala"
libraryDependencies ++= Seq(
  "commons-collections" % "commons-collections" % "20040102.233541",
  "org.owasp" % "dependency-check-core" % "5.2.2",
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)
