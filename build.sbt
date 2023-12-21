
// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.12"

name := "Flink-Examples"

version := "1.0"
// https://mvnrepository.com/artifact/org.apache.flink/flink-streaming-java
libraryDependencies += "org.apache.flink" % "flink-streaming-java" % "1.18.0" % "provided"

// https://mvnrepository.com/artifact/org.apache.flink/flink-connector-kafka
libraryDependencies += "org.apache.flink" % "flink-connector-kafka" % "3.0.2-1.18"
// by default sbt run runs the program in the same JVM as sbt
//in order to run the program in a different JVM, we add the following
fork in run := true