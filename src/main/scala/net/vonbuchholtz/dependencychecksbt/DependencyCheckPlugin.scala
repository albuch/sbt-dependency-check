package net.vonbuchholtz.dependencychecksbt

import java.io.{File, IOException}

import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.data.nexus.MavenArtifact
import org.owasp.dependencycheck.data.nvdcve.{CveDB, DatabaseException, DatabaseProperties}
import org.owasp.dependencycheck.dependency.{Confidence, Dependency}
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.owasp.dependencycheck.utils.Settings
import sbt.Keys._
import sbt._

object DependencyCheckPlugin extends AutoPlugin {

	object autoImport {
		lazy val checkDependencies = TaskKey[Unit]("scan")
	}

	import autoImport._

	override lazy val projectSettings = Seq(
		checkDependencies <<= scanDependencies(Compile),
		commands += dependencyCheckCommand
	)

	def initializeSettings(): Unit = {
		Settings.initialize()
		Settings.setBoolean(Settings.KEYS.AUTO_UPDATE, true)
		Settings.setBoolean(Settings.KEYS.ANALYZER_JAR_ENABLED, true)
		//NUSPEC ANALYZER
		Settings.setBoolean(Settings.KEYS.ANALYZER_NUSPEC_ENABLED, true)
		//NEXUS ANALYZER
		Settings.setBoolean(Settings.KEYS.ANALYZER_CENTRAL_ENABLED, true)
		//NEXUS ANALYZER
		Settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_ENABLED, true)
		Settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_USES_PROXY, true)
		//ARCHIVE ANALYZER
		Settings.setBoolean(Settings.KEYS.ANALYZER_ARCHIVE_ENABLED, true)
		Settings.setBoolean(Settings.KEYS.ANALYZER_ASSEMBLY_ENABLED, true)
		// Fixing issues with h2 versions 1.4+ that somehow come bundled with play.
		//Settings.setString(Settings.KEYS.DB_CONNECTION_STRING, "jdbc:h2:file:D:/data/dc;FILE_LOCK=FS;AUTOCOMMIT=ON;")
		Settings.setString(Settings.KEYS.SUPPRESSION_FILE, "dependency-check-suppressions.xml")
	}

	def scanDependencies(conf: Configuration) = Def.task {
		initializeSettings()
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
						// unmanaged JAR, just
						engine.scan {
							new File(attributed.data.getAbsolutePath)
						}
				}
		)
		engine.analyzeDependencies()
		writeReports(engine, new File("."))
		Settings.cleanup()
	}

	def writeReports(engine: Engine, outputDir: File) = {
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
			r.generateReports(outputDir.getAbsolutePath, "all")
		} catch {
			case ioe: IOException =>
			case ex: Throwable =>
		}
	}

	lazy val dependencyCheckCommand =
		Command.command("check") { (state: State) =>
			println("Hi PHASE!")
			state.log.info("check!")
			state
		}

}