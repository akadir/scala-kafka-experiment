package com.test.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.test.config.Configuration
import com.test.model.command.{ConsumeExceptions, InitExceptionActors, Start, Stop}
import org.slf4j.LoggerFactory

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ExceptionManagerActor(implicit system: ActorSystem, ec: ExecutionContext) extends Actor with Configuration {
  private val logger = LoggerFactory.getLogger(classOf[ExceptionManagerActor])

  private var actorRefSeq = Seq[ActorRef]()

  private def init(): Unit = {

    val testExceptionActor: ActorRef = context.actorOf(Props(new TestActor(exceptionTopic, exceptionTopic)),
      s"test-exception-actor-${UUID.randomUUID()}")

    actorRefSeq = actorRefSeq :+ testExceptionActor
  }

  override def receive: Receive = {
    case ConsumeExceptions =>
      for (actorRef <- actorRefSeq) startScheduledActor(actorRef)
    case InitExceptionActors =>
      init()
  }

  private def startScheduledActor(actorRef: ActorRef): Unit = {
    actorRef ! Start

    context.system.scheduler.scheduleOnce(20.seconds) {
      logger.info("stopping consuming messages")
      actorRef ! Stop
    }
  }
}