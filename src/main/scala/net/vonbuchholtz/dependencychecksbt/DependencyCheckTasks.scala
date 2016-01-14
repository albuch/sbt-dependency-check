package net.vonbuchholtz.dependencychecksbt

import sbt.Keys._

trait DependencyCheckTasks {

	def dependencyCheckTask = (libraryDependencies, streams).map(Reporter.check)

	def dependencyCheckAggregateTask = (libraryDependencies, streams).map(Reporter.aggregate)

}
