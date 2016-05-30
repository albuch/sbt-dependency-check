package net.vonbuchholtz.sbt.dependencycheck

import sbt._

trait DependencyCheckKeys {

	//TODO copy docs from dependency-check
	// Configuration
	lazy val dependencyCheckAutoUpdate = settingKey[Option[Boolean]]("Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not recommended that this be turned to false.")
	lazy val dependencyCheckCveValidForHours = settingKey[Option[Int]]("")
	lazy val dependencyCheckFailBuildOnCVSS = settingKey[Option[Float]]("")
	lazy val dependencyCheckFormat = settingKey[String]("")
	lazy val dependencyCheckName = settingKey[Option[String]]("")
	lazy val dependencyCheckOutputDirectory = settingKey[Option[File]]("")
	lazy val dependencyCheckSkip = settingKey[Boolean]("")
	lazy val dependencyCheckSkipTestScope = settingKey[Boolean]("")
	lazy val dependencyCheckSkipRuntimeScope = settingKey[Boolean]("")
	lazy val dependencyCheckSkipProvidedScope = settingKey[Boolean]("")
	lazy val dependencyCheckSkipOptionalScope = settingKey[Boolean]("")
	lazy val dependencyCheckSuppressionFile = settingKey[Option[File]]("")
	// Analyzer configuration
	lazy val dependencyCheckArchiveAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckZipExtensions = settingKey[Option[String]]("")
	lazy val dependencyCheckJarAnalyzer = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckCentralAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckNexusAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckNexusUrl = settingKey[Option[URL]]("")
	lazy val dependencyCheckNexusUsesProxy = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckPyDistributionAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckPyPackageAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckRubygemsAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckOpensslAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckCmakeAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckAutoconfAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckComposerAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckNodeAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckNuspecAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckAssemblyAnalyzerEnabled = settingKey[Option[Boolean]]("")
	lazy val dependencyCheckPathToMono = settingKey[Option[File]]("")

	// Advanced configuration
	lazy val dependencyCheckCveUrl12Modified = settingKey[Option[URL]]("")
	lazy val dependencyCheckCveUrl20Modified = settingKey[Option[URL]]("")
	lazy val dependencyCheckCveUrl12Base = settingKey[Option[String]]("")
	lazy val dependencyCheckCveUrl20Base = settingKey[Option[String]]("")
	lazy val dependencyCheckConnectionTimeout = settingKey[Option[Int]]("")
	lazy val dependencyCheckDataDirectory = settingKey[Option[File]]("")
	lazy val dependencyCheckDatabaseDriverName = settingKey[Option[String]]("")
	lazy val dependencyCheckDatabaseDriverPath = settingKey[Option[File]]("")
	lazy val dependencyCheckConnectionString = settingKey[Option[String]]("")
	lazy val dependencyCheckDatabaseUser = settingKey[Option[String]]("")
	lazy val dependencyCheckDatabasePassword = settingKey[Option[String]]("")
	lazy val dependencyCheckMetaFileName = settingKey[Option[String]]("")

	// TaskKeys
	lazy val dependencyCheckTask = TaskKey[Unit]("check", "Runs dependency-check against the project and generates a report.")
	lazy val dependencyCheckAggregate = TaskKey[Unit]("aggregate-check", "Runs dependency-check against the child projects and aggregates the results into a single report.")
	lazy val dependencyCheckUpdateOnly = TaskKey[Unit]("update-only", "Updates the local cache of the NVD data from NIST.")
	lazy val dependencyCheckPurge = TaskKey[Unit]("purge", "Deletes the local copy of the NVD. This is used to force a refresh of the data.")
}
