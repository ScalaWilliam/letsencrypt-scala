ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "co.fs2"           %% "fs2-io"          % "3.0.1",
  "com.scalawilliam" %% "letsencrypt-ce3" % "0.0.5-SNAPSHOT"
)

reStartArgs += "--insecure"
