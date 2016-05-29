package net.vonbuchholtz.sbt.dependencycheck

import sbt.ModuleID
import sbt.std.TaskStreams


object Reporter {

	def aggregate(dependencies: Seq[ModuleID], out: TaskStreams[_]) : Unit = {

	}

	def check(dependencies: Seq[ModuleID], out: TaskStreams[_]) : Unit = {
	}
}
