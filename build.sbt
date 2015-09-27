import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

//Project Information
name := "actuarius"

description := "Actuarius is a Markdown Processor written in Scala using parser combinators."

scalaVersion := "2.11.7"

scalacOptions += "-deprecation"

publishMavenStyle := true

autoCompilerPlugins := true

organization := "com.viagraphs" // note that true organization is "eu.henkelmann", this is for sonatype publishing

resolvers += "Scala Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/" 

resolvers += "Scala" at "https://oss.sonatype.org/content/groups/scala-tools/"

version := "0.3.0-SNAPSHOT"

// crossScalaVersions in ThisBuild := Seq("2.9.2", "2.10.0")

lazy val `actuarius` = (project in file(".")).enablePlugins(ScalaJSPlugin)

scalaJSStage := FastOptStage

testFrameworks += new TestFramework("utest.runner.Framework")

requiresDOM := true

libraryDependencies ++= {
  Seq(
    "org.scalajs" %%% "scala-parser-combinators" % "1.0.3",
    "com.lihaoyi" %%% "utest" % "0.3.1" % "test"
  )
}

//TODO: reactivate once junit-XML listener is on maven central
//testListeners <<= target.map(t => Seq(new eu.henkelmann.sbt.JUnitXmlTestsListener(t.getAbsolutePath)))

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/chenkelmann/actuarius</url>
  <licenses>
    <license>
      <name>BSD 3 clause</name>
      <url>http://opensource.org/licenses/BSD-3-Clause</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:l15k4/actuarius.git</url>
    <connection>scm:git:git@github.com:l15k4/actuarius.git</connection>
  </scm>
  <developers>
  <developer>
    <id>chenkelmann</id>
    <name>Christoph Henkelmann</name>
    <url>http://henkelmann.eu/</url>
  </developer>
    <developer>
      <id>dpp</id>
      <name>David Pollak</name>
      <url>http://blog.goodstuff.im</url>
    </developer>
    <developer>
      <id>l15k4</id>
      <name>Jakub Liska</name>
      <email>liska.jakub@gmail.com</email>
    </developer>
  </developers>)

