lazy val instantGroup = (project in file("."))
  .settings(
    name := "instant-group",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.8"
  ).dependsOn(commons)

lazy val commons = ProjectRef(uri("https://github.com/odwrotnie/rzepaw-commons.git#master"), "rzepawCommons")

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0-M16-SNAP1"
