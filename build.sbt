name := "letsencrypt-scala-root"

ThisBuild / organization := "com.scalawilliam"

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / publish / skip := true

ThisBuild / version := "0.0.5-SNAPSHOT"

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / scalaVersion := "2.13.6"

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
  .aggregate(`examples-fs2-echo`)
  .aggregate(`examples-http4s`)
  .aggregate(`examples-play`)

lazy val `play-ce2` = project
  .settings(
    publish / skip := false,
    crossScalaVersions := Seq("2.12.12", "2.13.6"),
    name := "letsencrypt-play-ce2",
    scalaVersion := "2.13.6",
    libraryDependencies += "com.typesafe.play" %% "play" % "2.8.8"
  )
  .dependsOn(ce2)

lazy val `play-ce3` = project
  .settings(
    publish / skip := false,
    crossScalaVersions := Seq("2.12.12", "2.13.6"),
    name := "letsencrypt-play",
    scalaVersion := "2.13.6",
    libraryDependencies += "com.typesafe.play" %% "play" % "2.8.8"
  )
  .dependsOn(ce3)

lazy val ce2 =
  project
    .settings(
      publish / skip := false,
      crossScalaVersions := Seq("2.12.12", "2.13.6", "3.0.1"),
      name := "letsencrypt-scala-ce2",
      libraryDependencies += "org.typelevel"    %% "cats-effect" % "2.5.1",
      libraryDependencies += "org.bouncycastle" % "bcprov-jdk16" % "1.46",
      Compile / scalaSource := (ce3 / Compile / scalaSource).value
    )

lazy val ce3 =
  project
    .settings(
      publish / skip := false,
      crossScalaVersions := Seq("2.12.12", "2.13.6", "3.0.1"),
      name := "letsencrypt-scala",
      libraryDependencies += "org.typelevel"    %% "cats-effect" % "3.2.0",
      libraryDependencies += "org.bouncycastle" % "bcprov-jdk16" % "1.46"
    )

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val `examples-fs2-echo` = project.in(file("examples/fs2-echo"))
lazy val `examples-http4s`   = project.in(file("examples/http4s"))
lazy val `examples-play`     = project.in(file("examples/play"))
