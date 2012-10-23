package com.avricot.avrilog

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.actor.Terminated
import akka.util.duration._
import akka.actor.ActorLogging
import akka.pattern.ask
import akka.util.Timeout
import com.avricot.avrilog.model.Trace
import com.avricot.avrilog.sign.Sign
import com.avricot.avrilog.model.ClientTrace
import org.joda.time.DateTime
import com.avricot.avrilog.stockage.HBaseStockage
import com.avricot.avrilog.timestamp.Timestamping
import org.msgpack.AvrilogMPack

class TraceConsumer {
  def handleTrance(msg: Message) = {
    msg.sendAck();
    val clientTrace = AvrilogMPack.read[ClientTrace](msg.body);
    var trace = new Trace(clientTrace)
    if (clientTrace.horodate) {
      trace = trace.copy(timestampingContent = Timestamping.timestamp(trace.toJson))
    }
    if (clientTrace.sign) {
      trace = trace.copy(signContent = Sign.sign())
    }
    HBaseStockage.store(trace)
    println(trace.toJson)
    println(trace.category);
    println(msg.deliveryTag);
  }

  val system = ActorSystem()
  system.actorOf(Props(new ConsumerManager(handleTrance, RabbitMQConfig.queue))) ! Start
}