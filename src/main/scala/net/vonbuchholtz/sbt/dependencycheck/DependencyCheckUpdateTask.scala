package net.vonbuchholtz.sbt.dependencycheck

import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.utils.Settings
import sbt.Logger

object DependencyCheckUpdateTask {
  def update(settings: Settings, log: Logger): Unit = {
    val engine: Engine = new Engine(classOf[Engine].getClassLoader, settings)
    try {
      engine.doUpdates()
    } catch {
      case e: Exception =>
        log.error(s"An exception occurred connecting to the local database: ${e.getLocalizedMessage}")
        throw e
    } finally {
      engine.close()
      settings.cleanup()
    }
  }
}
