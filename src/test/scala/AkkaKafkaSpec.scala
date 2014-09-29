package ly.stealth.testing

import akka.actor._
import akka.util.Timeout
import akka.testkit.{ImplicitSender, TestKit}
import com.sclasen.akka.kafka._
import kafka.akka.KafkaAkkaProducer
import kafka.consumer.Whitelist
import kafka.producer.KafkaProducer
import kafka.serializer.StringDecoder
import kafka.utils.Logging
import org.specs2.mutable.{After, Specification}
import java.util.UUID
import concurrent.duration._

import org.specs2.time.NoTimeConversions

abstract class AkkaTestkit extends TestKit(ActorSystem()) with After with ImplicitSender {
  // make sure we shut down the actor system after all tests have run
  def after = system.shutdown()
}

class ConsumerActor(testActor: ActorRef, onReceive: (String => _) = (message: String) => ()) extends Actor {
  def receive = {
    case message: String =>
      onReceive(message)
      sender ! StreamFSM.Processed
      testActor ! message
  }
}

class BatchActor[T](testActor: ActorRef, onReceive: (IndexedSeq[_] => T)) extends Actor {
  def receive = {
    case xs: IndexedSeq[_] =>
      onReceive(xs)
      sender ! BatchConnectorFSM.BatchProcessed
      testActor ! xs
  }
}

class AkkaKafkaSpec extends Specification with NoTimeConversions with Logging {
  sequential

  val BROKERS = "192.168.86.10:9092"
  val ZOOKEEPER_CONNECT = "192.168.86.5:2181"

  val messages = 100
  val timeout = 10.seconds
  val batchTimeout = 5.seconds

  "Simple Producer and AkkaConsumer" should {
    "send string to broker and consume that string back" in new AkkaTestkit {
      val testMessage = UUID.randomUUID().toString
      val testTopic = UUID.randomUUID().toString
      val testGroupId = UUID.randomUUID().toString

      info(">>> starting sample broker testing")
      val producer = new KafkaProducer(testTopic, BROKERS)
      producer.send(testMessage)
      info(">>> message sent")

      var testStatus = false

      val actor = system.actorOf(Props(new ConsumerActor(testActor, (message: String) => {
        if (message == testMessage) testStatus = true
      })))
      val consumerProps = AkkaConsumerProps.forSystem(system, ZOOKEEPER_CONNECT, testTopic, testGroupId, 1, new StringDecoder(), new StringDecoder(), actor)
      val consumer = new AkkaConsumer(consumerProps)
      info(">>> starting consumer")
      consumer.start()
      expectMsg(testMessage)
      testStatus must beTrue
      consumer.stop()
    }

    s"send $messages messages to broker and consume them back in different consumer groups" in new AkkaTestkit {
      val testMessage = UUID.randomUUID().toString
      val testTopic = UUID.randomUUID().toString
      val testGroupId_1 = UUID.randomUUID().toString
      val testGroupId_2 = UUID.randomUUID().toString

      info(">>> starting sample broker testing")
      val producer = new KafkaProducer(testTopic, BROKERS)
      (1 to messages).foreach(i => producer.send(UUID.randomUUID().toString))
      info(">>> messages sent")

      val actor1 = system.actorOf(Props(new ConsumerActor(testActor)))
      val consumerProps1 = AkkaConsumerProps.forSystem(system, ZOOKEEPER_CONNECT, testTopic, testGroupId_1, 2, new StringDecoder(), new StringDecoder(), actor1)

      val actor2 = system.actorOf(Props(new ConsumerActor(testActor)))
      val consumerProps2 = AkkaConsumerProps.forSystem(system, ZOOKEEPER_CONNECT, testTopic, testGroupId_2, 2, new StringDecoder(), new StringDecoder(), actor2)

      val consumer1 = new AkkaConsumer(consumerProps1)
      val consumer2 = new AkkaConsumer(consumerProps2)
      consumer1.start()
      consumer2.start()

      receiveN(messages * 2, timeout)
      consumer1.stop()
      consumer2.stop()
    }

    "work fine with topic filters" in new AkkaTestkit {
      val white = "white"
      val black = "black"
      val whitelistedTopic = white + System.currentTimeMillis()
      val blacklistedTopic = black + System.currentTimeMillis()
      val whiteProducer = new KafkaProducer(whitelistedTopic, BROKERS)
      whiteProducer.send(white)
      val blackProducer = new KafkaProducer(blacklistedTopic, BROKERS)
      blackProducer.send(black)

      val actor = system.actorOf(Props(new ConsumerActor(testActor, (message: String) => {
        message must_== white
      })))
      val consumerProps = AkkaConsumerProps.forSystemWithFilter(system, ZOOKEEPER_CONNECT, new Whitelist(s"$white.*"), UUID.randomUUID().toString, 2, new StringDecoder(), new StringDecoder(), actor)
      val consumer = new AkkaConsumer(consumerProps)
      consumer.start()
      receiveWhile(max = timeout/2) {
        case msg: String => msg must_== white
      }
      consumer.stop()
    }
  }

