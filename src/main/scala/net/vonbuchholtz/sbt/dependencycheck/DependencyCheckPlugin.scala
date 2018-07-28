package net.vonbuchholtz.sbt.dependencycheck

import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.data.nexus.MavenArtifact
import org.owasp.dependencycheck.dependency.{Confidence, Dependency, EvidenceType, Vulnerability}
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{Def, File, ScopeFilter, _}

import scala.collection.JavaConverters._

object DependencyCheckPlugin extends sbt.AutoPlugin {

  object autoImport extends DependencyCheckKeys

  import autoImport._

  override def requires = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings = Seq(
    dependencyCheckFormat := "html",
    dependencyCheckAutoUpdate := None,
    dependencyCheckCveValidForHours := None,
    dependencyCheckFailBuildOnCVSS := 11,
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
    dependencyCheckNexusAnalyzerEnabled := None,
    dependencyCheckNexusUrl := None,
    dependencyCheckNexusUsesProxy := None,
    dependencyCheckPyDistributionAnalyzerEnabled := None,
    dependencyCheckPyPackageAnalyzerEnabled := None,
    dependencyCheckRubygemsAnalyzerEnabled := None,
    dependencyCheckOpensslAnalyzerEnabled := None,
    dependencyCheckCmakeAnalyzerEnabled := None,
    dependencyCheckAutoconfAnalyzerEnabled := None,
    dependencyCheckComposerAnalyzerEnabled := None,
    dependencyCheckNodeAnalyzerEnabled := None,
    dependencyCheckNSPAnalyzerEnabled := None,
    dependencyCheckNSPAnalyzerUrl := None,
    dependencyCheckNuspecAnalyzerEnabled := None,
    dependencyCheckCocoapodsEnabled := None,
    dependencyCheckSwiftEnabled := None,
    dependencyCheckBundleAuditEnabled := None,
    dependencyCheckPathToBundleAudit := None,
    dependencyCheckAssemblyAnalyzerEnabled := None,
    dependencyCheckPathToMono := None,
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

    // Advanced configuration
    dependencyCheckCveUrl12Modified := None,
    dependencyCheckCveUrl20Modified := None,
    dependencyCheckCveUrl12Base := None,
    dependencyCheckCveUrl20Base := None,
    dependencyCheckConnectionTimeout := None,
    dependencyCheckDataDirectory := None,
    dependencyCheckDatabaseDriverName := None,
    dependencyCheckDatabaseDriverPath := None,
    dependencyCheckConnectionString := Some("jdbc:h2:file:%s;AUTOCOMMIT=ON;MV_STORE=FALSE;"),
    dependencyCheckDatabaseUser := None,
    dependencyCheckDatabasePassword := None,
    dependencyCheckMetaFileName := Some("dependency-check.ser"),
    dependencyCheckUseSbtModuleIdAsGav := None,
    dependencyCheck := checkTask.value,
    dependencyCheckAggregate := aggregateTask.value,
    dependencyCheckUpdateOnly := updateTask.value,
    dependencyCheckPurge := purgeTask.value,
    dependencyCheckListSettings := listSettingsTask.value,
    aggregate in dependencyCheckAggregate := false,
    aggregate in dependencyCheckUpdateOnly := false,
    aggregate in dependencyCheckPurge := false,
    aggregate in dependencyCheckListSettings := false,
    concurrentRestrictions in Global += Tags.exclusive(NonParallel)
  )

  private val NonParallel = Tags.Tag("NonParallel")

