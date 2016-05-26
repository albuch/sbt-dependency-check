package net.vonbuchholtz.dependencychecksbt

import java.io.{File, IOException}

import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.data.nexus.MavenArtifact
import org.owasp.dependencycheck.data.nvdcve.{CveDB, DatabaseException, DatabaseProperties}
import org.owasp.dependencycheck.dependency.{Confidence, Dependency}
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.Keys._
import sbt._

object DependencyCheckPlugin extends AutoPlugin {


	object autoImport extends DependencyCheckKeys
	import autoImport._

	private lazy val initSettingsTask = taskKey[Settings]("Initialize dependency check settings with project settings.")
	private lazy val writeReportTask = taskKey[Settings]("Write the reports.")

	override def trigger = allRequirements

	override lazy val projectSettings = Seq(
		dependencyCheckFormat := "all",
		dependencyCheckAutoUpdate := None,
		dependencyCheckCveValidForHours := None,
		dependencyCheckFailBuildOnCVSS := None,
		dependencyCheckOutputDirectory := None,
		dependencyCheckSkip := None,
		dependencyCheckSkipTestScope := None,
		dependencyCheckSkipRuntimeScope := None,
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
		dependencyCheckMetaFileName := None,
		dependencyCheckTask := scanDependencies(Compile).value,
		initSettingsTask := initializeSettings.value,
		commands += dependencyCheckCommand
	)

	lazy val initializeSettings: Def.Initialize[Task[Settings]] = Def.task {
		val log: Logger = streams.value.log
		Settings.initialize()

		log.info("Applying project settings to DependencyCheck settings")
		setBooleanSetting(AUTO_UPDATE, dependencyCheckAutoUpdate.value)
		setIntSetting(CVE_CHECK_VALID_FOR_HOURS, dependencyCheckCveValidForHours.value)

		// TODO:put this at end of check
		dependencyCheckFailBuildOnCVSS

		Settings.setStringIfNotEmpty(APPLICATION_VAME, name.value)

		// TODO: Move this to report gen
		// setFileSetting("?", dependencyCheckOutputDirectory)

		// TODO: put this at start of check
		dependencyCheckSkip
		dependencyCheckSkipTestScope
		dependencyCheckSkipRuntimeScope

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
		// TODO used for writeDataFile in Maven check
		dependencyCheckMetaFileName
		Settings.getInstance()
	}

	private def setBooleanSetting(key: String, b: Option[Boolean]): Unit = {
		Settings.setBooleanIfNotNull(key, b.map(b => b: java.lang.Boolean).orNull)
	}

	private def setIntSetting(key: String, i: Option[Int]): Unit = {
		Settings.setIntIfNotNull(key, i.map(i => i: java.lang.Integer).orNull)
	}

	private def setStringSetting(key: String, s: Option[String]): Unit = {
		Settings.setStringIfNotEmpty(key, s.orNull)
	}

	private def setFileSetting(key: String, file: Option[File]): Unit = {
		Settings.setStringIfNotEmpty(key, file match {case Some(f) => f.getAbsolutePath case None => null})
	}

	private def setUrlSetting(key: String, url: Option[URL]): Unit = {
		Settings.setStringIfNotEmpty(key, url match{ case Some(u) => u.toExternalForm case None => null})
	}

	def scanDependencies(conf: Configuration) = Def.task {
		val settings: Settings = initSettingsTask.value
		// working around threadlocal issue with DependencyCheck's Settings and sbt task system.
		Settings.setInstance(settings)

		val engine: Engine = new Engine(classOf[Engine].getClassLoader)
		val report = (dependencyClasspath in conf).value.foreach(

			attributed =>
				attributed.get(Keys.moduleID.key) match {
					case Some(moduleId) =>
						// TODO excludes
						val dependencies = engine.scan {
							new File(attributed.data.getAbsolutePath)
						}
						if (!dependencies.isEmpty) {
							val artifact: MavenArtifact = new MavenArtifact(moduleId.organization, moduleId.name, moduleId.revision)
							val dependency: Dependency = dependencies.get(0)
							dependency.addAsEvidence("sbt", artifact, Confidence.HIGHEST)
							moduleId.configurations match {
								case Some(configurations) =>
									dependency.getVendorEvidence.addEvidence("sbt", "configuration", configurations, Confidence.HIGHEST)
								case None =>
							}
						}

					case None =>
						// unmanaged JAR, just scan the file
						engine.scan {
							new File(attributed.data.getAbsolutePath)
						}
				}
		)
		engine.analyzeDependencies()
		writeReports(engine, new File("."), dependencyCheckFormat.value)
		engine.cleanup()
		Settings.cleanup()
	}

	def writeReports(engine: Engine, outputDir: File, format: String): Unit = {
		// val log: Logger = streams.value.log
		// log.info(s"Writing reports to ${outputDir.absolutePath}")
		var prop: DatabaseProperties = null
		var cve: CveDB = null
		try {
			cve = new CveDB()
			cve.open()
			prop = cve.getDatabaseProperties
		} catch {
			case ex: DatabaseException =>
		} finally {
			if (cve != null) {
				cve.close()
			}
		}
		val r: ReportGenerator = new ReportGenerator("", engine.getDependencies, engine.getAnalyzers, prop)
		try {
			r.generateReports(outputDir.getAbsolutePath, format)
		} catch {
			case ioe: IOException =>
			case ex: Throwable =>
		}
	}

	lazy val dependencyCheckCommand =
		Command.command("dc") { (state: State) =>
			println("Hi PHASE!")
			state.log.info("test!")
			state
		}

}