package net.vonbuchholtz.dependencychecksbt

import sbt._

trait DependencyCheckKeys {

	// Configuration
	lazy val dependencyCheckAutoUpdate = SettingKey[Boolean]("autoUpdate", "Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not recommended that this be turned to false.")
	lazy val dependencyCheckCveValidForHours = SettingKey[Int]("cveValidForHours","")
	lazy val dependencyCheckFailBuildOnCVSS = SettingKey[Int]("failBuildOnCVSS","")
	lazy val dependencyCheckFormat = SettingKey[String]("format","")
	lazy val dependencyCheckName = SettingKey[String]("name","")
	lazy val dependencyCheckOutputDirectory = SettingKey[File]("outputDirectory","")
	lazy val dependencyCheckSkip = SettingKey[Boolean]("skip","")
	lazy val dependencyCheckSkipTestScope = SettingKey[Boolean]("skipTestScope","")
	lazy val dependencyCheckSkipRuntimeScope = SettingKey[Boolean]("skipRuntimeScope","")
	lazy val dependencyCheckSuppressionFile = SettingKey[File]("suppressionFile","")

	// Analyzer configuration
	lazy val dependencyCheckArchiveAnalyzerEnabled = SettingKey[Boolean]("archiveAnalyzerEnabled","")
	lazy val dependencyCheckZipExtensions = SettingKey[String]("zipExtensions","")
	lazy val dependencyCheckJarAnalyzer = SettingKey[Boolean]("jarAnalyzer","")
	lazy val dependencyCheckCentralAnalyzerEnabled = SettingKey[Boolean]("centralAnalyzerEnabled","")
	lazy val dependencyCheckNexusAnalyzerEnabled = SettingKey[Boolean]("nexusAnalyzerEnabled","")
	lazy val dependencyCheckNexusUrl = SettingKey[String]("nexusUrl","")
	lazy val dependencyCheckNexusUsesProxy = SettingKey[Boolean]("nexusUsesProxy","")
	lazy val dependencyCheckPyDistributionAnalyzerEnabled = SettingKey[Boolean]("pyDistributionAnalyzerEnabled","")
	lazy val dependencyCheckPyPackageAnalyzerEnabled = SettingKey[Boolean]("pyPackageAnalyzerEnabled","")
	lazy val dependencyCheckRubygemsAnalyzerEnabled = SettingKey[Boolean]("rubygemsAnalyzerEnabled","")
	lazy val dependencyCheckOpensslAnalyzerEnabled = SettingKey[Boolean]("opensslAnalyzerEnabled","")
	lazy val dependencyCheckCmakeAnalyzerEnabled = SettingKey[Boolean]("cmakeAnalyzerEnabled","")
	lazy val dependencyCheckAutoconfAnalyzerEnabled = SettingKey[Boolean]("autoconfAnalyzerEnabled","")
	lazy val dependencyCheckComposerAnalyzerEnabled = SettingKey[Boolean]("composerAnalyzerEnabled","")
	lazy val dependencyCheckNodeAnalyzerEnabled = SettingKey[Boolean]("nodeAnalyzerEnabled","")
	lazy val dependencyCheckNuspecAnalyzerEnabled = SettingKey[Boolean]("rubygemsAnalyzerEnabled","")
	lazy val dependencyCheckAssemblyAnalyzerEnabled = SettingKey[Boolean]("assemblyAnalyzerEnabled","")
	lazy val dependencyCheckPathToMono = SettingKey[File]("pathToMono","")

	// Advanced configuration
	lazy val dependencyCheckCveUrl12Modified = SettingKey[URL]("cveUrl12Modified","")
	lazy val dependencyCheckCveUrl20Modified = SettingKey[URL]("cveUrl20Modified","")
	lazy val dependencyCheckCveUrl12Base = SettingKey[String]("pathToMcveUrl12Baseono","")
	lazy val dependencyCheckCveUrl20Base = SettingKey[String]("cveUrl20Base","")
	lazy val dependencyCheckConnectionTimeout = SettingKey[Int]("connectionTimeout","")
	lazy val dependencyCheckDataDirectory = SettingKey[File]("dataDirectory","")
	lazy val dependencyCheckDatabaseDriverName = SettingKey[String]("databaseDriverName","")
	lazy val dependencyCheckDatabaseDriverPath = SettingKey[File]("databaseDriverPath","")
	lazy val dependencyCheckConnectionString = SettingKey[String]("connectionString","")
	lazy val dependencyCheckDatabaseUser = SettingKey[String]("databaseUser","")
	lazy val dependencyCheckDatabasePassword = SettingKey[String]("databasePassword","")
	lazy val dependencyCheckMetaFileName = SettingKey[String]("metaFileName","")

	// TaskKeys
	lazy val dependencyCheckTask = TaskKey[Unit]("check", "Runs dependency-check against the project and generates a report.")
	lazy val dependencyCheckAggregate = TaskKey[File]("aggregate", "Runs dependency-check against the child projects and aggregates the results into a single report.")
	lazy val dependencyCheckUpdateOnly = TaskKey[Unit]("update-only", "Updates the local cache of the NVD data from NIST.")
	lazy val dependencyCheckPurge = TaskKey[Unit]("purge", "Deletes the local copy of the NVD. This is used to force a refresh of the data.")
}

object DependencyCheckKeys extends DependencyCheckKeys
