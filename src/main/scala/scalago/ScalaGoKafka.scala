package scalago

import java.util.{Properties, UUID}
import kafka.utils.VerifiableProperties
import org.apache.kafka.clients.producer.{ProducerRecord, ProducerConfig, KafkaProducer}
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import kafka.consumer.{ConsumerConfig, Consumer}
import io.confluent.kafka.serializers.{KafkaAvroDecoder, KafkaAvroSerializer}

object ScalaGoKafka {
  var readTopic: String = null
  var writeTopic: String = null
  val group = "ping-pong-scala-group"

  val broker = "localhost:9092"
  val zookeeper = "localhost:2181"
  val schemaRepo = "http://localhost:8081"

  val producerProps = new Properties()
  producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
  producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
  producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer])
  producerProps.put("schema.registry.url", schemaRepo)

  val consumerProps = new Properties()
  consumerProps.put("zookeeper.connect", zookeeper)
  consumerProps.put("group.id", group)
  consumerProps.put("auto.offset.reset", "smallest")
  consumerProps.put("schema.registry.url", schemaRepo)

  val consumerVerifiableProps = new VerifiableProperties(consumerProps)
  val keyDecoder = new KafkaAvroDecoder(consumerVerifiableProps)
  val valueDecoder = new KafkaAvroDecoder(consumerVerifiableProps)

  lazy val producer = new KafkaProducer[String, GenericRecord](producerProps)
  lazy val consumerIterator = Consumer.create(new ConsumerConfig(consumerProps)).createMessageStreams(Map(readTopic -> 1), keyDecoder, valueDecoder).get(readTopic).get(0).iterator()

  def main(args: Array[String]) {
    parseArgs(args)

    println("scala  > Started!")

    val rec = new GenericData.Record(new Schema.Parser().parse(getClass.getResourceAsStream("/scalago.avsc")))
    rec.put("counter", 1L)
    rec.put("name", s"ping-pong-${System.currentTimeMillis()}")
    rec.put("uuid", UUID.randomUUID().toString)

    val record = new ProducerRecord[String, GenericRecord](writeTopic, "0", rec)
    producer.send(record).get
    pingPongLoop()
  }

  def parseArgs(args: Array[String]) {
    if (args.length < 2) {
      println("USAGE: ScalaGoKafka $READ_TOPIC $WRITE_TOPIC")
      System.exit(1)
    }

    readTopic = args(0)
    writeTopic = args(1)
  }

  def pingPongLoop() {
    while (true) {
      val messageAndMetadata = consumerIterator.next()
      val record = messageAndMetadata.message().asInstanceOf[GenericRecord]
      Thread.sleep(2000)

      println("scala  > received " + record)
      modify(record)
      producer.send(new ProducerRecord[String, GenericRecord](writeTopic, "0", record)).get
    }
  }

  def modify(rec: GenericRecord) {
    rec.put("counter", rec.get("counter").asInstanceOf[Long] + 1)
    rec.put("uuid", UUID.randomUUID().toString)
  }
}