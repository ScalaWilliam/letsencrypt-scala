/** Names */
name := "letsencrypt-scala-root"

ThisBuild / organization := "com.scalawilliam"

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / version := "0.0.5-SNAPSHOT"

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / scalaVersion := "2.13.6"

ThisProject / crossScalaVersions := Seq("2.12.12", "2.13.6", "3.0.1")

ThisBuild / scalacOptions := Nil

ThisBuild / homepage := Some(
  url("https://www.scalawilliam.com/letsencrypt-scala/"))

ThisBuild / licenses := List(
  "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / developers := List(
  Developer(
    "ScalaWilliam",
    "ScalaWilliam",
    "hello@scalawilliam.com",
    url("https://www.scalawilliam.com")
  )
)

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value)
  Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//  Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test

lazy val root = project
  .in(file("."))
  .aggregate(ce2)
  .aggregate(ce3)
  .aggregate(`play-ce2`)
  .aggregate(`play-ce3`)

publish / skip := true

lazy val `play-ce2` = project
  .settings(
    publish / skip := true,
    name := "letsencrypt-play-ce2",
    scalaVersion := "2.13.6",
    libraryDependencies += "com.typesafe.play" %% "play" % "2.8.8"
  )
  .dependsOn(ce2)

lazy val `play-ce3` = project
  .settings(
    publish / skip := true,
    name := "letsencrypt-play-ce3",
    scalaVersion := "2.13.6",
    libraryDependencies += "com.typesafe.play" %% "play" % "2.8.8"
  )
  .dependsOn(ce3)

lazy val `play-example` = project
  .dependsOn(`play-ce3`)
  .enablePlugins(PlayScala)
  .settings(publish / skip := true, libraryDependencies += guice)

lazy val ce2 =
  project
    .settings(
      name := "letsencrypt-ce2",
      libraryDependencies += "org.typelevel"    %% "cats-effect" % "2.5.1",
      libraryDependencies += "org.bouncycastle" % "bcprov-jdk16" % "1.46")

lazy val ce3 =
  project
    .settings(
      Compile / scalaSource := (ce2 / Compile / scalaSource).value,
      name := "letsencrypt-ce3",
      libraryDependencies += "org.typelevel"    %% "cats-effect" % "3.2.0",
      libraryDependencies += "org.bouncycastle" % "bcprov-jdk16" % "1.46"
    )

Global / onChangedBuildSource := ReloadOnSourceChanges
