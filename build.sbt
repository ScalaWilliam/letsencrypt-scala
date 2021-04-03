/** Names */
organization := "com.scalawilliam"
name := "letsencrypt-scala"

/** Versions */
version := "0.0.2-SNAPSHOT"
versionScheme := Some("semver-spec")
scalaVersion := "2.13.5"
crossScalaVersions := Seq("2.13.5", "3.0.0-RC2")
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
  "org.scalatest" %% "scalatest" % "3.2.7" % Test, {
    val sv = scalaVersion.value
    "org.typelevel" %% "cats-effect" % (if (sv.startsWith("3"))
                                          "3.0.1"
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
