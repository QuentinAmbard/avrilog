package com.avricot.avrilog.rabbitmq

import com.rabbitmq.client.QueueingConsumer
import akka.actor.Props
import akka.actor.Actor
import com.rabbitmq.client.Channel
import akka.actor.ActorLogging
import com.rabbitmq.client.ShutdownSignalException
import akka.util.duration._
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import java.util.Map
import org.slf4j.LoggerFactory
import com.rabbitmq.client.AMQP

case class Listen
case class Start
case class Error
case class ConsumerError

case class Message(body: Array[Byte], deliveryTag: Long, channel: Channel, actorRef: ActorRef) {
  val logger = LoggerFactory.getLogger(Message.getClass())

  def sendAck() = {
    logger.debug("send basic Ack {}", deliveryTag);
    channel.basicAck(deliveryTag, false)
  }

}

class ConsumerActor(queuName: String, durable: Boolean, exclusive: Boolean, autodelete: Boolean, autoAck: Boolean, params: Map[String, Object], f: (Message) => Any) extends Actor with ActorLogging {
  @volatile var listen = true
  def receive = {
    case Listen => {
      try {
        val connection = RabbitConnector.getNewConnection
        val channel = connection.createChannel()
        log.info("creating/registring queue, queuName=" + queuName + ", durable=" + durable + ", exclusive=" + exclusive + ", autodelete=" + autodelete)
        val consumer = new QueueingConsumer(channel)
        channel.queueDeclare(queuName, durable, exclusive, autodelete, params)
        log.info("consume queuName=" + queuName + ", autoAck=" + autoAck)
        channel.basicConsume(queuName, autoAck, consumer)
        log.info("queue {} declared and consumed, waiting for messages.", queuName)
        while (listen) {
          // wait for the message
          val delivery = consumer.nextDelivery()
          log.debug("getting next deliv {}", delivery.getEnvelope().getDeliveryTag())
          val msg = Message(delivery.getBody(), delivery.getEnvelope().getDeliveryTag(), channel, sender)
          if (!listen) {
            log.warning("getting next deliv but programmed to stop (get a ConsumerException from the last execution), just skip it, ack won't be sent.")
          } else {
            // send the message to the provided callback function
            // and execute this in a subactor
            context.actorOf(Props(new Actor {
              def receive = {
                case msg: Message => {
                  //execute the action, catch and log any kind of exception (actor musn't stop listening)
                  try {
                    f(msg)
                  } catch {
                    case e: ConsumerException => log.error(e, "Consumer Exception received, stop the consumer actor."); listen = false;
                    case e: Throwable => log.error(e, "can't parse message. Make sure client and server use the same version."); listen = false;
                  }
                }
                case _ => log.error("unkonwn message")
              }
            })) ! msg
          }
        }
        //Try to close the channel, no matter what.
        Thread.sleep(1000);
        channel.close()
      } catch {
        case e: ShutdownSignalException => log.error(e, "ShutdownSignalException")
        case e: Throwable => log.error(e, "Unkown exception")
      } finally {
        if (!listen) {
          log.error("Stop listening rabbitmq message, report consumer error to the manager")
          sender ! ConsumerError
        } else {
          log.error("Stop listening rabbitmq message, report error to the manager")
          sender ! Error
        }
      }
    }
  }
}