package net.vonbuchholtz.sbt.dependencycheck

import java.io.File

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.Logger

object DependencyCheckListSettingsTask {
  def logSettings(settings: Settings, failBuildOnCVSS: Float, format: String, outputDirectory: String, scanSet: Seq[sbt.File],
                  skip: Boolean, skipRuntime: Boolean, skipTest: Boolean, skipProvided: Boolean, skipOptional: Boolean,
                  useSbtModuleIdAsGav: Boolean, log: Logger): Unit = {
    def logBooleanSetting(key: String, setting: String, log: Logger): Unit = {
      log.info(s"\t$setting: ${settings.getBoolean(key)}")
    }

    def logStringSetting(key: String, setting: String, log: Logger): Unit = {

      log.info(s"\t$setting: ${if(key.contains("assword")) "******" else settings.getString(key)}")
    }

    def logFileSetting(key: String, setting: String, log: Logger): Unit = {
      val someFile: Option[File] = Option(settings.getFile(key))
      log.info(s"\t$setting: ${if(someFile.isDefined) someFile.get.getPath else ""}")
    }

    def logUrlSetting(key: String, setting: String, log: Logger): Unit = {
      log.info(s"\t$setting: ${settings.getString(key)}")
    }

    logBooleanSetting(AUTO_UPDATE, "dependencyCheckAutoUpdate", log)
    logStringSetting(CVE_CHECK_VALID_FOR_HOURS, "dependencyCheckCveValidForHours", log)
    log.info(s"\tdependencyCheckFailBuildOnCVSS: ${failBuildOnCVSS.toString}")
    log.info(s"\tdependencyCheckFormat: $format")
    log.info(s"\tdependencyCheckOutputDirectory: $outputDirectory")
    log.info(s"\tdependencyCheckScanSet: ${scanSet.map(f => f.getAbsolutePath).mkString(", ")}")
    log.info(s"\tdependencyCheckSkip: ${skip.toString}")
    log.info(s"\tdependencyCheckSkipTestScope: ${skipTest.toString}")
    log.info(s"\tdependencyCheckSkipRuntimeScope: ${skipRuntime.toString}")
    log.info(s"\tdependencyCheckSkipProvidedScope: ${skipProvided.toString}")
    log.info(s"\tdependencyCheckSkipOptionalScope: ${skipOptional.toString}")
    logFileSetting(SUPPRESSION_FILE, "dependencyCheckSuppressionFile/s", log)
    logFileSetting(HINTS_FILE, "dependencyCheckHintsFile", log)
    logStringSetting(ANALYSIS_TIMEOUT, "dependencyCheckAnalysisTimeout", log)
    logBooleanSetting(ANALYZER_EXPERIMENTAL_ENABLED, "dependencyCheckEnableExperimental", log)
    logBooleanSetting(ANALYZER_RETIRED_ENABLED, "dependencyCheckEnableRetired", log)

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
    logBooleanSetting(ANALYZER_NSP_PACKAGE_ENABLED, "dependencyCheckNSPAnalyzerEnabled", log)
    logUrlSetting(ANALYZER_NSP_URL, "dependencyCheckNSPAnalyzerUrl", log)
    logBooleanSetting(ANALYZER_NUSPEC_ENABLED, "dependencyCheckNuspecAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_NUGETCONF_ENABLED, "dependencyCheckNugetConfAnalyzerEnabled", log)
    logBooleanSetting(ANALYZER_COCOAPODS_ENABLED, "dependencyCheckCocoapodsEnabled", log)
    logBooleanSetting(ANALYZER_SWIFT_PACKAGE_MANAGER_ENABLED, "dependencyCheckSwiftEnabled", log)
    logBooleanSetting(ANALYZER_BUNDLE_AUDIT_ENABLED, "dependencyCheckBundleAuditEnabled", log)
    logFileSetting(ANALYZER_BUNDLE_AUDIT_PATH, "dependencyCheckPathToBundleAudit", log)
    logBooleanSetting(ANALYZER_ASSEMBLY_ENABLED, "dependencyCheckAssemblyAnalyzerEnabled", log)
    logFileSetting(ANALYZER_ASSEMBLY_MONO_PATH, "dependencyCheckPathToMono", log)
    logStringSetting(CVE_CPE_STARTS_WITH_FILTER, "dependencyCheckCpeStartsWith", log)
    logStringSetting(ANALYZER_RETIREJS_ENABLED, "dependencyCheckRetireJSAnalyzerEnabled", log)
    logUrlSetting(ANALYZER_RETIREJS_REPO_JS_URL, "dependencyCheckRetireJSAnalyzerRepoJSUrl", log)
    logStringSetting(ANALYZER_RETIREJS_REPO_VALID_FOR_HOURS, "dependencyCheckRetireJsAnalyzerRepoValidFor", log)
    logStringSetting(ANALYZER_RETIREJS_FILTERS, "dependencyCheckRetireJsAnalyzerFilters", log)
    logBooleanSetting(ANALYZER_RETIREJS_FILTER_NON_VULNERABLE, "dependencyCheckRetireJsAnalyzerFilterNonVulnerable", log)
    logBooleanSetting(ANALYZER_ARTIFACTORY_ENABLED, "dependencyCheckArtifactoryAnalyzerEnabled", log)
    logUrlSetting(ANALYZER_ARTIFACTORY_URL, "dependencyCheckArtifactoryAnalyzerUrl", log)
    logBooleanSetting(ANALYZER_ARTIFACTORY_USES_PROXY, "dependencyCheckArtifactoryAnalyzerUseProxy", log)
    logBooleanSetting(ANALYZER_ARTIFACTORY_PARALLEL_ANALYSIS, "dependencyCheckArtifactoryAnalyzerParallelAnalysis", log)
    logStringSetting(ANALYZER_ARTIFACTORY_API_USERNAME, "dependencyCheckArtifactoryAnalyzerUsername", log)
    logStringSetting(ANALYZER_ARTIFACTORY_API_TOKEN, "dependencyCheckArtifactoryAnalyzerApiToken", log)
    logStringSetting(ANALYZER_ARTIFACTORY_BEARER_TOKEN, "dependencyCheckArtifactoryAnalyzerBearerToken", log)

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
    log.info(s"\tdependencyCheckUseSbtModuleIdAsGav: ${useSbtModuleIdAsGav.toString}")
  }
}
