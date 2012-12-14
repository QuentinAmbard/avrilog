package com.avricot.avrilog.trace

import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.duration._
import com.avricot.avrilog.model.Trace
import com.avricot.avrilog.model.ClientTrace
import org.msgpack.AvrilogMPack
import com.avricot.avrilog.model.TraceContent
import org.slf4j.LoggerFactory
import java.io.IOException
import akka.actor.actorRef2Scala
import com.avricot.avrilog.rabbitmq.ConsumerManager
import com.avricot.avrilog.rabbitmq.RabbitMQConfig
import com.avricot.avrilog.rabbitmq.Message
import com.avricot.avrilog.crypto.sign.Sign
import com.avricot.avrilog.crypto.timestamp.Timestamping
import com.avricot.avrilog.rabbitmq.Start
import com.avricot.avrilog.rabbitmq.ConsumerException

class TraceConsumer {
  val logger = LoggerFactory.getLogger(classOf[TraceConsumer])

  def handleTrance(msg: Message): Unit = {
    try {
      throw new ConsumerException("TEST ERROR - ACK NOT SENT.", null)
      val clientTrace = AvrilogMPack.read[ClientTrace](msg.body);
      val traceContent = TraceContent(clientTrace)
      val traceContentBytes = traceContent.toJson.getBytes()
      if (Trace.find(traceContent.id).isDefined) {
        logger.error("Trace {} is already defined. This might be only happen if you restart your server after a crash : data is saved but acks have not reached the rabbitmq server. trace skipped", traceContent.toJson)
      } else {
        try {
          val trace = clientTrace match {
            case c if c.sign && c.horodate => Trace(traceContent, null, Sign.signWithRemoteTimestamp(traceContentBytes))
            case c if c.sign => Trace(traceContent, null, Sign.sign(traceContentBytes))
            case c if c.horodate => Trace(traceContent, Timestamping.timestamp(traceContentBytes), null)
            case _ => Trace(traceContent)
          }
          try {
            Trace.save(trace)
            //One it's saved, send the ack to rabbitmq.
            if (!RabbitMQConfig.Trace.autoAck) {
              try {
                msg.sendAck();
              } catch {
                case e: IOException => throw new ConsumerException("error trying to ack the msg. rabbitMQ is probably down, or channel has been closed after a parallel ConsumerException.", e)
              }
            }
          } catch {
            case e: Throwable => throw new ConsumerException("error while trying to save the trace in db. Trace content : " + trace.toJson, e)
          }
        } catch {
          case e: Throwable => throw new ConsumerException("error while trying to horodate/sign trace. Trace content : " + traceContent.toJson, e)
        }
      }
    } catch {
      case e: ConsumerException => logger.error("oOOOOooooooooooooooooo"); throw e
      case e: IOException => throw new ConsumerException("IO exception. Your incoming message might not verify the protocol (field name much match to read trace using messagePack). body: " + msg.body + ", delivery Tag: " + msg.deliveryTag, e)
      case e: Throwable => throw new ConsumerException("Unknown exception.", e)
    }
  }
  val system = ActorSystem()
  system.actorOf(Props(new ConsumerManager(RabbitMQConfig.Trace.queue, RabbitMQConfig.Trace.durable, RabbitMQConfig.Trace.exclusive, RabbitMQConfig.Trace.autodelete, RabbitMQConfig.Trace.autoAck, RabbitMQConfig.Trace.haConfig, handleTrance))) ! Start

}