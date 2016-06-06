package net.vonbuchholtz.sbt.dependencycheck

import java.io.File

import net.vonbuchholtz.sbt.dependencycheck.DependencyCheckPlugin.autoImport._
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.Keys._
import sbt.Logger

object DependencyCheckListSettingsTask {
  def logSettings(settings: Settings, failBuildOnCVSS: Float, format: String, outputDirectory: String, skip: Boolean,
                  skipRuntime: Boolean, skipTest: Boolean, skipProvided: Boolean, skipOptional: Boolean, log: Logger): Unit = {
    // working around threadlocal issue with DependencyCheck's Settings and sbt task dependency system.
    Settings.setInstance(settings)

    logBooleanSetting(AUTO_UPDATE, "dependencyCheckAutoUpdate", log)
    logStringSetting(CVE_CHECK_VALID_FOR_HOURS, "dependencyCheckCveValidForHours", log)
    log.info(s"\tdependencyCheckFailBuildOnCVSS: ${failBuildOnCVSS.toString}")
    log.info(s"\tdependencyCheckFormat: $format")
    log.info(s"\tdependencyCheckOutputDirectory: $outputDirectory")
    log.info(s"\tdependencyCheckSkip: ${skip.toString}")
    log.info(s"\tdependencyCheckSkipTestScope: ${skipTest.toString}")
    log.info(s"\tdependencyCheckSkipRuntimeScope: ${skipRuntime.toString}")
    log.info(s"\tdependencyCheckSkipProvidedScope: ${skipProvided.toString}")
    log.info(s"\tdependencyCheckSkipOptionalScope: ${skipOptional.toString}")
    logFileSetting(SUPPRESSION_FILE, "dependencyCheckSuppressionFile", log)

    // Analyzer Configuration
    logBooleanSetting(ANALYZER_ARCHIVE_ENABLED, "dependencyCheckArchiveAnalyzerEnabled", log)
    logStringSetting(ADDITIONAL_ZIP_EXTENSIONS, "dependencyCheckZipExtensions", log)
    logBooleanSetting(ANALYZER_JAR_ENABLED, "dependencyCheckJarAnalyzer", log)
    logBooleanSetting(ANALYZER_CENTRAL_ENABLED, "dependencyCheckCentralAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_NEXUS_ENABLED, "dependencyCheckNexusAnalyzerEnabled", log)
    logUrlSetting(ANALYZER_NEXUS_URL, "dependencyCheckNexusUrl", log)
    logBooleanSetting(ANALYZER_NEXUS_USES_PROXY, "dependencyCheckNexusUsesProxy", log)
    logBooleanSetting(ANALYZER_PYTHON_DISTRIBUTION_ENABLED, "dependencyCheckPyDistributionAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_PYTHON_PACKAGE_ENABLED, "dependencyCheckPyPackageAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_RUBY_GEMSPEC_ENABLED, "dependencyCheckRubygemsAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_OPENSSL_ENABLED, "dependencyCheckOpensslAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_CMAKE_ENABLED, "dependencyCheckCmakeAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_AUTOCONF_ENABLED, "dependencyCheckAutoconfAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_COMPOSER_LOCK_ENABLED, "dependencyCheckComposerAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_NODE_PACKAGE_ENABLED, "dependencyCheckNodeAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_NUSPEC_ENABLED, "dependencyCheckNuspecAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_ASSEMBLY_ENABLED, "dependencyCheckAssemblyAnalyzerEnabled", log)
    logFileSetting(ANALYZER_ASSEMBLY_MONO_PATH, "dependencyCheckPathToMono", log)
    // Advanced Configuration
    logUrlSetting(CVE_MODIFIED_12_URL, "dependencyCheckCveUrl12Modified", log)
    logUrlSetting(CVE_MODIFIED_20_URL, "dependencyCheckCveUrl20Modified", log)
    logStringSetting(CVE_SCHEMA_1_2, "dependencyCheckCveUrl12Base", log)
    logStringSetting(CVE_SCHEMA_2_0, "dependencyCheckCveUrl20Base", log)
    logStringSetting(CONNECTION_TIMEOUT, "dependencyCheckConnectionTimeout", log)
    logFileSetting(DATA_DIRECTORY, "dependencyCheckDataDirectory", log)
    logStringSetting(DB_DRIVER_NAME, "dependencyCheckDatabaseDriverName", log)
    logFileSetting(DB_DRIVER_PATH, "dependencyCheckDatabaseDriverPath", log)
    logStringSetting(DB_CONNECTION_STRING, "dependencyCheckConnectionString", log)
    logStringSetting(DB_USER, "dependencyCheckDatabaseUser", log)
    logStringSetting(DB_PASSWORD, "dependencyCheckDatabasePassword", log)
  }

  def logBooleanSetting(key: String, setting: String, log: Logger): Unit = {
    log.info(s"\t$setting: ${Settings.getBoolean(key)}")
  }

  def logStringSetting(key: String, setting: String, log: Logger): Unit = {

    log.info(s"\t$setting: ${if(key.contains("assword")) "******" else Settings.getString(key)}")
  }

  def logFileSetting(key: String, setting: String, log: Logger): Unit = {
    val someFile: Option[File] = Option(Settings.getFile(key))
    log.info(s"\t$setting: ${if(someFile.isDefined) someFile.get.getPath else ""}")
  }

  def logUrlSetting(key: String, setting: String, log: Logger): Unit = {
    log.info(s"\t$setting: ${Settings.getString(key)}")
  }
}
