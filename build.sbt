name := "storage-service"

version := "0.1"

scalaVersion := "2.13.0"

val http4sVersion = "0.21.0"
val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.13.0",
  "io.circe" %% "circe-literal" % "0.13.0",
  "io.circe" %% "circe-parser" % circeVersion
)
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.typelevel" %% "cats-effect" % "2.1.2"
)
libraryDependencies += "co.fs2" %% "fs2-core" % "2.2.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

mainClass in (Compile, run) := Some("tox.storage_service.MainServer")

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

packageName in Docker := packageName.value
version in Docker := version.value
dockerBaseImage in Docker := "openjdk"
dockerRepository in Docker := Some("toxrepo")
dockerExposedPorts := Seq(8080)