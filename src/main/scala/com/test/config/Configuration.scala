package com.test.config

import com.typesafe.config.{Config, ConfigFactory}

import java.util.Objects
import scala.collection.JavaConverters

trait Configuration {

  private val config: Config = ConfigFactory.load().getConfig("dev")

  val akkaKafkaConf: Config = ConfigFactory.load().getConfig("akka.kafka")
  val producerConfig: Config = akkaKafkaConf.getConfig("producer")
  val consumerConfig: Config = akkaKafkaConf.getConfig("consumer")

  private val kafka: Config = load("kafka")
  val bootstrapServers: String = kafka.getString("bootstrap-servers")
  val consumerGroup: String = kafka.getString("consumer-group")
  val exceptionTopic: String = kafka.getString("test-exception-topic")

  def load(key: String): Config = {
    config.getConfig(key)
  }
}
