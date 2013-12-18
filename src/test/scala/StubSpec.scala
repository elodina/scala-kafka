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

import org.specs2.mutable._
import java.util.UUID
import kafka.consumer._
import kafka.producer._
import kafka.utils._

class KafkaSpec extends Specification with Logging {
	
  val testMessage = UUID.randomUUID().toString
  val testTopic = UUID.randomUUID().toString

  "Sample" should {
    "send string to broker and consume that string back" in {

      var testStatus = false

      info("starting sample broker testing")
      val producer = new KafkaProducer(testTopic,"192.168.86.10:9092")
      producer.sendString(testMessage)


      val consumer = new KafkaConsumer(testTopic,"groupId","192.168.86.5:2181")

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
}