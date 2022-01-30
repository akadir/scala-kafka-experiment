package com.test.actor

import akka.Done
import akka.actor.ActorSystem
import com.test.service.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class TestActor(topic: String, exceptionTopic: String)(implicit system: ActorSystem, ec: ExecutionContext) extends BaseActorWithManuelCommit(Set(topic)) {
  private val logger = LoggerFactory.getLogger(classOf[TestActor])

  protected lazy val kafkaProducerService: KafkaProducerService = KafkaProducerService

  override protected def processMessage(messages: Seq[ConsumerRecord[String, String]]): Future[Done] = {
    Future.sequence(messages.map(message => {
      logger.info(s"--CONSUMED: offset: ${message.offset()} message: ${message.value()}")
      // in actual implementation, some process is done here and if an exception occurs, the message is sent to the same topic as seen below
      sendToExceptionTopic(Instant.now().toEpochMilli)
      Thread.sleep(1000)
      Future(Done)
    })).transformWith(_ => Future(Done))
  }

  private def sendToExceptionTopic(time: Long): Unit = {
    kafkaProducerService.produce(exceptionTopic, s"$time")
  }
}
