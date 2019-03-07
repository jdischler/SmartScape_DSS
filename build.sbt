name := """SmartScape"""
organization := "edu.wisc"

version := "2.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  
scalaVersion := "2.12.7"

libraryDependencies += guice

libraryDependencies ++= Seq(
  guice,
  javaCore,
  javaJdbc,
  "com.h2database" % "h2" % "1.4.197",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-email" % "1.3.3"
)

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")