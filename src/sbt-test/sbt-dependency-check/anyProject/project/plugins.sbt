resolvers += Resolver.sonatypeRepo("snapshots")
sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                        |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}