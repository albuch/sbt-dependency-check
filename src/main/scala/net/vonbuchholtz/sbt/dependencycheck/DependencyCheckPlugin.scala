package net.vonbuchholtz.sbt.dependencycheck

import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.agent.DependencyCheckScanAgent
import org.owasp.dependencycheck.data.nexus.MavenArtifact
import org.owasp.dependencycheck.dependency.naming.{GenericIdentifier, Identifier, PurlIdentifier}
import org.owasp.dependencycheck.dependency.{Confidence, Dependency, EvidenceType}
import org.owasp.dependencycheck.exception.ExceptionCollection
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{Def, File, ScopeFilter, _}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal
import java.io.{PrintWriter, StringWriter}

object DependencyCheckPlugin extends sbt.AutoPlugin {

  object autoImport extends DependencyCheckKeys

  import autoImport._

  override def requires = JvmPlugin

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings = Seq(
    dependencyCheckFormat := "HTML",
    dependencyCheckFormats := Seq(),
    dependencyCheckAutoUpdate := None,
    dependencyCheckCveValidForHours := None,
    dependencyCheckFailBuildOnCVSS := 11,
    dependencyCheckJUnitFailBuildOnCVSS := None,
    dependencyCheckOutputDirectory := Some(crossTarget.value),
    dependencyCheckScanSet := Seq(baseDirectory.value / "src/main/resources"),
    dependencyCheckSkip := false,
    dependencyCheckSkipTestScope := true,
    dependencyCheckSkipRuntimeScope := false,
    dependencyCheckSkipProvidedScope := false,
    dependencyCheckSkipOptionalScope := false,
    dependencyCheckSuppressionFile := None,
    dependencyCheckSuppressionFiles := Seq(),
    dependencyCheckCpeStartsWith := None,
    dependencyCheckHintsFile := None,
    dependencyCheckAnalysisTimeout := None,
    dependencyCheckEnableExperimental := None,
    dependencyCheckEnableRetired := None,

    // Analyzer configuration
    dependencyCheckArchiveAnalyzerEnabled := None,
    dependencyCheckZipExtensions := None,
    dependencyCheckJarAnalyzerEnabled := None,
    dependencyCheckCentralAnalyzerEnabled := Some(false),
    dependencyCheckCentralAnalyzerUseCache := None,
    dependencyCheckOSSIndexAnalyzerEnabled := None,
    dependencyCheckOSSIndexAnalyzerUrl := None,
    dependencyCheckOSSIndexAnalyzerUseCache := None,
    dependencyCheckOSSIndexAnalyzerUsername := None,
    dependencyCheckOSSIndexAnalyzerPassword := None,
    dependencyCheckNexusAnalyzerEnabled := None,
    dependencyCheckNexusUrl := None,
    dependencyCheckNexusUsesProxy := None,
    dependencyCheckNexusUser := None,
    dependencyCheckNexusPassword := None,
    dependencyCheckPyDistributionAnalyzerEnabled := None,
    dependencyCheckPyPackageAnalyzerEnabled := None,
    dependencyCheckRubygemsAnalyzerEnabled := None,
    dependencyCheckOpensslAnalyzerEnabled := None,
    dependencyCheckCmakeAnalyzerEnabled := None,
    dependencyCheckAutoconfAnalyzerEnabled := None,
    dependencyCheckComposerAnalyzerEnabled := None,
    dependencyCheckNodeAnalyzerEnabled := None,
    dependencyCheckNodeAuditAnalyzerEnabled := None,
    dependencyCheckNodeAuditAnalyzerUrl := None,
    dependencyCheckNodeAuditAnalyzerUseCache := None,
    dependencyCheckNuspecAnalyzerEnabled := None,
    dependencyCheckNugetConfAnalyzerEnabled := None,
    dependencyCheckCocoapodsEnabled := None,
    dependencyCheckSwiftEnabled := None,
    dependencyCheckBundleAuditEnabled := None,
    dependencyCheckPathToBundleAudit := None,
    dependencyCheckBundleAuditWorkingDirectory := None,
    dependencyCheckAssemblyAnalyzerEnabled := None,
    dependencyCheckPathToDotNETCore := None,
    dependencyCheckRetireJSAnalyzerEnabled := None,
    dependencyCheckRetireJSAnalyzerRepoJSUrl := None,
    dependencyCheckRetireJsAnalyzerRepoValidFor := None,
    dependencyCheckRetireJsAnalyzerFilters := Seq(),
    dependencyCheckRetireJsAnalyzerFilterNonVulnerable := None,
    dependencyCheckArtifactoryAnalyzerEnabled := None,
    dependencyCheckArtifactoryAnalyzerUrl := None,
    dependencyCheckArtifactoryAnalyzerUseProxy := None,
    dependencyCheckArtifactoryAnalyzerParallelAnalysis := None,
    dependencyCheckArtifactoryAnalyzerUsername := None,
    dependencyCheckArtifactoryAnalyzerApiToken := None,
    dependencyCheckArtifactoryAnalyzerBearerToken := None,
    dependencyCheckGolangDepEnabled := None,
    dependencyCheckGolangModEnabled := None,
    dependencyCheckPathToGo := None,

    // Advanced configuration
    dependencyCheckCveUrlModified := None,
    dependencyCheckCveUrlBase := None,
    dependencyCheckConnectionTimeout := None,
    dependencyCheckDataDirectory := None,
    dependencyCheckDatabaseDriverName := None,
    dependencyCheckDatabaseDriverPath := None,
    dependencyCheckConnectionString := None,
    dependencyCheckDatabaseUser := None,
    dependencyCheckDatabasePassword := None,
    dependencyCheckMetaFileName := Some("dependency-check.ser"),
    dependencyCheckUseSbtModuleIdAsGav := None,
    dependencyCheck := checkTask.value,
    dependencyCheckAggregate := aggregateTask.value,
    dependencyCheckAnyProject := anyProjectTask.value,
    dependencyCheckUpdateOnly := updateTask.value,
    dependencyCheckPurge := purgeTask.value,
    dependencyCheckListSettings := listSettingsTask.value,
    aggregate in dependencyCheckAggregate := false,
    aggregate in dependencyCheckAnyProject := false,
    aggregate in dependencyCheckUpdateOnly := false,
    aggregate in dependencyCheckPurge := false,
    aggregate in dependencyCheckListSettings := false,
    concurrentRestrictions in Global += Tags.exclusive(NonParallel)
  )

