import sbt._
import Keys._

object AvrilogBuild extends Build {
    def standardSettings = Seq(
      exportJars := true
    ) ++ Defaults.defaultSettings

    lazy val root = Project(id = "avrilog", settings = standardSettings,
                            base = file(".")) aggregate(common, server)

    lazy val common = Project(id = "common", settings = standardSettings,
                            base = file("avrilog-common"))

    lazy val server = Project(id = "server", settings = standardSettings,
                           base = file("avrilog-server")) dependsOn(common)
}