  "AkkaProducer and AkkaConsumer" should {
    s"send $messages messages with Akka producer and consume them back" in new AkkaTestkit {
      val testTopic = UUID.randomUUID().toString
      val testGroupId = UUID.randomUUID().toString
      val producer = system.actorOf(Props[KafkaAkkaProducer])

      producer !(testTopic, BROKERS)
      (1 to messages).foreach(i => producer ! UUID.randomUUID().toString)

      val actor = system.actorOf(Props(new ConsumerActor(testActor)))
      val consumerProps = AkkaConsumerProps.forSystem(system, ZOOKEEPER_CONNECT, testTopic, testGroupId, 1, new StringDecoder(), new StringDecoder(), actor)
      val consumer = new AkkaConsumer(consumerProps)
      consumer.start()
      receiveN(messages, timeout)

      //produce 50 more messages
      (1 to messages / 2).foreach(i => producer ! UUID.randomUUID().toString)
      receiveN(messages / 2, timeout)
      consumer.stop()
    }
  }

  "AkkaProducer and AkkaBatchConsumer" should {
    "send and receive 100 messages in one batch" in new AkkaTestkit {
      val testTopic = UUID.randomUUID().toString
      val testGroupId = UUID.randomUUID().toString
      val producer = system.actorOf(Props[KafkaAkkaProducer])

      producer ! testTopic -> BROKERS
      (1 to messages).foreach(i => producer ! UUID.randomUUID().toString)

      val actor = system.actorOf(Props(new BatchActor(testActor, (xs: IndexedSeq[_]) => {
        xs.length must_== messages
      })))
      val consumerProps = batchConsumerProps(system, testTopic, testGroupId, actor, batchTimeout)
      val consumer = new AkkaBatchConsumer(consumerProps)
      consumer.start()
      //we expect EXACTLY 1 message within the given time
      within(timeout) {
        receiveN(1)
        expectNoMsg()
      }
      consumer.stop()
    }

    "ensure that non-full batches won't be received until batch timeout comes" in new AkkaTestkit {
      val testTopic = UUID.randomUUID().toString
      val testGroupId = UUID.randomUUID().toString
      val producer = system.actorOf(Props[KafkaAkkaProducer])

      producer ! testTopic -> BROKERS

      val halfMessages = messages / 2
      val actor = system.actorOf(Props(new BatchActor(testActor, (xs: IndexedSeq[_]) => {
        xs.length must_== halfMessages
      })))
      val consumerProps = batchConsumerProps(system, testTopic, testGroupId, actor, batchTimeout)
      val consumer = new AkkaBatchConsumer(consumerProps)
      consumer.start()
      //produce halfMessages new messages and make sure they are not consumed until the batchTimeout comes
      within(batchTimeout) {
        (1 to halfMessages).foreach(i => producer ! UUID.randomUUID().toString)
        expectNoMsg()
      }
      //now we should receive exactly one batch of size halfMessages
      receiveN(1)
      expectNoMsg()
      consumer.stop()
    }
  }

  private def batchConsumerProps(system: ActorSystem, topic: String, group: String, actor: ActorRef, batchTimeout: Timeout): AkkaBatchConsumerProps[String, String, String, IndexedSeq[String]] = {
    AkkaBatchConsumerProps.forSystem[String, String, String, IndexedSeq[String]](
      system = system,
      zkConnect = ZOOKEEPER_CONNECT,
      topic = topic,
      group = group,
      streams = 1,
      keyDecoder = new StringDecoder(),
      msgDecoder = new StringDecoder(),
      receiver = actor,
      batchHandler = (batch: IndexedSeq[String]) => batch,
      batchSize = messages,
      batchTimeout = batchTimeout)
  }
}
