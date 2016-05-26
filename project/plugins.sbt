//logLevel := Level.Warn

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.8.2")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")
// Doesn't work until fix for https://github.com/scoverage/sbt-coveralls/issues/73 is released
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.3")