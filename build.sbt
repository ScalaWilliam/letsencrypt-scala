/** Names */
organization := "com.scalawilliam"
name := "letsencrypt-scala"

/** Versions */
version := "0.0.3-SNAPSHOT"
versionScheme := Some("semver-spec")
scalaVersion := "2.13.5"
crossScalaVersions := Seq("2.13.5", "3.0.0-RC1", "3.0.0-RC2")
scalacOptions := Nil

/** Publishing: currently to Sonatype snapshots */
publishTo := {
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
    val scalaTestV = if (sv == "3.0.0-RC1") "3.2.6" else "3.2.7"
    "org.scalatest" %% "scalatest" % scalaTestV % Test
  }, {
    val myName = name.value
    "org.typelevel" %% "cats-effect" % (if (myName.endsWith("ce3")) "3.0.1"
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
    scalaVersion := "2.13.5",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-server" % "2.8.8",
      "org.scalatest"     %% "scalatest"   % "3.2.7" % Test)
  )
