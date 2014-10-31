/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ly.stealth.testing

import java.util.concurrent.{ExecutionException, TimeUnit}

import org.apache.kafka.clients.producer.{Callback, RecordMetadata, ProducerRecord}
import org.apache.kafka.common.errors.RecordTooLargeException
import org.specs2.mutable._
import java.util.UUID
import kafka.consumer._
import kafka.producer._
import kafka.utils._
import kafka.akka._
import akka.actor.{Props, ActorSystem}
import akka.routing.RoundRobinRouter

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Promise}

class KafkaSpec extends Specification with Logging with BaseSpec {


  "Simple Producer and Consumer" should {
    "send string to broker and consume that string back" in {
      val testMessage = UUID.randomUUID().toString
      val testTopic = UUID.randomUUID().toString
      val groupId_1 = UUID.randomUUID().toString

      var testStatus = false

      info("starting sample broker testing")
      val producer = new KafkaProducer(testTopic,"192.168.86.10:9092")
      producer.send(testMessage)

      val consumer = new KafkaConsumer(testTopic,groupId_1,"192.168.86.5:2181")

      def exec(binaryObject: Array[Byte]) = {
        val message = new String(binaryObject)
        info("testMessage = " + testMessage + " and consumed message = " + message)
        testMessage must_== message
        consumer.close()
        testStatus = true
      }

      info("KafkaSpec is waiting some seconds")
      consumer.read(exec)
      info("KafkaSpec consumed")

      testStatus must beTrue // we need to get to this point but a failure in exec will fail the test
    }

    "send string to broker and consume that string back in different consumer groups" in {
      val testMessage = UUID.randomUUID().toString
      val testTopic = UUID.randomUUID().toString
      val groupId_1 = UUID.randomUUID().toString
      val groupId_2 = UUID.randomUUID().toString

      var testStatus1 = false
      var testStatus2 = false

      info("starting sample broker testing")
      val producer = new KafkaProducer(testTopic,"192.168.86.10:9092")
      producer.send(testMessage)

      val consumer1 = new KafkaConsumer(testTopic,groupId_1,"192.168.86.5:2181")

      def exec1(binaryObject: Array[Byte]) = {
        val message1 = new String(binaryObject)
        info("testMessage 1 = " + testMessage + " and consumed message 1 = " + message1)
        testMessage must_== message1
        consumer1.close()
        testStatus1 = true
      }

      info("KafkaSpec : consumer 1 - is waiting some seconds")
      consumer1.read(exec1)
      info("KafkaSpec : consumer 1 - consumed")

      val consumer2 = new KafkaConsumer(testTopic,groupId_2,"192.168.86.5:2181")

      def exec2(binaryObject: Array[Byte]) = {
        val message2 = new String(binaryObject)
        info("testMessage 2 = " + testMessage + " and consumed message 2 = " + message2)
        testMessage must_== message2
        consumer2.close()
        testStatus2 = true
      }

      info("KafkaSpec : consumer 2 - is waiting some seconds")
      consumer2.read(exec2)
      info("KafkaSpec : consumer 2 - consumed")

      testStatus2 must beTrue // we need to get to this point but a failure in exec will fail the test
    }
  }

  "New Shiny Producer and Simple Consumer" should {
    "send string to broker and consume that string back" in {

      val testMessage = UUID.randomUUID().toString
      val testTopic = UUID.randomUUID().toString
      val groupId_1 = UUID.randomUUID().toString

      var testStatus = false

      info("starting sample broker testing")
      val newProducer = createNewKafkaProducer("192.168.86.10:9092")
      val record = new ProducerRecord(testTopic, 0, "key".getBytes, testMessage.getBytes)
      newProducer.send(record)

      val consumer = new KafkaConsumer(testTopic, groupId_1, "192.168.86.5:2181")

      def exec(binaryObject: Array[Byte]) = {
        val message = new String(binaryObject)
        info("testMessage = " + testMessage + " and consumed message = " + message)
        testMessage must_== message
        consumer.close()
        testStatus = true
      }

      info("KafkaSpec is waiting some seconds")
      consumer.read(exec)
      info("KafkaSpec consumed")

      testStatus must beTrue // we need to get to this point but a failure in exec will fail the test
    }

  }

