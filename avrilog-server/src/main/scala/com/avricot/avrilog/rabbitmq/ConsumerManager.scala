package com.avricot.avrilog.rabbitmq
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.util.duration._
import akka.actor.ActorLogging
import java.util.Map

class ConsumerManager(queuName: String, durable: Boolean, exclusive: Boolean, autodelete: Boolean, autoAck: Boolean, params: Map[String, Object], f: (Message) => Any) extends Actor with ActorLogging {
  def receive = {
    case Error => {
      log.error("Consumer crashed ! Scheduling restart in 5 seconds...")
      ActorSystem().scheduler.scheduleOnce(5 seconds, self, Start)
    }
    case ConsumerError => {
      log.error("ConsumerException ! Server is stopped. Scheduling restart in 1 hour... ")
      ActorSystem().scheduler.scheduleOnce(1 hour, self, Start)
    }
    case Start => {
      log.info("start new consumer actor")
      val actor = context.actorOf(Props(new ConsumerActor(queuName, durable, exclusive, autodelete, autoAck, params, { msg: Message => f(msg) })))
      val future = actor ! Listen
    }
  }
}
