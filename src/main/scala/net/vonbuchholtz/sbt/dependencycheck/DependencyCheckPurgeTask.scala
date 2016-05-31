package net.vonbuchholtz.sbt.dependencycheck

import java.io.{File, IOException}

import org.owasp.dependencycheck.utils.Settings
import sbt._

object DependencyCheckPurgeTask {

  def purge(connectionString: Option[String], settings: Settings, log: Logger): Unit = {

    if(connectionString.isDefined) {
      throw new IllegalStateException("Unable to purge the local NVD when using a non-default connection string")
    }

    // working around threadlocal issue with DependencyCheck's Settings and sbt task dependency system.
    Settings.setInstance(settings)
    try {
      val db: File = new File(Settings.getDataDirectory, "dc.h2.db")
      if (db.exists()) {
        if (db.delete) {
          log.info("Database file purged; local copy of the NVD has been removed")
        } else {
          log.error(s"Unable to delete '${db.getAbsolutePath}'; please delete the file manually")
        }
      } else {
        log.error(s"Unable to delete '${db.getAbsolutePath}'; the database file does not exists")
      }
    } catch {
      case e: IOException =>
        log.error(s"Can't purge NVD database: ${e.getLocalizedMessage}")
        throw e
    } finally {
      Settings.cleanup()
    }
  }
}