  private[this] lazy val initializeSettings: Def.Initialize[Task[Settings]] = Def.task {
    val settings = new Settings()

    def setBooleanSetting(key: String, b: Option[Boolean]) = {
      settings.setBooleanIfNotNull(key, b.map(b => b: java.lang.Boolean).orNull)
    }

    def setIntSetting(key: String, i: Option[Int]) = {
      settings.setIntIfNotNull(key, i.map(i => i: java.lang.Integer).orNull)
    }

    def setStringSetting(key: String, s: Option[String]) = {
      settings.setStringIfNotEmpty(key, s.orNull)
    }

    def setFileSetting(key: String, file: Option[File]) = {
      settings.setStringIfNotEmpty(key, file match { case Some(f) => f.getAbsolutePath case None => null })
    }

    def setFileSequenceSetting(key: String, files: Seq[File]) = {
      val filePaths: Seq[String] = files map { file => file.getAbsolutePath}
      settings.setArrayIfNotEmpty(key, filePaths.toArray)
    }

    def setUrlSetting(key: String, url: Option[URL]) = {
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
    setBooleanSetting(ANALYZER_NEXUS_ENABLED, dependencyCheckNexusAnalyzerEnabled.value)
    setUrlSetting(ANALYZER_NEXUS_URL, dependencyCheckNexusUrl.value)
    setBooleanSetting(ANALYZER_NEXUS_USES_PROXY, dependencyCheckNexusUsesProxy.value)
    setBooleanSetting(ANALYZER_PYTHON_DISTRIBUTION_ENABLED, dependencyCheckPyDistributionAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_PYTHON_PACKAGE_ENABLED, dependencyCheckPyPackageAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_RUBY_GEMSPEC_ENABLED, dependencyCheckRubygemsAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_OPENSSL_ENABLED, dependencyCheckOpensslAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_CMAKE_ENABLED, dependencyCheckCmakeAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_AUTOCONF_ENABLED, dependencyCheckAutoconfAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_COMPOSER_LOCK_ENABLED, dependencyCheckComposerAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_NODE_PACKAGE_ENABLED, dependencyCheckNodeAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_NSP_PACKAGE_ENABLED, dependencyCheckNSPAnalyzerEnabled.value)
    setUrlSetting(ANALYZER_NSP_URL, dependencyCheckNSPAnalyzerUrl.value)
    setBooleanSetting(ANALYZER_NUSPEC_ENABLED, dependencyCheckNuspecAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_ASSEMBLY_ENABLED, dependencyCheckAssemblyAnalyzerEnabled.value)
    setFileSetting(ANALYZER_ASSEMBLY_MONO_PATH, dependencyCheckPathToMono.value)
    setBooleanSetting(ANALYZER_COCOAPODS_ENABLED, dependencyCheckCocoapodsEnabled.value)
    setBooleanSetting(ANALYZER_SWIFT_PACKAGE_MANAGER_ENABLED, dependencyCheckSwiftEnabled.value)
    setBooleanSetting(ANALYZER_BUNDLE_AUDIT_ENABLED, dependencyCheckBundleAuditEnabled.value)
    setFileSetting(ANALYZER_BUNDLE_AUDIT_PATH, dependencyCheckPathToBundleAudit.value)
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
    setUrlSetting(CVE_MODIFIED_12_URL, dependencyCheckCveUrl12Modified.value)
    setUrlSetting(CVE_MODIFIED_20_URL, dependencyCheckCveUrl20Modified.value)
    setStringSetting(CVE_SCHEMA_1_2, dependencyCheckCveUrl12Base.value)
    setStringSetting(CVE_SCHEMA_2_0, dependencyCheckCveUrl20Base.value)
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
        val cvssScore: Float = dependencyCheckFailBuildOnCVSS.value
        val useSbtModuleIdAsGav: Boolean = dependencyCheckUseSbtModuleIdAsGav.value.getOrElse(false)

        var checkDependencies = Set[Attributed[File]]()
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

        val scanSet: Seq[File] = (dependencyCheckScanSet.value.map { _ ** "*" } reduceLeft( _ +++ _) filter {_.isFile}).get

        withEngine(initializeSettings.value) { engine =>
          try {
            createReport(engine, checkDependencies, scanSet, outputDir, reportFormat, useSbtModuleIdAsGav, log)
            determineTaskFailureStatus(cvssScore, engine)
          } catch {
            case e: Exception =>
              log.error(s"Failed creating report: ${e.getLocalizedMessage}")
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
    log.info(s"Running aggregate-check for ${name.value}")

    val outputDir: File = dependencyCheckOutputDirectory.value.getOrElse(crossTarget.value)
    val reportFormat: String = dependencyCheckFormat.value
    val cvssScore: Float = dependencyCheckFailBuildOnCVSS.value
    val useSbtModuleIdAsGav: Boolean = dependencyCheckUseSbtModuleIdAsGav.value.getOrElse(false)

    var aggregatedDependencies = Set[Attributed[File]]()
    val compileDependencies: Seq[(ProjectRef, Configuration, Seq[Attributed[File]])] = aggregateCompileTask.all(aggregateCompileFilter).value
    aggregatedDependencies = addClasspathDependencies(compileDependencies, aggregatedDependencies, log)
    val runtimeDependencies: Seq[(ProjectRef, Configuration, Seq[Attributed[File]])] = aggregateRuntimeTask.all(aggregateRuntimeFilter).value
    aggregatedDependencies = addClasspathDependencies(runtimeDependencies, aggregatedDependencies, log)
    val testDependencies: Seq[(ProjectRef, Configuration, Seq[Attributed[File]])] = aggregateTestTask.all(aggregateTestFilter).value
    aggregatedDependencies = addClasspathDependencies(testDependencies, aggregatedDependencies, log)
    val providedDependencies: Seq[(ProjectRef, Configuration, Seq[Attributed[File]])] = aggregateProvidedTask.all(aggregateProvidedFilter).value
    aggregatedDependencies = removeClasspathDependencies(providedDependencies, aggregatedDependencies, log)
    val optionalDependencies: Seq[(ProjectRef, Configuration, Seq[Attributed[File]])] = aggregateOptionalTask.all(aggregateOptionalFilter).value
    aggregatedDependencies = removeClasspathDependencies(optionalDependencies, aggregatedDependencies, log)

    val scanSet: Seq[File] = (dependencyCheckScanSet.value.map { _ ** "*" } reduceLeft( _ +++ _) filter {_.isFile}).get
    withEngine(initializeSettings.value) { engine =>
      try {
        createReport(engine, aggregatedDependencies, scanSet, outputDir, reportFormat, useSbtModuleIdAsGav, log)
        determineTaskFailureStatus(cvssScore, engine)
      } catch {
        case e: Exception =>
          log.error(s"Failed creating report: ${e.getLocalizedMessage}")
          throw e
      }
    }
  }

  lazy val aggregateCompileFilter = ScopeFilter(inAnyProject, inConfigurations(Compile))
  lazy val aggregateRuntimeFilter = ScopeFilter(inAnyProject, inConfigurations(Runtime))
  lazy val aggregateTestFilter = ScopeFilter(inAnyProject, inConfigurations(Test))
  lazy val aggregateProvidedFilter = ScopeFilter(inAnyProject, inConfigurations(Provided))
  lazy val aggregateOptionalFilter = ScopeFilter(inAnyProject, inConfigurations(Optional))
  lazy val aggregateCompileTask: Def.Initialize[Task[(ProjectRef, Configuration, Seq[Attributed[File]])]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value)
      Def.task {(thisProjectRef.value, configuration.value, Seq.empty)}
    else
      Def.task {(thisProjectRef.value, configuration.value, (externalDependencyClasspath in configuration).value)}
  }
  lazy val aggregateRuntimeTask: Def.Initialize[Task[(ProjectRef, Configuration, Seq[Attributed[File]])]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value || (dependencyCheckSkipRuntimeScope ?? false).value)
      Def.task {(thisProjectRef.value, configuration.value, Seq.empty)}
    else
      Def.task {(thisProjectRef.value, configuration.value, (externalDependencyClasspath in configuration).value)}
  }
  lazy val aggregateTestTask: Def.Initialize[Task[(ProjectRef, Configuration, Seq[Attributed[File]])]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value || (dependencyCheckSkipTestScope ?? true).value)
      Def.task {(thisProjectRef.value, configuration.value, Seq.empty)}
    else
      Def.task {(thisProjectRef.value, configuration.value, (externalDependencyClasspath in configuration).value)}
  }
  lazy val aggregateProvidedTask: Def.Initialize[Task[(ProjectRef, Configuration, Seq[Attributed[File]])]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value || !(dependencyCheckSkipProvidedScope ?? false).value)
      Def.task {(thisProjectRef.value, configuration.value, Seq.empty) }
    else
      Def.task {(thisProjectRef.value, configuration.value, Classpaths.managedJars(configuration.value, classpathTypes.value, update.value))}
  }
  lazy val aggregateOptionalTask: Def.Initialize[Task[(ProjectRef, Configuration, Seq[Attributed[File]])]] = Def.taskDyn {
    if ((dependencyCheckSkip ?? false).value || !(dependencyCheckSkipOptionalScope ?? false).value)
      Def.task {(thisProjectRef.value, configuration.value, Seq.empty)}
    else
      Def.task {(thisProjectRef.value, configuration.value, Classpaths.managedJars(configuration.value, classpathTypes.value, update.value))}
  }

  def addClasspathDependencies(classpathToAdd: Seq[(ProjectRef, Configuration, Seq[Attributed[File]])], checkClasspath: Set[Attributed[File]], log: Logger): Set[Attributed[File]] = {
    var newClasspath = checkClasspath
    for ((projectRef, conf, classpath) <- classpathToAdd if classpath.nonEmpty) {
      log.debug(s"Adding ${conf.name} classpath for project ${projectRef.project}")
      classpath.foreach(f => log.debug(s"\t${f.data.getName}"))
      newClasspath ++= classpath
    }
    newClasspath
  }

  def removeClasspathDependencies(classpathToAdd: Seq[(ProjectRef, Configuration, Seq[Attributed[File]])], checkClasspath: Set[Attributed[File]], log: Logger): Set[Attributed[File]] = {
    var newClasspath = checkClasspath
    for ((projectRef, conf, classpath) <- classpathToAdd if classpath.nonEmpty) {
      log.debug(s"Removing ${conf.name} classpath for project ${projectRef.project}")
      classpath.foreach(f => log.info(s"\t${f.data.getName}"))
      newClasspath --= classpath
    }
    newClasspath
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
      DependencyCheckListSettingsTask.logSettings(engine.getSettings, dependencyCheckFailBuildOnCVSS.value, dependencyCheckFormat.value,
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
                if(dependency != null)
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
    log.debug(s"$action ${configuration.name} dependencies to check.")
    classpath.foreach(f => log.debug("\t" + f.data.getName))
    classpath
  }

  def addEvidence(moduleId: ModuleID, dependency: Dependency, useSbtModuleIdAsGav: Boolean): Unit = {
    val artifact: MavenArtifact = new MavenArtifact(moduleId.organization, moduleId.name, moduleId.revision)
    dependency.addAsEvidence("sbt", artifact, Confidence.HIGHEST)
    if (useSbtModuleIdAsGav) {
      // unfortunately, for an identifier to act as a GAV, it needs to have the source 'maven' (hardcoded in owasp d-c)
      dependency
        .addIdentifier("maven", String.format("%s:%s:%s", moduleId.organization, moduleId.name, moduleId.revision), null, Confidence.HIGH)
    }
    moduleId.configurations match {
      case Some(configurations) =>
        dependency.addEvidence(EvidenceType.VENDOR, "sbt", "configuration", configurations, Confidence.HIGHEST)
      case None =>
    }
  }

  def createReport(engine: Engine, checkClasspath: Set[Attributed[File]], scanSet: Seq[File], outputDir: File, reportFormat: String, useSbtModuleIdAsGav: Boolean, log: Logger): Unit = {
    addDependencies(checkClasspath, engine, useSbtModuleIdAsGav, log)
    scanSet.foreach(file => engine.scan(file))
    
    engine.analyzeDependencies()
    engine.writeReports(engine.getSettings.getString(APPLICATION_NAME), outputDir , reportFormat)
    //writeReports(outputDir, reportFormat, log)
  }

  def determineTaskFailureStatus(failCvssScore: Float, engine: Engine): Unit = {
    if (failBuildOnCVSS(engine.getDependencies, failCvssScore)) {
      throw new IllegalStateException(s"Vulnerability with CVSS score higher $failCvssScore found. Failing build.")
    }
  }

  def failBuildOnCVSS(dependencies: Array[Dependency], cvssScore: Float): Boolean = dependencies.exists(p => {
    p.getVulnerabilities.asInstanceOf[java.util.Set[Vulnerability]].asScala.exists(v => {
      v.getCvssScore >= cvssScore
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

}
