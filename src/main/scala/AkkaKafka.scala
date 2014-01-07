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

package kafka.akka

import scala.collection.JavaConverters._
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport
import org.apache.thrift.{TSerializer, TDeserializer}
import akka.actor.{Actor, Props, ActorSystem}
import akka.routing.RoundRobinRouter
import kafka.utils.Logging
import java.util.concurrent.atomic._
import kafka.producer._

class KafkaAkkaProducer extends Actor with Logging {
  private val producerCreated = new AtomicBoolean(false)

  var producer: KafkaProducer = null

  def init(topic: String, zklist: String) = {
    if (producerCreated.getAndSet(true)) {
      throw new RuntimeException(this.getClass.getSimpleName + " this kafka akka actor has a producer already")  
    }
    
    producer = new KafkaProducer(topic,zklist)
  }

  def receive = {
    case t: (String, String) ⇒ {
        init(t._1, t._2)
      }
    case msg: String ⇒
      {
        producer.send(msg)
      }
  }
}