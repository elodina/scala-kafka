package scalago

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.UUID
import kafka.consumer.KafkaConsumer
import kafka.producer.KafkaProducer
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericData, GenericRecord, GenericDatumWriter}
import org.apache.avro.io.{DecoderFactory, EncoderFactory}

object ScalaGoKafka {
  var readTopic: String = null
  var writeTopic: String = null
  val group = "ping-pong-scala-group"

  val broker = "localhost:9092"
  val zookeeper = "localhost:2181"

  lazy val producer = new KafkaProducer(writeTopic, broker)

  val schemaRegistry = Map(0 -> new Schema.Parser().parse(getClass.getResourceAsStream("/scalago.avsc")))

  def main(args: Array[String]) {
    parseArgs(args)

    println("scala  > Started!")

    val schemaId = 0
    val rec = new GenericData.Record(schemaRegistry.get(schemaId).get)
    rec.put("counter", 1L)
    rec.put("name", s"ping-pong-${System.currentTimeMillis()}")
    rec.put("uuid", UUID.randomUUID().toString)

    producer.send(AvroWrapper.encode(rec, schemaId), null)
    pingPongLoop(schemaId)
  }

  def parseArgs(args: Array[String]) {
    if (args.length < 2) {
      println("USAGE: ScalaGoKafka $READ_TOPIC $WRITE_TOPIC")
      System.exit(1)
    }

    readTopic = args(0)
    writeTopic = args(1)
  }

  def pingPongLoop(schemaId: Int) {
    var message: Array[Byte] = null
    while (true) {
      val consumer = new KafkaConsumer(readTopic, group, zookeeper)
      consumer.read(bytes => {
        Thread.sleep(2000)
        message = bytes
        consumer.close()
      })

      val record = AvroWrapper.decode(message)
      println("scala  > received " + record)
      modify(record)
      producer.send(AvroWrapper.encode(record, schemaId), null)
    }
  }

  def modify(rec: GenericRecord) {
    rec.put("counter", rec.get("counter").asInstanceOf[Long] + 1)
    rec.put("uuid", UUID.randomUUID().toString)
  }
}

object AvroWrapper {
  final val MAGIC = Array[Byte](0x0)

  def encode(obj: GenericRecord, schemaId: Int): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    out.write(MAGIC)
    out.write(ByteBuffer.allocate(4).putInt(schemaId).array())

    val encoder = EncoderFactory.get().directBinaryEncoder(out, null)
    val schemaOpt = ScalaGoKafka.schemaRegistry.get(schemaId)
    if (schemaOpt.isEmpty) throw new IllegalArgumentException("Invalid schema id")

    val writer = new GenericDatumWriter[GenericRecord](schemaOpt.get)
    writer.write(obj, encoder)

    out.toByteArray
  }

  def decode(bytes: Array[Byte]): GenericRecord = {
    val decoder = DecoderFactory.get().binaryDecoder(bytes, null)
    val magic = new Array[Byte](1)
    decoder.readFixed(magic)
    if (magic.deep != MAGIC.deep) throw new IllegalArgumentException("Not a camus byte array")

    val schemaIdArray = new Array[Byte](4)
    decoder.readFixed(schemaIdArray)

    val schemaOpt = ScalaGoKafka.schemaRegistry.get(ByteBuffer.wrap(schemaIdArray).getInt)
    schemaOpt match {
      case None => throw new IllegalArgumentException("Invalid schema id")
      case Some(schema) =>
        val reader = new GenericDatumReader[GenericRecord](schema)
        reader.read(null, decoder)
    }
  }
}