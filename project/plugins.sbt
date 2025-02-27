// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// https://github.com/jrudolph/sbt-dependency-graph
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")


Compile / unmanagedSourceDirectories += baseDirectory.value.getParentFile / "src" / "main" / "scala"
libraryDependencies ++= Seq(
  "org.owasp" % "dependency-check-core" % "8.1.2",
  "org.slf4j" % "slf4j-simple" % "2.0.17"
)
