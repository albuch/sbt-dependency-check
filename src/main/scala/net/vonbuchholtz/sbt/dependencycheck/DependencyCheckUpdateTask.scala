package net.vonbuchholtz.sbt.dependencycheck

import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.utils.Settings
import sbt.Logger

object DependencyCheckUpdateTask {

  def update(settings: Settings, log: Logger): Unit = {
    // working around threadlocal issue with DependencyCheck's Settings and sbt task dependency system.
    Settings.setInstance(settings)

    val engine: Engine = new Engine(classOf[Engine].getClassLoader)
    try {
      engine.doUpdates()
    } catch {
      case e: Exception =>
        log.error(s"An exception occured connecting to the local database: ${e.getLocalizedMessage}")
    } finally {
      engine.cleanup()
      Settings.cleanup()
    }
  }
}
