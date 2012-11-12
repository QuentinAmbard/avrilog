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
import org.slf4j.LoggerFactory
import java.io.IOException

class TraceConsumer {
  val logger = LoggerFactory.getLogger(classOf[TraceConsumer])

  def handleTrance(msg: Message): Unit = {
    try {
      try {
        msg.sendAck();
      } catch {
        case e: IOException => logger.error("error trying to ack the msg. rabbitMQ is probably down.", e); return
      }
      val clientTrace = AvrilogMPack.read[ClientTrace](msg.body);
      val traceContent = new TraceContent(clientTrace)
      val traceContentBytes = traceContent.toJson.getBytes()
      try {
        val trace = clientTrace match {
          case c if c.sign && c.horodate => Trace(traceContent, signContent = Sign.signWithRemoteTimestamp(traceContentBytes))
          case c if c.sign => Trace(traceContent, signContent = Sign.sign(traceContentBytes))
          case c if c.horodate => Trace(traceContent, timestampingContent = Timestamping.timestamp(traceContentBytes))
        }
        try {
          Trace.save(trace)
        } catch {
          case e: Throwable => logger.error("error while trying to save the trace in db. Trace content : " + trace.toJson, e)
        }
      } catch {
        case e: Throwable => logger.error("error while trying to horodate/sign trace. Trace content : " + traceContent.toJson, e)
      }
    } catch {
      case e: IOException => logger.error("IO exception. Your incoming message might not verify the protocol (field name much match to read trace using messagePack). body: " + msg.body + ", delivery Tag: " + msg.deliveryTag, e);
    }
  }
  val system = ActorSystem()
  system.actorOf(Props(new ConsumerManager(handleTrance, RabbitMQConfig.queue))) ! Start
}