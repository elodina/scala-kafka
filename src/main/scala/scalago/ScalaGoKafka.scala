package scalago

import java.io.{FileOutputStream, ByteArrayOutputStream}
import java.nio.ByteBuffer
import kafka.consumer.KafkaConsumer
import kafka.producer.KafkaProducer
import org.apache.avro.generic.{GenericDatumWriter, IndexedRecord}
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.{SpecificDatumWriter, SpecificRecord}

object ScalaGoKafka {
  var readTopic: String = null
  var writeTopic: String = null
  val group = "ping-pong-scala-group"

  val broker = "localhost:9092"
  val zookeeper = "localhost:2181"

  val pingPong = new PingPong(1, s"ping-pong-${System.currentTimeMillis()}")
  lazy val producer = new KafkaProducer(writeTopic, broker)

  def main(args: Array[String]) = {
    parseArgs(args)

    println("scala  > Started!")
    producer.send(AvroWrapper.encode(pingPong), null)
    pingPongLoop(pingPong)
  }

  def parseArgs(args: Array[String]) {
    if (args.length < 2) {
      println("USAGE: ScalaGoKafka $READ_TOPIC $WRITE_TOPIC")
      System.exit(1)
    }

    readTopic = args(0)
    writeTopic = args(1)
  }

  def pingPongLoop(obj: PingPong) {
    var message: Array[Byte] = null
    while (true) {
      val consumer = new KafkaConsumer(readTopic, group, zookeeper)
      consumer.read(bytes => {
        Thread.sleep(2000)
        println("scala  > received " + new String(bytes))
        message = bytes
        consumer.close()
      })

      pingPong.setCounter(new String(message).toLong + 1)
      producer.send(AvroWrapper.encode(pingPong), null)
    }
  }
}

object AvroWrapper {
  final val MAGIC = Array[Byte](0x0)

  def encode(obj: IndexedRecord): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    out.write(MAGIC)
    out.write(ByteBuffer.allocate(4).putInt(0).array())

    val encoder = EncoderFactory.get().directBinaryEncoder(out, null)
    val writer = if (obj.isInstanceOf[SpecificRecord])
      new SpecificDatumWriter[IndexedRecord](obj.getSchema)
    else new GenericDatumWriter[IndexedRecord](obj.getSchema)

    writer.write(obj, encoder)

    //just for debug
    val fos = new FileOutputStream("out.avro")
    fos.write(out.toByteArray)
    fos.close()

    out.toByteArray
  }
}