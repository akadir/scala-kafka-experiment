package com.test.actor

import akka.Done
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorSystem, OneForOneStrategy}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.test.config.Configuration
import com.test.model.command.{Start, Stop}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class BaseActorWithManuelCommit(topics: Set[String])(implicit system: ActorSystem, ec: ExecutionContext) extends Actor with Configuration {
  private val logger = LoggerFactory.getLogger(classOf[BaseActorWithManuelCommit])

  protected val consumerSettings: ConsumerSettings[String, String] = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(consumerGroup)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
    .withProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "1000")
    .withProperty(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "5000")
    .withProperty(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "5000")
    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

  protected var consumerBase: DrainingControl[Done] = _
  protected val consumer: Source[CommittableMessage[String, String], Consumer.Control] = Consumer
    .committableSource(consumerSettings, Subscriptions.topics(topics))

  protected val decider: Supervision.Decider = {
    case exception: Exception =>
      logger.error(s"An error occurred in actor and Restarted, Exception Type: ${exception.getClass} Exception Message: ${exception.getMessage}")
      Supervision.Restart
  }

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  override val supervisorStrategy: OneForOneStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
    case ex: Exception =>
      logger.error(s"An Error Occurred in actor and Restarted, Exception Type: ${ex.getClass} Exception Message: ${ex.getMessage}")
      Restart
  }

  sys.addShutdownHook {
    logger.info("actor KafkaSource Closed Gracefully")
    if (null != consumerBase) consumerBase.drainAndShutdown()
  }

  override def postStop(): Unit = {
    logger.debug(s"actor is Stopped for ConsumerGroup: $consumerGroup and Topic: $topics")
    super.postStop()
  }

  override def receive: Receive = {
    case Start =>
      consumerBase = consumer
        .mapAsyncUnordered(10) { msg =>
          processMessage(Seq(msg.record)).map(_ => msg.committableOffset)
        }
        .toMat(Committer.sink(CommitterSettings(system)))(DrainingControl.apply)
        .run()

    case Stop =>
      consumerBase.drainAndShutdown().transformWith {
        case Success(value) =>
          logger.info(s"actor stopped")
          Future(value)
        case Failure(ex) =>
          logger.error("error: ", ex)
          Future.failed(ex)
      }
    //Await.result(consumerBase.drainAndShutdown(), 1.minute)
  }

  protected def processMessage(messages: Seq[ConsumerRecord[String, String]]): Future[Done]
}
