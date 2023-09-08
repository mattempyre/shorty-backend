name := """shorty-backend"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  filters,
  guice,
  "com.google.inject" % "guice" % "5.1.0",
  "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  "org.mockito" % "mockito-core" % "3.12.4" % Test,
  "net.debasishg" %% "redisclient" % "3.30",
  "com.github.etaty" %% "rediscala" % "1.9.0",
  "commons-codec" % "commons-codec" % "1.15"
)

// resolvers += Resolver.sonatypeRepo("releases")
// resolvers += Resolver.sonatypeRepo("snapshots")
