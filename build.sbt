/** Names */
ThisBuild / organization := "com.scalawilliam"
name := "letsencrypt-scala"

/** Versions */
ThisBuild / version := "0.0.4-SNAPSHOT"
ThisBuild / versionScheme := Some("semver-spec")
ThisBuild / scalaVersion := "2.13.6"
ThisProject / crossScalaVersions := Seq("2.13.6", "3.0.1")
ThisBuild / scalacOptions := Nil

/** Publishing: currently to Sonatype snapshots */
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value)
  Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//  Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

/** Dependencies */
libraryDependencies ++= Seq(
  {
    val sv         = scalaVersion.value
    val scalaTestV = "3.2.9"
    "org.scalatest" %% "scalatest" % scalaTestV % Test
  }, {
    val myName = name.value
    "org.typelevel" %% "cats-effect" % (if (myName.endsWith("ce3")) "3.1.1"
                                        else "2.4.1")
  },
  "org.bouncycastle" % "bcprov-jdk16" % "1.46"
)

/** Metadata */
homepage := Some(url("https://www.scalawilliam.com/letsencrypt-scala/"))

licenses := List(
  "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

developers := List(
  Developer(
    "ScalaWilliam",
    "ScalaWilliam",
    "hello@scalawilliam.com",
    url("https://www.scalawilliam.com")
  )
)

lazy val root = ProjectRef(file("."), "letsencrypt-scala")

lazy val play = project
  .dependsOn(root)
  .settings(
    name := "letsencrypt-play",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq("com.typesafe.play" %% "play"      % "2.8.8",
                                "org.scalatest"     %% "scalatest" % "3.2.7" % Test)
  )

lazy val `play-example` = project
  .dependsOn(play)
  .enablePlugins(PlayScala)
  .settings(libraryDependencies += guice)
