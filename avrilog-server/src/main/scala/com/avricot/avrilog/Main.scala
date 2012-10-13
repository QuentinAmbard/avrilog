
package com.avricot.avrilog

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.actor.Terminated
import akka.util.duration._

object Main {
  def main(args: Array[String]) = {

    val system = ActorSystem()
    system.actorOf(Props(new TraceActor)) ! "start"
  }
}

class TraceActor extends Actor {
  val system = ActorSystem()
  val myActor = system.actorOf(Props(new TraceActor))
  def receive = {
    case Terminated => {
      system.scheduler.scheduleOnce(5000 milliseconds) {
        createAndInitWatchedActor
      }
    }
    case _ => createAndInitWatchedActor
  }

  def createAndInitWatchedActor {
    val actor = context.actorOf(Props(new ConsumerActor(RabbitMQConfig.queue, println(_))))
    context.watch(actor)
    actor ! "start"
  }
}