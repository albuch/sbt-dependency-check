publishMavenStyle := true
sonatypeProfileName := "net.vonbuchholtz"

// To sync with Maven central, you need to supply the following information:
pomExtra in Global := {
  <url>https://github.com/albuch/sbt-dependency-check</url>
    <licenses>
      <license>
        <name>Apache License Version 2.0</name>
        <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/albuch/sbt-dependency-check</connection>
      <developerConnection>scm:git:git@github.com:albuch/sbt-dependency-check</developerConnection>
      <url>github.com/albuch/sbt-dependency-check</url>
    </scm>
    <developers>
      <developer>
        <id>albuch</id>
        <name>Alexander v. Buchholtz</name>
        <url>https://github.com/albuch/</url>
      </developer>
    </developers>
}