  private val NonParallel = Tags.Tag("NonParallel")

  private[this] lazy val initializeSettings: Def.Initialize[Task[Settings]] = Def.task {
    val settings = new Settings()

    def setBooleanSetting(key: String, b: Option[Boolean]): Unit = {
      settings.setBooleanIfNotNull(key, b.map(b => b: java.lang.Boolean).orNull)
    }

    def setIntSetting(key: String, i: Option[Int]): Unit = {
      settings.setIntIfNotNull(key, i.map(i => i: java.lang.Integer).orNull)
    }

    def setFloatSetting(key: String, f: Option[Float]): Unit = {
      f.foreach(fl => settings.setFloat(key, fl))
    }

    def setStringSetting(key: String, s: Option[String]): Unit = {
      settings.setStringIfNotEmpty(key, s.orNull)
    }

    def setFileSetting(key: String, file: Option[File]): Unit = {
      settings.setStringIfNotEmpty(key, file match { case Some(f) => f.getAbsolutePath case None => null })
    }

    def setFileSequenceSetting(key: String, files: Seq[File]): Unit = {
      val filePaths: Seq[String] = files map { file => file.getAbsolutePath }
      settings.setArrayIfNotEmpty(key, filePaths.toArray)
    }

    def setUrlSetting(key: String, url: Option[URL]): Unit = {
      settings.setStringIfNotEmpty(key, url match { case Some(u) => u.toExternalForm case None => null })
    }

    def initProxySettings(): Unit = {
      val httpsProxyHost = sys.props.get("https.proxyHost")
      val httpsProxyPort = sys.props.get("https.proxyPort")
      if (httpsProxyHost.isDefined && httpsProxyPort.isDefined) {
        setStringSetting(PROXY_SERVER, httpsProxyHost)
        setIntSetting(PROXY_PORT, httpsProxyPort.map(_.toInt))
        setStringSetting(PROXY_USERNAME, sys.props.get("https.proxyUser"))
        setStringSetting(PROXY_PASSWORD, sys.props.get("https.proxyPassword"))
      } else {
        setStringSetting(PROXY_SERVER, sys.props.get("http.proxyHost"))
        setIntSetting(PROXY_PORT, sys.props.get("http.proxyPort").map(_.toInt))
        setStringSetting(PROXY_USERNAME, sys.props.get("http.proxyUser"))
        setStringSetting(PROXY_PASSWORD, sys.props.get("http.proxyPassword"))
      }
      setStringSetting(PROXY_NON_PROXY_HOSTS, sys.props.get("nonProxyHosts"))
    }

    val log: Logger = streams.value.log

    log.info("Applying project settings to DependencyCheck settings")

    setBooleanSetting(AUTO_UPDATE, dependencyCheckAutoUpdate.value)
    setIntSetting(CVE_CHECK_VALID_FOR_HOURS, dependencyCheckCveValidForHours.value)
    setFloatSetting(JUNIT_FAIL_ON_CVSS, dependencyCheckJUnitFailBuildOnCVSS.value)

    settings.setStringIfNotEmpty(APPLICATION_NAME, name.value)
    val suppressionFiles = dependencyCheckSuppressionFiles.value ++ Seq(dependencyCheckSuppressionFile.value).flatten
    setFileSequenceSetting(SUPPRESSION_FILE, suppressionFiles)
    setFileSetting(HINTS_FILE, dependencyCheckHintsFile.value)
    setIntSetting(ANALYSIS_TIMEOUT, dependencyCheckAnalysisTimeout.value)
    setBooleanSetting(ANALYZER_EXPERIMENTAL_ENABLED, dependencyCheckEnableExperimental.value)
    setBooleanSetting(ANALYZER_RETIRED_ENABLED, dependencyCheckEnableRetired.value)

    // Analyzer Configuration
    setBooleanSetting(ANALYZER_ARCHIVE_ENABLED, dependencyCheckArchiveAnalyzerEnabled.value)
    setStringSetting(ADDITIONAL_ZIP_EXTENSIONS, dependencyCheckZipExtensions.value)
    setBooleanSetting(ANALYZER_JAR_ENABLED, dependencyCheckJarAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_CENTRAL_ENABLED, dependencyCheckCentralAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_CENTRAL_USE_CACHE, dependencyCheckCentralAnalyzerUseCache.value)
    setBooleanSetting(ANALYZER_OSSINDEX_ENABLED, dependencyCheckOSSIndexAnalyzerEnabled.value)
    setUrlSetting(ANALYZER_OSSINDEX_URL, dependencyCheckOSSIndexAnalyzerUrl.value)
    setBooleanSetting(ANALYZER_OSSINDEX_USE_CACHE, dependencyCheckOSSIndexAnalyzerUseCache.value)
    setBooleanSetting(ANALYZER_NEXUS_ENABLED, dependencyCheckNexusAnalyzerEnabled.value)
    setUrlSetting(ANALYZER_NEXUS_URL, dependencyCheckNexusUrl.value)
    setStringSetting(ANALYZER_NEXUS_USER, dependencyCheckNexusUser.value)
    setStringSetting(ANALYZER_NEXUS_PASSWORD, dependencyCheckNexusPassword.value)
    setBooleanSetting(ANALYZER_NEXUS_USES_PROXY, dependencyCheckNexusUsesProxy.value)
    setBooleanSetting(ANALYZER_PYTHON_DISTRIBUTION_ENABLED, dependencyCheckPyDistributionAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_PYTHON_PACKAGE_ENABLED, dependencyCheckPyPackageAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_RUBY_GEMSPEC_ENABLED, dependencyCheckRubygemsAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_OPENSSL_ENABLED, dependencyCheckOpensslAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_CMAKE_ENABLED, dependencyCheckCmakeAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_AUTOCONF_ENABLED, dependencyCheckAutoconfAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_COMPOSER_LOCK_ENABLED, dependencyCheckComposerAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_NODE_PACKAGE_ENABLED, dependencyCheckNodeAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_NODE_AUDIT_ENABLED, dependencyCheckNodeAuditAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_NODE_AUDIT_USE_CACHE, dependencyCheckNodeAuditAnalyzerUseCache.value)
    setUrlSetting(ANALYZER_NODE_AUDIT_URL, dependencyCheckNodeAuditAnalyzerUrl.value)
    setBooleanSetting(ANALYZER_NUSPEC_ENABLED, dependencyCheckNuspecAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_NUGETCONF_ENABLED, dependencyCheckNugetConfAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_ASSEMBLY_ENABLED, dependencyCheckAssemblyAnalyzerEnabled.value)
    setFileSetting(ANALYZER_ASSEMBLY_DOTNET_PATH, dependencyCheckPathToDotNETCore.value)
    setBooleanSetting(ANALYZER_COCOAPODS_ENABLED, dependencyCheckCocoapodsEnabled.value)
    setBooleanSetting(ANALYZER_SWIFT_PACKAGE_MANAGER_ENABLED, dependencyCheckSwiftEnabled.value)
    setBooleanSetting(ANALYZER_BUNDLE_AUDIT_ENABLED, dependencyCheckBundleAuditEnabled.value)
    setFileSetting(ANALYZER_BUNDLE_AUDIT_PATH, dependencyCheckPathToBundleAudit.value)
    setFileSetting(ANALYZER_BUNDLE_AUDIT_WORKING_DIRECTORY, dependencyCheckBundleAuditWorkingDirectory.value)
    setBooleanSetting(ANALYZER_RETIREJS_ENABLED, dependencyCheckRetireJSAnalyzerEnabled.value)
    setUrlSetting(ANALYZER_RETIREJS_REPO_JS_URL, dependencyCheckRetireJSAnalyzerRepoJSUrl.value)
    setIntSetting(ANALYZER_RETIREJS_REPO_VALID_FOR_HOURS, dependencyCheckRetireJsAnalyzerRepoValidFor.value)
    settings.setArrayIfNotEmpty(ANALYZER_RETIREJS_FILTERS, dependencyCheckRetireJsAnalyzerFilters.value.toArray)
    setBooleanSetting(ANALYZER_RETIREJS_FILTER_NON_VULNERABLE, dependencyCheckRetireJsAnalyzerFilterNonVulnerable.value)
    setBooleanSetting(ANALYZER_ARTIFACTORY_ENABLED, dependencyCheckArtifactoryAnalyzerEnabled.value)
    setUrlSetting(ANALYZER_ARTIFACTORY_URL, dependencyCheckArtifactoryAnalyzerUrl.value)
    setBooleanSetting(ANALYZER_ARTIFACTORY_USES_PROXY, dependencyCheckArtifactoryAnalyzerUseProxy.value)
    setBooleanSetting(ANALYZER_ARTIFACTORY_PARALLEL_ANALYSIS, dependencyCheckArtifactoryAnalyzerParallelAnalysis.value)
    setStringSetting(ANALYZER_ARTIFACTORY_API_USERNAME, dependencyCheckArtifactoryAnalyzerUsername.value)
    setStringSetting(ANALYZER_ARTIFACTORY_API_TOKEN, dependencyCheckArtifactoryAnalyzerApiToken.value)
    setStringSetting(ANALYZER_ARTIFACTORY_BEARER_TOKEN, dependencyCheckArtifactoryAnalyzerBearerToken.value)

    // Advanced Configuration
    setUrlSetting(CVE_MODIFIED_JSON, dependencyCheckCveUrlModified.value)
    setStringSetting(CVE_BASE_JSON, dependencyCheckCveUrlBase.value)
    setIntSetting(CONNECTION_TIMEOUT, dependencyCheckConnectionTimeout.value)
    setFileSetting(DATA_DIRECTORY, dependencyCheckDataDirectory.value)
    setStringSetting(DB_DRIVER_NAME, dependencyCheckDatabaseDriverName.value)
    setFileSetting(DB_DRIVER_PATH, dependencyCheckDatabaseDriverPath.value)
    setStringSetting(DB_CONNECTION_STRING, dependencyCheckConnectionString.value)
    setStringSetting(DB_USER, dependencyCheckDatabaseUser.value)
    setStringSetting(DB_PASSWORD, dependencyCheckDatabasePassword.value)
    setStringSetting(CVE_CPE_STARTS_WITH_FILTER, dependencyCheckCpeStartsWith.value)

    initProxySettings()

    settings
  }

