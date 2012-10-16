organization := "com.avricot.avrilog"

name := "avrilog-parent"

version := "0.1"

scalaVersion := "2.9.1"

resolvers ++= Seq(
	"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
	"takezoux2@github" at "http://takezoux2.github.com/maven"
)

EclipseKeys.withSource := true
