
name := "scala-kafka"

version := "0.1.0.0"

scalaVersion := "2.10.3"

mainClass := Some("Stub")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.2.2" % "test",
  "org.apache.kafka" % "kafka_2.10" % "0.8.1" intransitive(),
  "com.101tec" % "zkclient" % "0.3",
  "log4j" % "log4j" % "1.2.17",
  "net.sf.jopt-simple" % "jopt-simple" % "4.5",
  "org.apache.avro" % "avro" % "1.7.5",
  "play" %% "play-json" % "2.2-SNAPSHOT" from "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots",
  "com.github.scopt" %% "scopt" % "3.1.0",
  "com.yammer.metrics" % "metrics-core" % "2.2.0" % "compile",
  "nl.grons" %% "metrics-scala" % "3.0.3",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "org.apache.thrift" % "libthrift" % "0.9.1",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5"
)