  def checkTask: Def.Initialize[Task[Unit]] = Def.taskDyn {
    val log: Logger = streams.value.log

    if (!dependencyCheckSkip.value) {
      Def.task {
        log.info(s"Running check for ${name.value}")

        val outputDir: File = dependencyCheckOutputDirectory.value.getOrElse(crossTarget.value)
        val reportFormat: String = dependencyCheckFormat.value
        val reportFormats: Seq[String] = dependencyCheckFormats.value
        val cvssScore: Float = dependencyCheckFailBuildOnCVSS.value
        val useSbtModuleIdAsGav: Boolean = dependencyCheckUseSbtModuleIdAsGav.value.getOrElse(false)

        val checkDependencies = scala.collection.mutable.Set[Attributed[File]]()
        checkDependencies ++= logAddDependencies((externalDependencyClasspath in Compile).value, Compile, log)

        val skipRuntimeScope = dependencyCheckSkipRuntimeScope.value
        val skipTestScope = dependencyCheckSkipTestScope.value
        val skipProvidedScope = dependencyCheckSkipProvidedScope.value
        val skipOptionalScope = dependencyCheckSkipOptionalScope.value

        val runtimeClasspath = (externalDependencyClasspath in Runtime).value
        val testClasspath = (externalDependencyClasspath in Test).value
        val classpathTypeValue = classpathTypes.value
        val updateValue = update.value

        if (!skipRuntimeScope) {
          checkDependencies ++= logAddDependencies(runtimeClasspath, Runtime, log)
        }
        if (!skipTestScope) {
          checkDependencies ++= logAddDependencies(testClasspath, Test, log)
        }
        if (skipProvidedScope) {
          checkDependencies --= logRemoveDependencies(Classpaths.managedJars(Provided, classpathTypeValue, updateValue), Provided, log)
        }
        if (skipOptionalScope) {
          checkDependencies --= logRemoveDependencies(Classpaths.managedJars(Optional, classpathTypeValue, updateValue), Optional, log)
        }

        val scanSet: Seq[File] = getScanSet.value

        withEngine(initializeSettings.value) { engine =>
          try {
            createReport(engine, checkDependencies.toSet, scanSet, outputDir, getFormats(Some(reportFormat), reportFormats), useSbtModuleIdAsGav, log)
            determineTaskFailureStatus(cvssScore, engine, name.value)
          } catch { case NonFatal(e) =>
            logFailure(log, e)
            throw e
          }
        }

      } tag NonParallel
    }
    else {
      Def.task {
        log.info(s"Skipping dependency check for ${name.value}")
      }
    }
  } tag NonParallel


