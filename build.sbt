
name := "Stub"

version := "0.1.0.0"

scalaVersion := "2.10.2"

mainClass := Some("Stub")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.2.2" % "test",
  "com.datastax.cassandra" % "cassandra-driver-core" % "1.0.3",
  "org.apache.kafka" % "kafka_2.10" % "0.8.0",
  "org.apache.avro" % "avro" % "1.7.5",
  "play" %% "play-json" % "2.2-SNAPSHOT" from "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots",
  "com.github.scopt" %% "scopt" % "3.1.0",
  "com.codahale.metrics" % "metrics-core" % "3.0.1" % "compile",
  "nl.grons" %% "metrics-scala" % "3.0.3",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "org.apache.thrift" % "libthrift" % "0.9.1",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5"
)
