organization := "com.scalawilliam.rad4s"

scalaVersion := "2.13.6"

// Allow to package the whole app and its dependencies to a ZIP file
enablePlugins(JavaServerAppPackaging)

version := "0.1"
val Http4sVersion = "0.22.0"

// To be able to use the letsencrypt-scala library
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  // Always have this
  "org.http4s" %% "http4s-dsl"  % Http4sVersion,
  "org.http4s" %% "http4s-core" % Http4sVersion,
  // Render HTML with scalatags
  "org.http4s" %% "http4s-scalatags" % Http4sVersion,
  "org.http4s" %% "http4s-server"    % Http4sVersion,
  // http4s has various back-end implementations, even Servlets!
  "org.http4s"       %% "http4s-blaze-server" % Http4sVersion,
  "com.scalawilliam" %% "letsencrypt-ce2"     % "0.0.5-SNAPSHOT"
)

// To enable local development without a certificate
// use the '~ reStart' command in SBT to try this out
reStartArgs += "--insecure"

Global / onChangedBuildSource := ReloadOnSourceChanges
