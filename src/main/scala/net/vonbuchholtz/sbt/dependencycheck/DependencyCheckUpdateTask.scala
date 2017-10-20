package net.vonbuchholtz.sbt.dependencycheck

import org.owasp.dependencycheck.Engine
import sbt.Logger

object DependencyCheckUpdateTask {
  def update(engine: Engine, log: Logger): Unit = {
    try {
      engine.doUpdates()
    } catch {
      case e: Exception =>
        log.error(s"An exception occurred connecting to the local database: ${e.getLocalizedMessage}")
        throw e
    }
  }
}
