package com.avricot.avrilog.rabbitmq
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.util.duration._
import akka.actor.ActorLogging
import java.util.Map

class ConsumerManager(queuName: String, durable: Boolean, exclusive: Boolean, autodelete: Boolean, params: Map[String, Object], f: (Message) => Any) extends Actor with ActorLogging {
  def receive = {
    case Error => {
      log.info("oops, consumer crashed ! Scheduling restart in 5 seconds...")
      ActorSystem().scheduler.scheduleOnce(5 seconds, self, Start)
    }
    case Start => {
      log.info("start new consumer actor")
      val actor = context.actorOf(Props(new ConsumerActor(queuName, durable, exclusive, autodelete, params, { msg: Message => f(msg) })))
      val future = actor ! Listen
    }
  }
}
