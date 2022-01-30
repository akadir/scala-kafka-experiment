package com.test.service

import akka.kafka.ProducerSettings
import com.test.config.Configuration
import org.apache.kafka.clients.producer.{Producer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

trait KafkaProducerService extends Configuration {
  protected val logger: Logger = LoggerFactory.getLogger(classOf[KafkaProducerService])

  protected val producerSettings: ProducerSettings[String, String] = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)
    .withProperty(ProducerConfig.RETRIES_CONFIG, "5")
    .withProperty(ProducerConfig.ACKS_CONFIG, "1")
    .withProperty(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, "5000")
    .withProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "1000")

  protected val producer: Producer[String, String] = producerSettings.createKafkaProducer()

  def produce(topicName: String, record: String): Unit = {
    Try(producer.send(new ProducerRecord[String, String](topicName, record)).get()) match {
      case Success(metadata) => logger.info(s"----PRODUCED: offset: ${metadata.offset()} message: ${record}")
      case Failure(ex) => logger.error(s"exception while producing message, topic: $topicName record: $record ex: $ex")
    }
  }

}

object KafkaProducerService extends KafkaProducerService