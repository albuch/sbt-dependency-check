# sbt-dependency-check [![Build Status](https://travis-ci.org/albuch/sbt-dependency-check.svg)](https://travis-ci.org/albuch/sbt-dependency-check) [![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
The sbt-dependency-check plugin allows projects to monitor dependent libraries for known, published vulnerabilities (e.g. CVEs). The plugin achieves this by using the awesome [OWASP DependencyCheck library](https://github.com/jeremylong/DependencyCheck) which already offers several other build system and CI integrations.
## Getting started
`sbt-dependency-check` is an AutoPlugin, so you need sbt 0.13.5+. Simply add the plugin to `project/plugins.sbt` file.

    addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "1.0-SNAPSHOT")

## Usage
### Tasks
#### check
Runs dependency-check against the current project,its aggregate and dependencies and generates a report for each project.

    $ sbt check

The report will be written to the default location `crossTarget.value`. This can be overwritten by setting `dependencyCheckOutputDirectory`.

    dependencyCheckOutputDirectory := Some(new File("./target"))

#### aggregate-check
Runs dependency-check against the current project, it's aggregates and dependencies and generates a single report
 in the current project's output directory.

    $ sbt aggregate-check

#### update-only
Updates the local cache of the NVD data from NIST.

    $ sbt update-only

#### purge
Deletes the local copy of the NVD. This is used to force a refresh of the data.

    $ sbt purge

#### list-settings
Prints all settings and their values for the project.

    $ sbt list-settings

### Configuration
`sbt-dependency-check` uses the default configuration of OWASP [DependencyCheck](https://github.com/jeremylong/DependencyCheck). You can override them in your `build.sbt` files.
Use the task `list-settings` to print all available settings to sbt console.

The default values are identical to the [DependencyCheck Maven plugin](http://jeremylong.github.io/DependencyCheck/dependency-check-maven/configuration.html).

#### Changing Log Level
Add the following to your `build.sbt` file to increase the log level from  default `info` to `debug`.
```
logLevel in (dependencyCheck, dependencyCheckAggregate, dependencyCheckPurge, dependencyCheckUpdateOnly) := Level.Debug
initialize in (dependencyCheck, dependencyCheckAggregate, dependencyCheckPurge, dependencyCheckUpdateOnly) ~= { _ =>
    sys.props += (("org.slf4j.simpleLogger.log.org.owasp", "debug"))
}
```
### Multi-Project setup

Add all plugin settings to commonSettings that you pass to your projects.

**build.sbt**
```Scala
lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0",
  scalaVersion := "2.10.6",
  // Add your sbt-dependency-check settings
  dependencyCheckFormat := Some("All")
)

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(commonSettings: _*)
  .settings(
	libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "2.6" % "test",
	dependencyCheckSkipTestScope := false
  )

lazy val util = (project in file("util"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "commons-beanutils" % "commons-beanutils" % "1.9.1"
  )

lazy val core = project.dependsOn(util)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1" % "runtime",
    dependencyCheckSkip := true
  )

```

The only settings, that are supported to work for `aggregate()` and `dependsOn()` projects, are the scope skipping ones:
* `dependencyCheckSkip`
* `dependencyCheckSkipTestScope`
* `dependencyCheckSkipRuntimeScope`
* `dependencyCheckSkipProvidedScope`
* `dependencyCheckSkipOptionalScope`

You can set these individually for each project.

### Running behind a proxy
SBT and `sbt-dependency-check` both honor the standard http and https proxy settings for the JVM.

    sbt -Dhttp.proxyHost=proxy.example.com \
        -Dhttp.proxyPort=3218 \
        -Dhttp.proxyUser=username \
        -Dhttp.proxyPassword=password \
        -DnoProxyHosts="localhost|http://www.google.com" \
        check

## Known issues
* Check task runs forever, CVE database file size increases drastically
  * SBT projects that have a H2 database version 1.4.x in the classpath (e.g. Play Framework projects) will have issues with the dependency check maintenance task that is run after fetching NVD data sets und inserting them in the database. For the initial data import the database file size will increase up to several GB and the task will run for more than 1 hour depending on the hardware resources.
  * Current proposed workaround is to override the dependency to the latest 1.3.x release of H2 database in `project/plugins.sbt`.

    ```sbt
    dependencyOverrides += "com.h2database" % "h2" % "1.3.176"
    ```

## License
Copyright 2016 Alexander v. Buchholtz

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
