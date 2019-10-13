name := "Producer_Consumer"

version := "0.1"

libraryDependencies += "org.apache.kafka" %% "kafka" % "2.1.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.0"

val sparkVersion = "2.4.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion

)

libraryDependencies += ("com.datastax.spark" %% "spark-cassandra-connector" % "2.3.0").exclude("io.netty", "netty-handler")

//libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "2.3"

scalaVersion := "2.11.0"
