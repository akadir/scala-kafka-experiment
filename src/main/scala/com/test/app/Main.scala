package com.test.app

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.test.config.Configuration
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

object Main extends App with Configuration {

  private val logger = LoggerFactory.getLogger("Main")

  implicit val system: ActorSystem = ActorSystem(name = "test-consumer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val starter = new Starter()
  starter.init()
  logger.info("Test Consumer Started")
}