  def aggregateTask: Def.Initialize[Task[Unit]] = Def.task {
    val log: Logger = streams.value.log
    log.info(s"Running aggregate check for ${name.value}")

    val outputDir: File = dependencyCheckOutputDirectory.value.getOrElse(crossTarget.value)
    val reportFormat: String = dependencyCheckFormat.value
    val reportFormats: Seq[String] = dependencyCheckFormats.value
    val cvssScore: Float = dependencyCheckFailBuildOnCVSS.value
    val useSbtModuleIdAsGav: Boolean = dependencyCheckUseSbtModuleIdAsGav.value.getOrElse(false)

    val dependencies = scala.collection.mutable.Set[Attributed[File]]()
    dependencies ++= logAddDependencies(aggregateCompileFilter.value.flatten, Compile, log)
    dependencies --= logRemoveDependencies(aggregateProvidedFilter.value.flatten, Provided, log)
    dependencies ++= logAddDependencies(aggregateRuntimeFilter.value.flatten, Runtime, log)
    dependencies ++= logAddDependencies(aggregateTestFilter.value.flatten, Test, log)
    dependencies --= logRemoveDependencies(aggregateOptionalFilter.value.flatten, Optional, log)

    log.info("Scanning following dependencies: ")
    dependencies.foreach(f => log.info("\t" + f.data.getName))

    val scanSet: Seq[File] = getScanSet.value

    withEngine(initializeSettings.value) { engine =>
      try {
        createReport(engine, dependencies.toSet, scanSet, outputDir, getFormats(Some(reportFormat), reportFormats), useSbtModuleIdAsGav, log)
        determineTaskFailureStatus(cvssScore, engine, name.value)
      } catch { case NonFatal(e) =>
        logFailure(log, e)
        throw e
      }
    }
  }

