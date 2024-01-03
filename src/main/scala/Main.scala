//using scala "2.13.12"
//> using dep "org.apache.flink::flink-streaming-java:1.18.0"
//> using dep "org.apache.flink::flink-connector-kafka:3.0.2-1.18"
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.flink.streaming.api.watermark
import org.apache.flink.streaming.api.transformations

import org.apache.flink.streaming.api.operators
import org.apache.flink.streaming.api.graph

import org.apache.flink.streaming.api.functions

import org.apache.flink.streaming.api.environment

import org.apache.flink.streaming.api.datastream

import org.apache.flink.streaming.api.connector

import org.apache.flink.streaming.api.checkpoint

import org.apache.flink.streaming.api.environment.LocalStreamEnvironment
import org.apache.flink.streaming.api.environment.RemoteStreamEnvironment
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironmentFactory
import org.apache.flink.connector.kafka.source.split
import org.apache.flink.connector.kafka.source.reader
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.KafkaSourceOptions
import org.apache.flink.connector.kafka.sink.KafkaSink

import org.apache.flink.connector.kafka.sink.DefaultKafkaSinkContext

import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.connector.kafka.source.enumerator
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka
import org.apache.flink.configuration.Configuration

//provides extension methods that convert between Scala and Java function types
import scala.jdk.FunctionConverters._
import scala.jdk.CollectionConverters._
import scala.jdk.javaapi
import org.apache.flink.api.common.RuntimeExecutionMode
object Main extends App {
  // val environment = new LocalStreamEnvironment()

  val environment = StreamExecutionEnvironment
    .getExecutionEnvironment()
    .setMaxParallelism(8)
    .setParallelism(4)
    .enableCheckpointing(500L)

  val flinkConf = new Configuration()

  val env = StreamExecutionEnvironment
    .createLocalEnvironmentWithWebUI(flinkConf)
    .setMaxParallelism(8)
    .setParallelism(4)
    .enableCheckpointing(500L)

  val streamEnv = StreamExecutionEnvironment
    .createLocalEnvironment(flinkConf)
    .setRuntimeMode(RuntimeExecutionMode.STREAMING)

  val amounts = environment.fromElements(1, 29, 40, 50)

  val seq = environment.fromSequence(1, 500)

  val collect = amounts.filter(_ > 30) // .reduce((acc, x) => x + acc)
//union-> each stream contains the same data type
  val union = seq.union(amounts.map(_.toInt))

// connect-> two different data types

  val connect = seq.connect(
    amounts
  )//.process( new functions.co.CoProcessFunction())

  val kafkaSource = KafkaSource
    .builder()
    .setBootstrapServers("localhost:9092,localhost:9093,localhost:9094")
    .setTopics("flink-examples")
    .setGroupId("flink-consumer-group")
    .setStartingOffsets(OffsetsInitializer.earliest())
    .setValueOnlyDeserializer(new SimpleStringSchema())
    .build()
  val lines = environment.fromSource(
    kafkaSource,
    WatermarkStrategy.noWatermarks(),
    "Kafka source"
  )

  // before sending data, we need to serialize our value
  val serializer = KafkaRecordSerializationSchema
    .builder()
    .setValueSerializationSchema(new SimpleStringSchema())
    .setTopic("flink-examples-out")
    .build()
  val kafkaSink = KafkaSink
    .builder()
    .setBootstrapServers("localhost:9092,localhost:9093,localhost:9094")
    // .setDeliveryGuarantee(DeliveryGuarantee)
    .setRecordSerializer(serializer)
    .build()

  // we start reading from kafka
  val texts = environment.fromElements("hello", "world", "how are you")

// sending to new kafka topic
  // lines.sinkTo(kafkaSink)
//val f=texts.flatMap(_.split("\\W+"))
  println("Hello, World!")
  collect.print()

  //environment.execute()

  val h=(1 to 100).sliding(3)
  h.foreach(println)
}
