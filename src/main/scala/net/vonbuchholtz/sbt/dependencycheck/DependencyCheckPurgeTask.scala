package net.vonbuchholtz.sbt.dependencycheck

import java.io.{File, IOException}

import org.owasp.dependencycheck.utils.Settings
import sbt.*

object DependencyCheckPurgeTask {
  def purge(connectionString: Option[String], settings: Settings, log: Logger): Unit = {
    if(connectionString.isDefined) {
      throw new IllegalStateException("Unable to purge the local NVD when using a non-default connection string")
    }

    try {
      val db: File = new File(settings.getDataDirectory, settings.getString(Settings.KEYS.DB_FILE_NAME))
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
      settings.cleanup()
    }
  }
}
