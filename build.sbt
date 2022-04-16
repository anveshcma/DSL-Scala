ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "DSL"

  )
val scalaTestVersion = "3.2.9"

//noinspection SbtDependencyVersionInspection
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalatest" %% "scalatest-featurespec" % scalaTestVersion % Test)


