package net.vonbuchholtz.sbt.dependencycheck

import org.owasp.dependencycheck.utils.Settings
import sbt.Logger

object DependencyCheckAggregateTask {

  def aggregate(settings: Settings, log: Logger): Unit = {
    // working around threadlocal issue with DependencyCheck's Settings and sbt task dependency system.
    Settings.setInstance(settings)

    Settings.cleanup()
  }
}
