
package com.avricot.avrilog

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.actor.Terminated
import akka.util.duration._
import akka.actor.ActorLogging
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable.Set
import scala.collection.mutable.HashSet

object Main {
  def main(args: Array[String]) = {

    val system = ActorSystem()
    system.actorOf(Props(new TraceActor)) ! Start
  }
}

case class AckFail(deliveryTag: Long)

class TraceActor extends Actor with ActorLogging {
  def receive = {
    case Error => {
      log.info("oops, consumer crashed !")
      ActorSystem().scheduler.scheduleOnce(5 seconds, self, Start)
    }
    case Start => {
      log.info("start new consumer actor")
      val actor = context.actorOf(Props(new ConsumerActor(RabbitMQConfig.queue, { msg: Message => println(msg.body); println(msg.deliveryTag); msg.sendAck() })))
      context.watch(actor)
      //implicit val timeout = Timeout(0 seconds)
      val future = actor ! Listen
    }
  }
}