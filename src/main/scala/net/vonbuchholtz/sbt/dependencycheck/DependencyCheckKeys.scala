package net.vonbuchholtz.sbt.dependencycheck

import sbt._

trait DependencyCheckKeys {

	// Configuration
	lazy val dependencyCheckAutoUpdate = settingKey[Option[Boolean]]("Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not recommended that this be turned to false.")
	lazy val dependencyCheckCveValidForHours = settingKey[Option[Int]]("Sets the number of hours to wait before checking for new updates from the NVD.")
	lazy val dependencyCheckFailBuildOnCVSS = settingKey[Float]("Specifies if the build should be failed if a CVSS score above a specified level is identified. The default is 11 which means since the CVSS scores are 0-10, by default the build will never fail.")
	lazy val dependencyCheckFormat = settingKey[String]("The report format to be generated (HTML, XML, VULN, ALL). This configuration option has no affect if using this within the Site plugin unless the externalReport is set to true.")
	lazy val dependencyCheckOutputDirectory = settingKey[Option[File]]("The location to write the report(s).")
	lazy val dependencyCheckSkip = settingKey[Boolean]("Skips the dependency-check analysis ")
	lazy val dependencyCheckSkipTestScope = settingKey[Boolean]("Skips analysis for artifacts with Test Scope ")
	lazy val dependencyCheckSkipRuntimeScope = settingKey[Boolean]("Skips analysis for artifacts with Runtime Scope ")
	lazy val dependencyCheckSkipProvidedScope = settingKey[Boolean]("Skips analysis for artifacts with Provided Scope ")
	lazy val dependencyCheckSkipOptionalScope = settingKey[Boolean]("Skips analysis for artifacts with Optional Scope ")
	lazy val dependencyCheckSuppressionFile = settingKey[Option[File]]("The file path to the XML suppression file - used to suppress false positives")
	lazy val dependencyCheckHintsFile = settingKey[Option[File]]("The file path to the XML hints file - used to resolve false negatives.")
	lazy val dependencyCheckEnableExperimental = settingKey[Option[Boolean]]("Enable the experimental analyzers. If not enabled the experimental analyzers (see below) will not be loaded or used. ")
	// Analyzer configuration
	lazy val dependencyCheckArchiveAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether the Archive Analyzer will be used. ")
	lazy val dependencyCheckZipExtensions = settingKey[Option[String]]("A comma-separated list of additional file extensions to be treated like a ZIP file, the contents will be extracted and analyzed. ")
	lazy val dependencyCheckJarAnalyzer = settingKey[Option[Boolean]]("Sets whether Jar Analyzer will be used. ")
	lazy val dependencyCheckCentralAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether Central Analyzer will be used. If this analyzer is being disabled there is a good chance you also want to disable the Nexus Analyzer (see below). ")
	lazy val dependencyCheckNexusAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether Nexus Analyzer will be used. This analyzer is superceded by the Central Analyzer; however, you can configure this to run against a Nexus Pro installation. ")
	lazy val dependencyCheckNexusUrl = settingKey[Option[URL]]("Defines the Nexus Server’s web service end point (example http://domain.enterprise/service/local/). If not set the Nexus Analyzer will be disabled. ")
	lazy val dependencyCheckNexusUsesProxy = settingKey[Option[Boolean]]("Whether or not the defined proxy should be used when connecting to Nexus. ")
	lazy val dependencyCheckPyDistributionAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether the experimental Python Distribution Analyzer will be used. ")
	lazy val dependencyCheckPyPackageAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether the experimental Python Package Analyzer will be used. ")
	lazy val dependencyCheckRubygemsAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether the experimental Ruby Gemspec Analyzer will be used. ")
	lazy val dependencyCheckOpensslAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether or not the openssl Analyzer should be used. ")
	lazy val dependencyCheckCmakeAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether or not the experimental CMake Analyzer should be used. ")
	lazy val dependencyCheckAutoconfAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether or not the experimental autoconf Analyzer should be used. ")
	lazy val dependencyCheckComposerAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether or not the experimental PHP Composer Lock File Analyzer should be used. ")
	lazy val dependencyCheckNodeAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether or not the experimental Node.js Analyzer should be used. ")
	lazy val dependencyCheckNuspecAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether or not the .NET Nuget Nuspec Analyzer will be used. ")
	lazy val dependencyCheckCocoapodsEnabled = settingKey[Option[Boolean]]("Sets whether or not the experimental Cocoapods Analyzer should be used. ")
	lazy val dependencyCheckSwiftEnabled = settingKey[Option[Boolean]]("Sets whether or not the experimental Swift Package Manager Analyzer should be used. ")
	lazy val dependencyCheckBundleAuditEnabled = settingKey[Option[Boolean]]("Sets whether or not the experimental Ruby Bundle Audit Analyzer should be used. ")
	lazy val dependencyCheckPathToBundleAudit = settingKey[Option[File]]("The path to bundle audit. ")
	lazy val dependencyCheckAssemblyAnalyzerEnabled = settingKey[Option[Boolean]]("Sets whether or not the .NET Assembly Analyzer should be used. ")
	lazy val dependencyCheckPathToMono = settingKey[Option[File]]("The path to Mono for .NET assembly analysis on non-windows systems. ")

