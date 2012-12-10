organization := "com.avricot.avrilog"

name := "avrilog-server"

version := "0.1"

scalaVersion := "2.9.1"

mainClass in oneJar := Some("com.avricot.avrilog.Main")

resolvers ++= Seq(
"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
"Sonatype OSS Snapshots" at  "http://oss.sonatype.org/content/repositories/snapshots/",
"Sonatype OSS Releases" at  "http://oss.sonatype.org/content/repositories/releases/",
"Myp Snapshot" at  "http://nexus.myprocurement.fr/content/repositories/snapshots/",
"Myp Releases" at  "http://nexus.myprocurement.fr/content/repositories/releases/",
"takezoux2@github" at "http://takezoux2.github.com/maven"
)

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

libraryDependencies ++= Seq(
	"com.rabbitmq" % "amqp-client" % "2.8.7"  ,
	"com.typesafe.akka" % "akka-slf4j" % "2.0.3"  ,
	"com.typesafe.akka" % "akka-actor" % "2.0.3"  ,
	"commons-codec" % "commons-codec" % "1.7"  ,
	"org.msgpack" % "msgpack" % "0.6.6" ,
        "com.avricot.avrilog" %% "avrilog-common" % "0.1-SNAPSHOT",
	//"com.avricot.avrilog" %% "avrilog-common" % "0.1-SNAPSHOT" exclude("org.jruby", "jruby-complete") ,
	"com.avricot" %% "horm" % "0.3-SNAPSHOT" exclude("org.jruby", "jruby-complete"))
           
//EclipseKeys.withSource := true


