import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "avrilog-web"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "jp.t2v" %% "play20.auth" % "0.4", //0.2
        "com.avricot" %% "horm" % "0.2-SNAPSHOT",
        "org.apache.hbase" % "hbase" % "0.94.0",
        "org.apache.hadoop" % "hadoop-common" % "0.23.1",
        "org.apache.hadoop" % "hadoop-auth" % "0.23.1",
        "joda-time" % "joda-time" % "2.1"
   )
    
    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
        resolvers += "t2v.jp repo" at "http://www.t2v.jp/maven-repo/"
    )

}
