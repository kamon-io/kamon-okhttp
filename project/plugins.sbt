lazy val root = project in file(".") dependsOn RootProject(uri("git://github.com/kamon-io/kamon-sbt-umbrella.git#kamon-2.x"))
lazy val latestSbtUmbrella = uri("git://github.com/kamon-io/kamon-sbt-umbrella.git")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")
