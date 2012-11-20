//TODO http://stackoverflow.com/questions/4334534/how-to-set-default-libs-for-sbt-subprojects
organization := "com.avricot.avrilog"

name := "avrilog-common"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
"Sonatype OSS Snapshots" at  "http://oss.sonatype.org/content/repositories/snapshots/",
"Sonatype OSS Releases" at  "http://oss.sonatype.org/content/repositories/releases/",
"takezoux2@github" at "http://takezoux2.github.com/maven"
)

//EclipseKeys.createSrc := EclipseCreateSrc.Unmanaged + EclipseCreateSrc.Default + EclipseCreateSrc.Resource

libraryDependencies ++= Seq(
	"junit" % "junit" % "4.10" % "test",
    "org.apache.hbase" % "hbase" % "0.94.0" ,
    "org.apache.hadoop" % "hadoop-common" % "0.23.1"  ,
    "org.apache.hadoop" % "hadoop-auth" % "0.23.1"  ,
    "joda-time" % "joda-time" % "2.1"  ,
	"org.bouncycastle" % "bcprov-jdk16" % "1.46"  ,
	"org.bouncycastle" % "bctsp-jdk16" % "1.46"  ,
	"org.bouncycastle" % "bcmail-jdk16" % "1.46"  ,
	"com.github.scala-incubator.io" %% "scala-io-core" % "0.4.1-seq"  ,
    "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.1.1"   ,
	"com.typesafe" % "config" % "0.6.0"  ,
	"org.msgpack" %% "msgpack-scala" % "0.6.6"    ,
	"com.avricot" %% "horm" % "0.2-SNAPSHOT")


