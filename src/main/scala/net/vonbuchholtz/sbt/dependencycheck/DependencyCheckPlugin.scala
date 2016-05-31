package net.vonbuchholtz.sbt.dependencycheck

import java.util

import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.data.nexus.MavenArtifact
import org.owasp.dependencycheck.data.nvdcve.{CveDB, DatabaseException, DatabaseProperties}
import org.owasp.dependencycheck.dependency.{Confidence, Dependency, Vulnerability}
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.Keys._
import sbt.{File, _}

import scala.collection.JavaConverters._


object DependencyCheckPlugin extends sbt.AutoPlugin {


  object autoImport extends DependencyCheckKeys

  import autoImport._

  private lazy val initSettingsTask = taskKey[Settings]("Initialize dependency check settings with project settings.")

  override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    dependencyCheckFormat := "all",
    dependencyCheckAutoUpdate := None,
    dependencyCheckCveValidForHours := None,
    dependencyCheckFailBuildOnCVSS := Some(11),
    dependencyCheckOutputDirectory := Some(crossTarget.value),
    dependencyCheckSkip := false,
    dependencyCheckSkipTestScope := true,
    dependencyCheckSkipRuntimeScope := false,
    dependencyCheckSkipProvidedScope := true,
    dependencyCheckSkipOptionalScope := true,
    dependencyCheckSuppressionFile := None,
    dependencyCheckArchiveAnalyzerEnabled := None,
    dependencyCheckZipExtensions := None,
    dependencyCheckJarAnalyzer := None,
    dependencyCheckCentralAnalyzerEnabled := None,
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
    dependencyCheckNuspecAnalyzerEnabled := None,
    dependencyCheckAssemblyAnalyzerEnabled := None,
    dependencyCheckPathToMono := None,
    dependencyCheckCveUrl12Modified := None,
    dependencyCheckCveUrl20Modified := None,
    dependencyCheckCveUrl12Base := None,
    dependencyCheckCveUrl20Base := None,
    dependencyCheckConnectionTimeout := None,
    dependencyCheckDataDirectory := None,
    dependencyCheckDatabaseDriverName := None,
    dependencyCheckDatabaseDriverPath := None,
    dependencyCheckConnectionString := None,
    dependencyCheckDatabaseUser := None,
    dependencyCheckDatabasePassword := None,
    dependencyCheckMetaFileName := Some("dependency-check.ser"),
    dependencyCheckTask := checkTask.value,
    dependencyCheckAggregate := aggregateTask.value,
    dependencyCheckUpdateOnly := updateTask().value,
    dependencyCheckPurge := purgeTask.value,
    aggregate in dependencyCheckAggregate := false,
    aggregate in dependencyCheckUpdateOnly := false,
    aggregate in dependencyCheckPurge := false,
    initSettingsTask := initializeSettings.value
  )

  lazy val initializeSettings: Def.Initialize[Task[Settings]] = Def.task {
    val log: Logger = streams.value.log
    Settings.initialize()

    log.info("Applying project settings to DependencyCheck settings")

    setBooleanSetting(AUTO_UPDATE, dependencyCheckAutoUpdate.value)
    setIntSetting(CVE_CHECK_VALID_FOR_HOURS, dependencyCheckCveValidForHours.value)

    Settings.setStringIfNotEmpty(APPLICATION_VAME, name.value)

    setFileSetting(SUPPRESSION_FILE, dependencyCheckSuppressionFile.value)

    // Analyzer Configuration
    setBooleanSetting(ANALYZER_ARCHIVE_ENABLED, dependencyCheckArchiveAnalyzerEnabled.value)
    setStringSetting(ADDITIONAL_ZIP_EXTENSIONS, dependencyCheckZipExtensions.value)
    setBooleanSetting(ANALYZER_JAR_ENABLED, dependencyCheckJarAnalyzer.value)
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
    setBooleanSetting(ANALYZER_NUSPEC_ENABLED, dependencyCheckNuspecAnalyzerEnabled.value)
    setBooleanSetting(ANALYZER_ASSEMBLY_ENABLED, dependencyCheckAssemblyAnalyzerEnabled.value)
    setFileSetting(ANALYZER_ASSEMBLY_MONO_PATH, dependencyCheckPathToMono.value)
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
    // TODO used for writeDataFile in Maven aggregate to keep data between child scans
    dependencyCheckMetaFileName
    // TODO jvm/sbt proxy settings
    Settings.getInstance()
  }

  private def setBooleanSetting(key: String, b: Option[Boolean]) = {
    Settings.setBooleanIfNotNull(key, b.map(b => b: java.lang.Boolean).orNull)
  }

  private def setIntSetting(key: String, i: Option[Int]) = {
    Settings.setIntIfNotNull(key, i.map(i => i: java.lang.Integer).orNull)
  }

  private def setStringSetting(key: String, s: Option[String]) = {
    Settings.setStringIfNotEmpty(key, s.orNull)
  }

  private def setFileSetting(key: String, file: Option[File]) = {
    Settings.setStringIfNotEmpty(key, file match { case Some(f) => f.getAbsolutePath case None => null })
  }

  private def setUrlSetting(key: String, url: Option[URL]) = {
    Settings.setStringIfNotEmpty(key, url match { case Some(u) => u.toExternalForm case None => null })
  }

  def failBuildOnCVSS(dependencies: util.List[Dependency], cvssScore: Float): Boolean = dependencies.asScala.exists(p => {
    p.getVulnerabilities.asInstanceOf[java.util.Set[Vulnerability]].asScala.exists(v => {
      v.getCvssScore >= cvssScore
    })
  })

  def checkTask = Def.task {
    val log: Logger = streams.value.log
    val settings: Settings = initSettingsTask.value
    // working around threadlocal issue with DependencyCheck's Settings and sbt task dependency system.
    Settings.setInstance(settings)

    val engine: Engine = new Engine(classOf[Engine].getClassLoader)

    var checkClasspath = Set[Attributed[File]]()
    if (!dependencyCheckSkip.value) {
      checkClasspath ++= logAddDependencies((dependencyClasspath in Compile).value, Compile, log)

      if (!dependencyCheckSkipRuntimeScope.value) {
        checkClasspath ++= logAddDependencies((dependencyClasspath in Runtime).value, Runtime, log)
      }
      if (!dependencyCheckSkipTestScope.value) {
        checkClasspath ++= logAddDependencies((dependencyClasspath in Test).value, Test, log)
      }
      if (dependencyCheckSkipProvidedScope.value) {
        checkClasspath --= logRemoveDependencies(Classpaths.managedJars(Provided, classpathTypes.value, update.value), Provided, log)
      }
      if (dependencyCheckSkipOptionalScope.value) {
        checkClasspath --= logRemoveDependencies(Classpaths.managedJars(Optional, classpathTypes.value, update.value), Optional, log)
      }

      addDependencies(checkClasspath, engine, log)

      engine.analyzeDependencies()
      writeReports(engine, dependencyCheckOutputDirectory.value.getOrElse(crossTarget.value), dependencyCheckFormat.value, log)
    }
    else {
      log.info("Skipping dependency check.")
    }

    engine.cleanup()
    Settings.cleanup()

    val cvssScore: Float = dependencyCheckFailBuildOnCVSS.value.getOrElse(11)
    if (failBuildOnCVSS(engine.getDependencies, cvssScore)) {
      throw new IllegalStateException(s"Vulnerability with CVSS score higher $cvssScore found. Failing build.")
    }
  }

  def updateTask() = Def.task {
    val log: Logger = streams.value.log
    val settings: Settings = initSettingsTask.value
    log.info(s"Running update-only for ${name.value}")

    DependencyCheckUpdateTask.update(settings, log)
  }

  def purgeTask = Def.task {
    val log: Logger = streams.value.log
    val settings: Settings = initSettingsTask.value
    log.info(s"Running purge for ${name.value}")

    DependencyCheckPurgeTask.purge(dependencyCheckConnectionString.value, settings, log)
  }

  def aggregateTask = Def.task {
    val log: Logger = streams.value.log
    val settings: Settings = initSettingsTask.value
    log.info(s"Running aggregate-check for ${name.value}")

    DependencyCheckAggregateTask.aggregate(settings, log)
  }


  def addDependencies(checkClasspath: Set[Attributed[File]], engine: Engine, log: Logger): Unit = {
    checkClasspath.foreach(
      attributed =>
        attributed.get(Keys.moduleID.key) match {
          case Some(moduleId) =>
            // TODO excludes
            log.info(s"Scanning ${moduleId.name} ${moduleId.configurations}")
            if (attributed.data != null) {
              val dependencies = engine.scan {
                new File(attributed.data.getAbsolutePath)
              }
              if (dependencies != null && !dependencies.isEmpty) {
                val dependency: Dependency = dependencies.get(0)
                addEvidence(moduleId, dependency)
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

  def addEvidence(moduleId: ModuleID, dependency: Dependency): Unit = {
    val artifact: MavenArtifact = new MavenArtifact(moduleId.organization, moduleId.name, moduleId.revision)
    dependency.addAsEvidence("sbt", artifact, Confidence.HIGHEST)
    moduleId.configurations match {
      case Some(configurations) =>
        dependency.getVendorEvidence.addEvidence("sbt", "configuration", configurations, Confidence.HIGHEST)
      case None =>
    }
  }

  def writeReports(engine: Engine, outputDir: File, format: String, log: Logger): Unit = {
    log.info(s"Writing reports to ${outputDir.absolutePath}")
    var prop: DatabaseProperties = null
    var cve: CveDB = null
    try {
      cve = new CveDB()
      cve.open()
      prop = cve.getDatabaseProperties
    } catch {
      case ex: DatabaseException =>
        log.error(s"Error opening CVE Database: ${ex.getLocalizedMessage}")
        throw ex
    } finally {
      if (cve != null) {
        cve.close()
      }
    }
    val r: ReportGenerator = new ReportGenerator(Settings.getString(APPLICATION_VAME), engine.getDependencies, engine.getAnalyzers, prop)
    try {
      r.generateReports(outputDir.getAbsolutePath, format)
    } catch {
      case ex: Exception =>
        log.error(s"Error generating report: ${ex.getLocalizedMessage}")
        throw ex
    }
  }

}