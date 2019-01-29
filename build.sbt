name := """SmartScape"""
organization := "edu.wisc"

version := "2.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.7"

libraryDependencies += guice
