package com.test.app

import akka.actor.{ActorRef, ActorSystem, DeadLetter, Props}
import akka.stream.ActorMaterializer
import com.test.actor._
import com.test.config.Configuration
import com.test.model.command.{ConsumeExceptions, InitExceptionActors, Start}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Starter()(implicit system: ActorSystem, ec: ExecutionContext, materializer: ActorMaterializer) extends Configuration {
  private val logger = LoggerFactory.getLogger(classOf[Starter])

  val exceptionManagerActor: ActorRef = system.actorOf(Props(new ExceptionManagerActor()))

  def init(): Unit = {
    exceptionManagerActor ! InitExceptionActors

    system.scheduler.schedule(2.second, 60.seconds) {
      logger.info("started consuming messages")
      exceptionManagerActor ! ConsumeExceptions
    }
  }
}