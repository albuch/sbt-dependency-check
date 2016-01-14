logLevel := Level.Warn

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.8.2")
// Temorarily downgraded sbt-scoverage and sbt-coverall until fix for https://github.com/scoverage/sbt-coveralls/issues/73 is released
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")