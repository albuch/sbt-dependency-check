lazy val root = (project in file("."))
  .aggregate(meta)

lazy val meta = project
  .disablePlugins(DependencyCheckPlugin)