  "New Shiny Producer" should {
    "get the same the data in callback and send request result" in {
      val testMessage = UUID.randomUUID().toString
      val testTopic = UUID.randomUUID().toString

      info("starting sample broker testing")
      val newProducer = createNewKafkaProducer("192.168.86.10:9092", acks = 0)
      val record = new ProducerRecord(testTopic, 0, "key".getBytes, testMessage.getBytes)

      val p = Promise[(RecordMetadata, Exception)]()
      val response = newProducer.send(record, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          p.success((metadata, exception))
        }
      })

      val reqMetadata = response.get(2, TimeUnit.SECONDS)
      val (callbackMeta, callbackEx) = Await.result(p.future, new FiniteDuration(3, TimeUnit.SECONDS))
      reqMetadata.offset() must_== callbackMeta.offset()
      reqMetadata.topic() must_== callbackMeta.topic()
      reqMetadata.partition() must_== callbackMeta.partition()
      callbackEx must_== null
    }

    "return exception in callback when buffer cannot accept message" in {
      val testMessage = UUID.randomUUID().toString
      val testTopic = UUID.randomUUID().toString

      info("starting sample broker testing")
      val newProducer = createNewKafkaProducer("192.168.86.10:9092", acks = 1, bufferSize = 1L)
      val record = new ProducerRecord(testTopic, 0, "key".getBytes, testMessage.getBytes)

      val p = Promise[(RecordMetadata, Exception)]()
      val response = newProducer.send(record, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          p.success((metadata, exception))
        }
      })

      response.get(2, TimeUnit.SECONDS) must throwA[ExecutionException]
      val (callbackMeta, callbackEx) = Await.result(p.future, new FiniteDuration(1, TimeUnit.SECONDS))
      callbackMeta must_== null
      callbackEx must_!= null
      callbackEx.getClass must_== classOf[RecordTooLargeException]
    }
  }

  "Akka Producer and Consumer" should {
    "send string to broker and consume that string back in different consumer groups" in {
      val testMessage = UUID.randomUUID().toString
      val testTopic = UUID.randomUUID().toString
      val groupId_1 = UUID.randomUUID().toString
      val groupId_2 = UUID.randomUUID().toString

      var testStatus1 = false
      var testStatus2 = false

      info("starting akka producertesting")
      val system = ActorSystem("testing")

      val actorCount = 1

      val producer = system.actorOf(Props[KafkaAkkaProducer].withRouter(RoundRobinRouter(actorCount)), "router")

      1 to actorCount foreach { i =>(
        producer ! (testTopic,"192.168.86.10:9092"))
      }

      producer ! testMessage

      val consumer1 = new KafkaConsumer(testTopic,groupId_1,"192.168.86.5:2181")

      def exec1(binaryObject: Array[Byte]) = {
        val message1 = new String(binaryObject)
        info("testMessage 1 = " + testMessage + " and consumed message 1 = " + message1)
        testMessage must_== message1
        consumer1.close()
        testStatus1 = true
      }

      info("KafkaSpec : consumer 1 - is waiting some seconds")
      consumer1.read(exec1)
      info("KafkaSpec : consumer 1 - consumed")

      val consumer2 = new KafkaConsumer(testTopic,groupId_2,"192.168.86.5:2181")

      def exec2(binaryObject: Array[Byte]) = {
        val message2 = new String(binaryObject)
        info("testMessage 2 = " + testMessage + " and consumed message 2 = " + message2)
        testMessage must_== message2
        consumer2.close()
        testStatus2 = true
      }

      info("KafkaSpec : consumer 2 - is waiting some seconds")
      consumer2.read(exec2)
      info("KafkaSpec : consumer 2 - consumed")

      testStatus2 must beTrue // we need to get to this point but a failure in exec will fail the test
    }
  }
}