  def anyProjectTask: Def.Initialize[Task[Unit]] = Def.task {
    val log: Logger = streams.value.log
    log.info(s"Running anyProject check for ${name.value}")

    val outputDir: File = dependencyCheckOutputDirectory.value.getOrElse(crossTarget.value)
    val reportFormat: String = dependencyCheckFormat.value
    val reportFormats: Seq[String] = dependencyCheckFormats.value
    val cvssScore: Float = dependencyCheckFailBuildOnCVSS.value
    val useSbtModuleIdAsGav: Boolean = dependencyCheckUseSbtModuleIdAsGav.value.getOrElse(false)

    val dependencies = scala.collection.mutable.Set[Attributed[File]]()
    dependencies ++= logAddDependencies(anyCompileFilter.value.flatten, Compile, log)
    dependencies --= logRemoveDependencies(anyProvidedFilter.value.flatten, Provided, log)
    dependencies ++= logAddDependencies(anyRuntimeFilter.value.flatten, Runtime, log)
    dependencies ++= logAddDependencies(anyTestFilter.value.flatten, Test, log)
    dependencies --= logRemoveDependencies(anyOptionalFilter.value.flatten, Optional, log)

    log.info("Scanning following dependencies: ")
    dependencies.foreach(f => log.info("\t" + f.data.getName))

    val scanSet: Seq[File] = getScanSet.value

    withEngine(initializeSettings.value) { engine =>
      try {
        createReport(engine, dependencies.toSet, scanSet, outputDir, getFormats(Some(reportFormat), reportFormats), useSbtModuleIdAsGav, log)
        determineTaskFailureStatus(cvssScore, engine, name.value)
      } catch { case NonFatal(e) =>
        logFailure(log, e)
        throw e
      }
    }
  }

