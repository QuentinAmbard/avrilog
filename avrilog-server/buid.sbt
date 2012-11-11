organization := "com.avricot.avrilog"

name := "avrilog-server"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
"Sonatype OSS Releases" at  "http://oss.sonatype.org/content/repositories/releases/" )

libraryDependencies ++= Seq(
	"com.rabbitmq" % "amqp-client" % "2.8.7" withSources()  ,
	"com.typesafe.akka" % "akka-actor" % "2.0.3" withSources() ,
	"commons-codec" % "commons-codec" % "1.7" withSources() ,
	"com.github.scala-incubator.io" %% "scala-io-core" % "0.4.1-seq" withSources() ,
	"org.msgpack" % "msgpack" % "0.6.6" withSources() ,
	"com.avricot" %% "horm" % "0.1" withSources() ,
	"org.bouncycastle" % "bcprov-jdk16" % "1.46" withSources() ,
	"org.bouncycastle" % "bctsp-jdk16" % "1.46" withSources() ,
	"org.bouncycastle" % "bcmail-jdk16" % "1.46" withSources() ,
    "org.scalamock" %% "scalamock-scalatest-support" % "latest.integration"
	//"ch.qos.logback" % "logback-classic" % "1.0.6" withSources()
	//exclude("org.slf4j", "slf4j-log4j12") exclude("slf4j", "slf4j")
)
           
EclipseKeys.withSource := true
