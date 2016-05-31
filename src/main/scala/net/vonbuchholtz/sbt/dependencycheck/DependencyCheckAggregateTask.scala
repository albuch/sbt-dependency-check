package net.vonbuchholtz.sbt.dependencycheck

import org.owasp.dependencycheck.utils.Settings

object DependencyCheckAggregateTask {

  def aggregate(settings: Settings): Unit = {
    // working around threadlocal issue with DependencyCheck's Settings and sbt task dependency system.
    Settings.setInstance(settings)

    Settings.cleanup()
  }
}
