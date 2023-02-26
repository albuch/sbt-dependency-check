package net.vonbuchholtz.sbt.dependencycheck

import org.owasp.dependencycheck.Engine
import sbt.Logger

import scala.util.control.NonFatal

object DependencyCheckUpdateTask {
  def update(engine: Engine, log: Logger): Unit = {
    try {
      engine.doUpdates()
    } catch {
      case e: Exception if NonFatal(e) =>
        log.error(s"An exception occurred connecting to the local database: ${e.getLocalizedMessage}")
        throw e
    }
  }
}