	// Advanced configuration
	lazy val dependencyCheckCveUrl12Modified = settingKey[Option[URL]]("URL for the modified CVE 1.2. ")
	lazy val dependencyCheckCveUrl20Modified = settingKey[Option[URL]]("URL for the modified CVE 2.0. ")
	lazy val dependencyCheckCveUrl12Base = settingKey[Option[String]]("Base URL for each year’s CVE 1.2, the %d will be replaced with the year. ")
	lazy val dependencyCheckCveUrl20Base = settingKey[Option[String]]("Base URL for each year’s CVE 2.0, the %d will be replaced with the year. ")
	lazy val dependencyCheckConnectionTimeout = settingKey[Option[Int]]("Sets the URL Connection Timeout used when downloading external data. ")
	lazy val dependencyCheckDataDirectory = settingKey[Option[File]]("Sets the data directory to hold SQL CVEs contents. This should generally not be changed. ")
	lazy val dependencyCheckDatabaseDriverName = settingKey[Option[String]]("The name of the database driver. Example: org.h2.Driver. ")
	lazy val dependencyCheckDatabaseDriverPath = settingKey[Option[File]]("The path to the database driver JAR file; only used if the driver is not in the class path. ")
	lazy val dependencyCheckConnectionString = settingKey[Option[String]]("The connection string used to connect to the database. ")
	lazy val dependencyCheckDatabaseUser = settingKey[Option[String]]("The username used when connecting to the database. ")
	lazy val dependencyCheckDatabasePassword = settingKey[Option[String]]("The password used when connecting to the database. ")
	lazy val dependencyCheckMetaFileName = settingKey[Option[String]]("CURRENTLY NOT USED. Sets the name of the file to use for storing the metadata about the project. ")

	// TaskKeys
	lazy val dependencyCheck = TaskKey[Unit]("dependencyCheck", "Runs dependency-check against the project and generates a report.")
	lazy val dependencyCheckAggregate = TaskKey[Unit]("dependencyCheckAggregate", "Runs dependency-check against the child projects and aggregates the results into a single report.")
	lazy val dependencyCheckUpdateOnly = TaskKey[Unit]("dependencyCheckUpdateOnly", "Updates the local cache of the NVD data from NIST.")
	lazy val dependencyCheckPurge = TaskKey[Unit]("dependencyCheckPurge", "Deletes the local copy of the NVD. This is used to force a refresh of the data.")
	lazy val dependencyCheckListSettings = TaskKey[Unit]("dependencyCheckListSettings", "List the settings of the plugin")
}
