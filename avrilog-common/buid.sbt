//TODO http://stackoverflow.com/questions/4334534/how-to-set-default-libs-for-sbt-subprojects
organization := "com.avricot.avrilog"

name := "avrilog-common"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
	"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
	"takezoux2@github" at "http://takezoux2.github.com/maven"
)

libraryDependencies ++= Seq(
	"junit" % "junit" % "4.10" % "test",
    "org.apache.hbase" % "hbase" % "0.94.0" withSources(),
    "org.apache.hadoop" % "hadoop-common" % "0.23.1" withSources(),
    "org.apache.hadoop" % "hadoop-auth" % "0.23.1" withSources(),
    "joda-time" % "joda-time" % "2.1" withSources(),
	"com.typesafe" % "config" % "0.6.0" withSources(),
	"org.msgpack" %% "msgpack-scala" % "0.6.6"  withSources()
)

