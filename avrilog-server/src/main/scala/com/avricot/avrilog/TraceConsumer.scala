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
import org.msgpack.AvrilogMPack
import com.avricot.avrilog.timestamp.Timestamping
import com.avricot.avrilog.model.TraceContent

class TraceConsumer {
  def handleTrance(msg: Message) = {
    msg.sendAck();
    val clientTrace = AvrilogMPack.read[ClientTrace](msg.body);
    val traceContent = new TraceContent(clientTrace)
    val traceContentBytes = traceContent.toJson.getBytes()
    val trace = clientTrace match {
      case c if c.sign && c.horodate => Trace(traceContent, signContent = Sign.signWithRemoteTimestamp(traceContentBytes))
      case c if c.sign => Trace(traceContent, signContent = Sign.sign(traceContentBytes))
      case c if c.horodate => Trace(traceContent, timestampingContent = Timestamping.timestamp(traceContentBytes))
    }
    Trace.save(trace)
    println(trace.content.toJson)
    println(trace.content.category);
    println(msg.deliveryTag);
  }
  val system = ActorSystem()
  system.actorOf(Props(new ConsumerManager(handleTrance, RabbitMQConfig.queue))) ! Start
}