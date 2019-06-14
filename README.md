[![Codacy Badge](https://api.codacy.com/project/badge/Grade/25bd3b5e4f8e4ee78cfbdca62de31ca7)](https://app.codacy.com/app/albuch/sbt-dependency-check?utm_source=github.com&utm_medium=referral&utm_content=albuch/sbt-dependency-check&utm_campaign=Badge_Grade_Dashboard)
# sbt-dependency-check [![Build Status](https://travis-ci.org/albuch/sbt-dependency-check.svg)](https://travis-ci.org/albuch/sbt-dependency-check) [![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
The sbt-dependency-check plugin allows projects to monitor dependent libraries for known, published vulnerabilities
(e.g. CVEs). The plugin achieves this by using the awesome [OWASP DependencyCheck library](https://github.com/jeremylong/DependencyCheck)
which already offers several integrations with other build and continuous integration systems.
For more information on how OWASP DependencyCheck works and how to read the reports check the [project's documentation](https://jeremylong.github.io/DependencyCheck/index.html).
## Getting started
sbt-dependency-check is an AutoPlugin, so you need sbt 0.13.5+. Simply add the plugin to `project/plugins.sbt` file.

    addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "1.0.0")

For sbt 1.0.0+ use version `0.1.10` or higher.

## Usage
### Tasks
Task | Description | Command
:-------|:------------|:-----
dependencyCheck | Runs dependency-check against the current project, its aggregates and dependencies and generates a report for each project. | ```$ sbt dependencyCheck```
dependencyCheckAggregate | Runs dependency-check against the current project, it's aggregates and dependencies and generates a single report in the current project's output directory. | ```$ sbt dependencyCheckAggregate```
dependencyCheckUpdateOnly | Updates the local cache of the NVD data from NIST. | ```$ sbt dependencyCheckUpdateOnly```
dependencyCheckPurge | Deletes the local copy of the NVD. This is used to force a refresh of the data. | ```$ sbt dependencyCheckPurge```
dependencyCheckListSettings | Prints all settings and their values for the project. | ```$ sbt dependencyCheckListSettings```


The reports will be written to the default location `crossTarget.value`. This can be overwritten by setting `dependencyCheckOutputDirectory`. See Configuration for details.

**Note:** The first run might take a while as the full data from the National Vulnerability Database (NVD) hosted by NIST: <https://nvd.nist.gov> has to be downloaded and imported into the database.
Later runs will only download change sets unless the last update was more than 7 days ago.

### Configuration
`sbt-dependency-check` uses the default configuration of [OWASP DependencyCheck](https://github.com/jeremylong/DependencyCheck).
You can override them in your `build.sbt` files.
Use the task `dependencyCheckListSettings` to print all available settings and their values to sbt console.

Setting | Description | Default Value
:-------|:------------|:-------------
dependencyCheckAutoUpdate | Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not recommended that this be turned to false. | true
dependencyCheckCveValidForHours | Sets the number of hours to wait before checking for new updates from the NVD. | 4
dependencyCheckFailBuildOnCVSS | Specifies if the build should be failed if a CVSS score above, or equal to, a specified level is identified. The default is 11 which means since the CVSS scores are 0-10, by default the build will never fail. | 11.0
dependencyCheckJUnitFailOnCVSS | If using the JUNIT report format the dependencyCheckJUnitFailOnCVSS sets the CVSS score threshold that is considered a failure. The default value is 0 - all vulnerabilities are considered a failure.| 0
dependencyCheckFormat | The report format to be generated (HTML, XML, JUNIT, CSV, JSON, ALL). This setting is ignored if dependencyCheckReportFormats is set. | HTML
dependencyCheckFormats | A sequence of report formats to be generated (HTML, XML, JUNIT, CSV, JSON, ALL). | 
dependencyCheckOutputDirectory | The location to write the report(s). | `crossTarget.value` e.g. `./target/scala-2.11`
dependencyCheckScanSet | An optional sequence of files that specify additional files and/or directories to analyze as part of the scan. If not specified, defaults to standard scala conventions (see [SBT documentation](http://www.scala-sbt.org/0.13/docs/Directories.html#Source+code) for details). | `/src/main/resources`
dependencyCheckSkip | Skips the dependency-check analysis |  false
dependencyCheckSkipTestScope | Skips analysis for artifacts with Test Scope | true
dependencyCheckSkipRuntimeScope | Skips analysis for artifacts with Runtime Scope | false
dependencyCheckSkipProvidedScope | Skips analysis for artifacts with Provided Scope | false
dependencyCheckSkipOptionalScope | Skips analysis for artifacts with Optional Scope | false
dependencyCheckSuppressionFiles | The sequence of file paths to the XML suppression files - used to suppress false positives. See [Suppressing False Positives](https://jeremylong.github.io/DependencyCheck/general/suppression.html) for the file syntax. |
dependencyCheckHintsFile | The file path to the XML hints file - used to resolve [false negatives](https://jeremylong.github.io/DependencyCheck/general/hints.html). |
dependencyCheckUseSbtModuleIdAsGav | Use the SBT ModuleId as GAV identifier. Ensures GAV is available even if Maven Central isn't. | false
dependencyCheckAnalysisTimeout | Set the analysis timeout in minutes | 20 
dependencyCheckEnableExperimental | Enable the experimental analyzers. If not enabled the experimental analyzers (see below) will not be loaded or used. | false
dependencyCheckEnableRetired | Enable the retired analyzers. If not enabled retired analyzers will not be loaded or used. | false

#### Analyzer Configuration
The following properties are used to configure the various file type analyzers. These properties can be used to turn off specific analyzers if it is not needed. Note, that specific analyzers will automatically disable themselves if no file types that they support are detected - so specifically disabling them may not be needed.
For more information about the individual analyzers see the [DependencyCheck Analyzer documentation](https://jeremylong.github.io/DependencyCheck/analyzers/index.html).

Setting | Description | Default Value
:-------|:------------|:-------------
dependencyCheckArchiveAnalyzerEnabled | Sets whether the Archive Analyzer will be used. | true
dependencyCheckZipExtensions | A comma-separated list of additional file extensions to be treated like a ZIP file, the contents will be extracted and analyzed. |
dependencyCheckJarAnalyzerEnabled | Sets whether Jar Analyzer will be used.  | true
dependencyCheckCentralAnalyzerEnabled | Sets whether Central Analyzer will be used. If this analyzer is being disabled there is a good chance you also want to disable the Nexus Analyzer (see below). | false
dependencyCheckCentralAnalyzerUseCache | Sets whether the Central Analyer will cache results. Cached results expire after 30 days. | true
dependencyCheckOSSIndexAnalyzerEnabled | Sets whether the OSS Index Analyzer will be enabled. | true
dependencyCheckOSSIndexAnalyzerUrl | URL of the Sonatype OSS Index service. | https://ossindex.sonatype.org
dependencyCheckOSSIndexAnalyzerUseCache | Sets whether the OSS Index Analyzer will cache results. Cached results expire after 24 hours. | true
dependencyCheckNexusAnalyzerEnabled | Sets whether Nexus Analyzer will be used. This analyzer is superseded by the Central Analyzer; however, you can configure this to run against a Nexus Pro installation. | false
dependencyCheckNexusUrl | Defines the Nexus Server’s web service end point (example http://domain.enterprise/service/local/). If not set the Nexus Analyzer will be disabled. | <https://repository.sonatype.org/service/local/>
dependencyCheckNexusUsesProxy | Whether or not the defined proxy should be used when connecting to Nexus. | true
dependencyCheckNexusUser | The username to authenticate to the Nexus Server's web service end point. If not set the Nexus Analyzer will use an unauthenticated connection. | 
dependencyCheckNexusPassword | The password to authenticate to the Nexus Server's web service end point. If not set the Nexus Analyzer will use an unauthenticated connection. |
dependencyCheckPyDistributionAnalyzerEnabled | Sets whether the _experimental_ Python Distribution Analyzer will be used.  | true
dependencyCheckPyPackageAnalyzerEnabled | Sets whether the _experimental_ Python Package Analyzer will be used. | true
dependencyCheckRubygemsAnalyzerEnabled | Sets whether the _experimental_ Ruby Gemspec Analyzer will be used. | true
dependencyCheckOpensslAnalyzerEnabled | Sets whether or not the openssl Analyzer should be used. | true
dependencyCheckCmakeAnalyzerEnabled | Sets whether or not the _experimental_ CMake Analyzer should be used. | true
dependencyCheckAutoconfAnalyzerEnabled | Sets whether or not the _experimental_ autoconf Analyzer should be used. | true
dependencyCheckComposerAnalyzerEnabled | Sets whether or not the _experimental_ PHP Composer Lock File Analyzer should be used. | true
dependencyCheckNodeAnalyzerEnabled | Sets whether or not the _retired_ Node.js Analyzer should be used. | false
dependencyCheckNodeAuditAnalyzerEnabled | Sets whether or not the Node Audit Analyzer should be used. | true
dependencyCheckNodeAuditAnalyzerUrl | Sets the URL to the NPM Audit API. If not set uses default URL. | 
dependencyCheckNodeAuditAnalyzerUseCache | Sets whether the Node Audit Analyzer will cache results. Cached results expire after 24 hours. | true
dependencyCheckNuspecAnalyzerEnabled | Sets whether or not the .NET Nuget Nuspec Analyzer will be used. | true
dependencyCheckNugetConfAnalyzerEnabled | Sets whether the _experimental_ .NET Nuget packages.config Analyzer will be used. | false
dependencyCheckCocoapodsEnabled | Sets whether or not the _experimental_ Cocoapods Analyzer should be used. | true
dependencyCheckSwiftEnabled | Sets whether or not the _experimental_ Swift Package Manager Analyzer should be used. | true
dependencyCheckBundleAuditEnabled | Sets whether or not the Ruby Bundle Audit Analyzer should be used. | true
dependencyCheckPathToBundleAudit| The path to bundle audit. |
dependencyCheckAssemblyAnalyzerEnabled | Sets whether or not the .NET Assembly Analyzer should be used. | true
dependencyCheckPathToDotNETCore | The path to .NET Core for .NET assembly analysis on non-windows systems. |
dependencyCheckRetireJSAnalyzerEnabled | Sets whether or not the RetireJS Analyzer should be used. | true
dependencyCheckRetireJSAnalyzerRepoJSUrl | Set the URL to the RetireJS repository | https://raw.githubusercontent.com/Retirejs/retire.js/master/repository/jsrepository.json 
dependencyCheckRetireJsAnalyzerRepoValidFor | Set the interval in hours until the next check for CVEs updates is performed by the RetireJS analyzer | 24
dependencyCheckRetireJsAnalyzerFilters | Set one or more filters for the RetireJS analyzer. | 
dependencyCheckRetireJsAnalyzerFilterNonVulnerable | Sets whether or not the RetireJS analyzer should filter non-vulnerable dependencies | false
dependencyCheckArtifactoryAnalyzerEnabled | Sets whether or not the JFrog Artifactory analyzer will be used | false
dependencyCheckArtifactoryAnalyzerUrl | The Artifactory server URL. | 
dependencyCheckArtifactoryAnalyzerUseProxy | Sets whether Artifactory should be accessed through a proxy or not. | false
dependencyCheckArtifactoryAnalyzerParallelAnalysis | Sets whether the Artifactory analyzer should be run in parallel or not. | true 
dependencyCheckArtifactoryAnalyzerUsername | The user name (only used with API token) to connect to Artifactory instance. | 
dependencyCheckArtifactoryAnalyzerApiToken | The API token to connect to Artifactory instance. __Note:__ These settings should not be added to your local `build.sbt` file and commited to your code repository for security reasons. They can be added to `~/.sbt/<version>/global.sbt` file instead  | 
dependencyCheckArtifactoryAnalyzerBearerToken | The bearer token to connect to Artifactory instance. __Note:__ These settings should not be added to your local `build.sbt` file and commited to your code repository for security reasons. They can be added to `~/.sbt/<version>/global.sbt` file instead | 

#### Advanced Configuration
The following properties can be configured in the plugin. However, they are less frequently changed. One exception may be the cvedUrl properties, which can be used to host a mirror of the NVD within an enterprise environment.

Setting | Description | Default Value
:-------|:------------|:-------------
dependencyCheckCveUrlModified | URL for the modified CVE JSON data feed. When mirroring the NVD you must mirror the *.json.gz and the *.meta files. | <https://nvd.nist.gov/feeds/json/cve/1.0/nvdcve-1.0-modified.json.gz>
dependencyCheckCveUrlBase | Base URL for each year's CVE JSON data feed, the %d will be replaced with the year. | <https://nvd.nist.gov/feeds/json/cve/1.0/nvdcve-1.0-%d.json.gz>
dependencyCheckConnectionTimeout | Sets the URL Connection Timeout used when downloading external data. |
dependencyCheckDataDirectory | Sets the data directory to hold SQL CVEs contents. This should generally not be changed. | [JAR]\data
dependencyCheckDatabaseDriverName | The name of the database driver. Example: org.h2.Driver. | org.h2.Driver
dependencyCheckDatabaseDriverPath | The path to the database driver JAR file; only used if the driver is not in the class path. |
dependencyCheckConnectionString | The connection string used to connect to the database, the %s will be replace with a name for the database | jdbc:h2:file:%s;AUTOCOMMIT=ON;MV_STORE=FALSE;
dependencyCheckDatabaseUser | The username used when connecting to the database. | dcuser
dependencyCheckDatabasePassword | The password used when connecting to the database. |
dependencyCheckCpeStartsWith | The starting String to identify the CPEs that are qualified to be imported. | 

#### Changing Log Level
Add the following to your `build.sbt` file to increase the log level from  default `info` to e.g. `debug`.
```
logLevel := Level.Debug
initialize ~= { _ =>
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

### Global Plugin Configuration
If you want to apply some configuration for all your SBT projects you can add them as Global Settings:

1. Add the plugin to `~/sbt/1.0/plugins/sbt-dependency-check.sbt`
    ```Scala
    addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "1.0.0")
    ```

1. Add settings at `~/.sbt/1.0/global.sbt` using their fully qualified name (including package and nested object structure). E.g.
    ```Scala
    net.vonbuchholtz.sbt.dependencycheck.DependencyCheckPlugin.autoImport.dependencyCheckFormat := "All"
    ```

For further information about global settings and plugins check the sbt documentation: https://www.scala-sbt.org/1.x/docs/Global-Settings.html

### Running behind a proxy
SBT and `sbt-dependency-check` both honor the standard http and https proxy settings for the JVM.

    sbt -Dhttp.proxyHost=proxy.example.com \
        -Dhttp.proxyPort=3218 \
        -Dhttp.proxyUser=username \
        -Dhttp.proxyPassword=password \
        -DnoProxyHosts="localhost|http://www.google.com" \
        dependencyCheck

## License
Copyright (c) 2017 Alexander v. Buchholtz

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
