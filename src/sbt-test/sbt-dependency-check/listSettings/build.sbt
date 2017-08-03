version := "0.1"
lazy val root = project in file(".")
scalaVersion := "2.10.6"

dependencyCheckSuppressionFiles := Seq(baseDirectory.value / "src/main/resources", baseDirectory.value / "src/app/")