// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")

// https://github.com/jrudolph/sbt-dependency-graph
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")


Compile / unmanagedSourceDirectories += baseDirectory.value.getParentFile / "src" / "main" / "scala"
libraryDependencies ++= Seq(
  "com.google.code.gson" % "gson" % "2.9.0",
  "org.owasp" % "dependency-check-core" % "7.1.0",
  "org.slf4j" % "slf4j-simple" % "2.0.0"
)