  def getScanSet: Def.Initialize[Task[Seq[File]]] = Def.task {
    (dependencyCheckScanSet.value.map {
      _ ** "*"
    } reduceLeft (_ +++ _) filter {
      _.isFile
    }).get
  }

  lazy val anyCompileFilter = Def.settingDyn {
    compileDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Compile)))
  }
  lazy val anyRuntimeFilter = Def.settingDyn {
    runtimeDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Runtime)))
  }
  lazy val anyTestFilter = Def.settingDyn {
    testDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Test)))
  }
  lazy val anyProvidedFilter = Def.settingDyn {
    providedDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Provided)))
  }
  lazy val anyOptionalFilter = Def.settingDyn {
    optionalDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Optional)))
  }
  lazy val aggregateCompileFilter = Def.settingDyn {
    compileDependenciesTask.all(ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Compile)))
  }
  lazy val aggregateRuntimeFilter = Def.settingDyn {
    runtimeDependenciesTask.all(ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Runtime)))
  }
  lazy val aggregateTestFilter = Def.settingDyn {
    testDependenciesTask.all(ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Test)))
  }
  lazy val aggregateProvidedFilter = Def.settingDyn {
    providedDependenciesTask.all(ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Provided)))
  }
  lazy val aggregateOptionalFilter = Def.settingDyn {
    optionalDependenciesTask.all(ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Optional)))
  }

  lazy val compileDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value)
      Def.task { Seq.empty }
    else
      Def.task {
        (externalDependencyClasspath in configuration).value
      }
  }
  lazy val runtimeDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value || (dependencyCheckSkipRuntimeScope ?? false).value)
      Def.task { Seq.empty }
    else
      Def.task {
        (externalDependencyClasspath in configuration).value
      }
  }
  lazy val testDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value || (dependencyCheckSkipTestScope ?? true).value)
      Def.task { Seq.empty }
    else
      Def.task {
        (externalDependencyClasspath in configuration).value
      }
  }
  lazy val providedDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value || !(dependencyCheckSkipProvidedScope ?? false).value)
      Def.task { Seq.empty }
    else
      Def.task {
        Classpaths.managedJars(configuration.value, classpathTypes.value, update.value)
      }
  }
  lazy val optionalDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value || !(dependencyCheckSkipOptionalScope ?? false).value)
      Def.task { Seq.empty }
    else
      Def.task {
        Classpaths.managedJars(configuration.value, classpathTypes.value, update.value)
      }
  }

  def updateTask: Def.Initialize[Task[Unit]] = Def.task {
    val log: Logger = streams.value.log
    log.info(s"Running update-only for ${name.value}")

    withEngine(initializeSettings.value) { engine =>
      DependencyCheckUpdateTask.update(engine, log)
    }
  }

  def purgeTask: Def.Initialize[Task[Unit]] = Def.task {
    val log: Logger = streams.value.log
    log.info(s"Running purge for ${name.value}")
    withEngine(initializeSettings.value) { engine =>
      DependencyCheckPurgeTask.purge(dependencyCheckConnectionString.value, engine.getSettings, log)
    }
  }

  def listSettingsTask: Def.Initialize[Task[Unit]] = Def.task {
    val log: Logger = streams.value.log
    log.info(s"Running list-settings for ${name.value}")

    withEngine(initializeSettings.value) { engine =>
      DependencyCheckListSettingsTask.logSettings(engine.getSettings, dependencyCheckFailBuildOnCVSS.value,
        getFormats(Some(dependencyCheckFormat.value), dependencyCheckFormats.value),
        dependencyCheckOutputDirectory.value.getOrElse(new File(".")).getPath, dependencyCheckScanSet.value, dependencyCheckSkip.value,
        dependencyCheckSkipRuntimeScope.value, dependencyCheckSkipTestScope.value, dependencyCheckSkipProvidedScope.value,
        dependencyCheckSkipOptionalScope.value, dependencyCheckUseSbtModuleIdAsGav.value.getOrElse(false), log)
    }
  }

  def addDependencies(checkClasspath: Set[Attributed[File]], engine: Engine, useSbtModuleIdAsGav: Boolean, log: Logger): Unit = {
    checkClasspath.foreach(
      attributed =>
        attributed.get(Keys.moduleID.key) match {
          case Some(moduleId) =>
            log.debug(s"Scanning ${moduleId.name} ${moduleId.revision}")
            if (attributed.data != null) {
              val dependencies = engine.scan {
                new File(attributed.data.getAbsolutePath)
              }
              if (dependencies != null && !dependencies.isEmpty) {
                val dependency: Dependency = dependencies.get(0)
                if (dependency != null)
                  addEvidence(moduleId, dependency, useSbtModuleIdAsGav)
              }
            }
          case None =>
            // unmanaged JAR, just scan the file
            engine.scan {
              new File(attributed.data.getAbsolutePath)
            }
        }
    )
  }

  def logAddDependencies(classpath: Seq[Attributed[File]], configuration: Configuration, log: Logger): Seq[Attributed[File]] = {
    logDependencies(log, classpath, configuration, "Adding")
  }

  def logRemoveDependencies(classpath: Seq[Attributed[File]], configuration: Configuration, log: Logger): Seq[Attributed[File]] = {
    logDependencies(log, classpath, configuration, "Removing")
  }

  def logDependencies(log: Logger, classpath: Seq[Attributed[File]], configuration: Configuration, action: String): Seq[Attributed[File]] = {
    log.info(s"$action ${configuration.name} dependencies to check.")
    classpath.foreach(f => log.info("\t" + f.data.getName))
    classpath
  }

  def addEvidence(moduleId: ModuleID, dependency: Dependency, useSbtModuleIdAsGav: Boolean): Unit = {
    val artifact: MavenArtifact = new MavenArtifact(moduleId.organization, moduleId.name, moduleId.revision)
    dependency.addAsEvidence("sbt", artifact, Confidence.HIGHEST)
    if (useSbtModuleIdAsGav) {
      val id = getIdentifier(artifact, moduleId)
      dependency.addSoftwareIdentifier(id)
    }
    moduleId.configurations match {
      case Some(configurations) =>
        dependency.addEvidence(EvidenceType.VENDOR, "sbt", "configuration", configurations, Confidence.HIGHEST)
      case None =>
    }
  }

  private def getIdentifier(artifact: MavenArtifact, moduleId: ModuleID): Identifier = {
    Try {
      new PurlIdentifier("sbt", artifact.getGroupId, artifact.getArtifactId, artifact.getVersion, Confidence.HIGHEST)
    } match {
      case Success(id) => id
      case Failure(_) => new GenericIdentifier(String.format("sbt:%s:%s:%s", moduleId.organization, moduleId.name, moduleId.revision), Confidence.HIGHEST)
    }
  }

  def createReport(engine: Engine, checkClasspath: Set[Attributed[File]], scanSet: Seq[File], outputDir: File, reportFormats: Seq[String], useSbtModuleIdAsGav: Boolean, log: Logger): Unit = {
    addDependencies(checkClasspath, engine, useSbtModuleIdAsGav, log)
    scanSet.foreach(file => engine.scan(file))

    engine.analyzeDependencies()
    reportFormats.foreach(reportFormat => engine.writeReports(engine.getSettings.getString(APPLICATION_NAME), outputDir, reportFormat, null))
  }

  def determineTaskFailureStatus(failCvssScore: Float, engine: Engine, name: String): Unit = {
    if (failBuildOnCVSS(engine.getDependencies, failCvssScore)) {
      DependencyCheckScanAgent.showSummary(name, engine.getDependencies)
      throw new VulnerabilityFoundException(s"Vulnerability with CVSS score higher $failCvssScore found. Failing build.")
    }
  }

  def failBuildOnCVSS(dependencies: Array[Dependency], cvssScore: Float): Boolean = dependencies.exists(p => {
    p.getVulnerabilities.asScala.exists(v => {
      (v.getCvssV2 != null && v.getCvssV2.getScore >= cvssScore) || (v.getCvssV3 != null && v.getCvssV3.getBaseScore >= cvssScore)
    })
  })

  private[this] def withEngine(settings: Settings)(fn: Engine => Any): Unit = {
    val engine: Engine = new Engine(classOf[Engine].getClassLoader, settings)
    try {
      fn(engine)
      ()
    } finally {
      engine.close()
      engine.getSettings.cleanup(true)
    }
  }

  private[this] def getFormats(format: Option[String], formats: Seq[String]): Seq[String] = {
    val upperCaseFormats: Seq[String] = formats.map(f => f.toUpperCase)
    format.filter(_ => upperCaseFormats.isEmpty ).foldLeft(upperCaseFormats)(_ :+ _)
  }

  private def logFailure(log: Logger, ex: Throwable): Unit = ex match {
    case e: VulnerabilityFoundException =>
      log.error(s"${e.getLocalizedMessage}")
    case e: ExceptionCollection =>
      val prettyMessage = (
        "Failed creating report:" +:
          e.getExceptions.asScala.toVector.flatMap { t =>
            s"  ${t.getLocalizedMessage}" +:
              Option(t.getCause).map { cause =>
                s"  - ${cause.getLocalizedMessage}"
              }.toVector
          }
      ).mkString("\n")
      log.error(prettyMessage)

      // We have to log the full stacktraces here, since SBT doesn't use `printStackTrace`
      // when logging exceptions.
      // See https://github.com/albuch/sbt-dependency-check/issues/98
      e.getExceptions.asScala.foreach { _ =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw, true))
        log.error(sw.toString)
      }
    case e =>
      log.error(s"Failed creating report: ${e.getLocalizedMessage}")
  }
}
