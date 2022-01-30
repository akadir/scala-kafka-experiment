name := "test-consumer"

version := "0.1"

scalaVersion := "2.12.8"

mainClass in(Compile, run) := Some("com.test.app.Main")

parallelExecution := false

test in assembly := {}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

lazy val versions = new {
  val akka = "2.6.13"
  val akkaHttp = "10.1.9"
  val alpAkka = "2.0.7"
  val logback = "1.2.3"
  val apacheCommons = "1.7"
}

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % versions.akka,
    "com.typesafe.akka" %% "akka-stream-kafka" % versions.alpAkka,
    "com.typesafe.akka" %% "akka-http" % versions.akkaHttp,
    "com.typesafe.akka" %% "akka-protobuf" % versions.akka,
    "com.typesafe.akka" %% "akka-stream" % versions.akka,
    "ch.qos.logback" % "logback-classic" % versions.logback,
    "org.apache.commons" % "commons-text" % versions.apacheCommons,
  )
}

sonarUseExternalConfig := true

