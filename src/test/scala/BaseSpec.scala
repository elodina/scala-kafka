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

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer => NewKafkaProducer}
import org.apache.kafka.clients.producer.ProducerConfig

trait BaseSpec {
  def createNewKafkaProducer(brokerList: String,
                             acks: Int = -1,
                             metadataFetchTimeout: Long = 3000L,
                             blockOnBufferFull: Boolean = true,
                             bufferSize: Long = 1024L * 1024L,
                             retries: Int = 0): NewKafkaProducer[Array[Byte], Array[Byte]] = {

    val producerProps = new Properties()
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList)
    producerProps.put(ProducerConfig.ACKS_CONFIG, acks.toString)
    producerProps.put(ProducerConfig.METADATA_FETCH_TIMEOUT_CONFIG, metadataFetchTimeout.toString)
    producerProps.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, blockOnBufferFull.toString)
    producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferSize.toString)
    producerProps.put(ProducerConfig.RETRIES_CONFIG, retries.toString)
    producerProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "100")
    producerProps.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, "200")
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer")
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer")

    new NewKafkaProducer[Array[Byte], Array[Byte]](producerProps)
  }
}
