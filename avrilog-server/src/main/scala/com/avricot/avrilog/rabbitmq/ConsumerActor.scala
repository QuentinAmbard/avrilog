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

case class Listen
case class Start
case class Error

case class Message(body: Array[Byte], deliveryTag: Long, channel: Channel, actorRef: ActorRef) { def sendAck() = channel.basicAck(deliveryTag, false) }

class ConsumerActor(queuName: String, durable: Boolean, exclusive: Boolean, autodelete: Boolean, autoAck: Boolean, params: Map[String, Object], f: (Message) => Any) extends Actor with ActorLogging {
  def receive = {
    case Listen => {
      try {
        val connection = RabbitConnector.getNewConnection
        val channel = connection.createChannel()
        log.info("enter receging")
        val consumer = new QueueingConsumer(channel)
        channel.queueDeclare(queuName, durable, exclusive, autodelete, params)
        channel.basicConsume(queuName, autoAck, consumer)
        log.info("queue {} declared and consumed, waiting for messages.", queuName)
        while (true) {
          // wait for the message
          val delivery = consumer.nextDelivery()
          log.debug("getting next deliv")
          val msg = Message(delivery.getBody(), delivery.getEnvelope().getDeliveryTag(), channel, sender)
          // send the message to the provided callback function
          // and execute this in a subactor
          context.actorOf(Props(new Actor {
            def receive = {
              case msg: Message => {
                //execute the action, catch and log any kind of exception (actor musn't stop listening)
                try { f(msg) } catch {
                  case e: Throwable => log.error(e, "can't parse message. Make sure client and server use the same version.")
                }
              }
              case _ => log.error("unkonwn message")
            }
          })) ! msg
        }
      } catch {
        case e: ShutdownSignalException => log.error(e, "ShutdownSignalException")
        case e: Throwable => log.error(e, "Unkown exception")
      } finally {
        sender ! Error
      }
    }
  }
}