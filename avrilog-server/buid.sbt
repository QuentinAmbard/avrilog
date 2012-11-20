organization := "com.avricot.avrilog"

name := "avrilog-server"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
"Sonatype OSS Snapshots" at  "http://oss.sonatype.org/content/repositories/snapshots/",
"Sonatype OSS Releases" at  "http://oss.sonatype.org/content/repositories/releases/",
"takezoux2@github" at "http://takezoux2.github.com/maven"
)

libraryDependencies ++= Seq(
	"com.rabbitmq" % "amqp-client" % "2.8.7"  ,
	"com.typesafe.akka" % "akka-actor" % "2.0.3"  ,
	"commons-codec" % "commons-codec" % "1.7"  ,
	"org.msgpack" % "msgpack" % "0.6.6" ,
	"com.avricot" %% "horm" % "0.2-SNAPSHOT")
           
//EclipseKeys.withSource := true
