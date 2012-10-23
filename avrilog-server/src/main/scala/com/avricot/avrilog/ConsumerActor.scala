package com.avricot.avrilog

import com.rabbitmq.client.QueueingConsumer
import akka.actor.Props
import akka.actor.Actor
import com.rabbitmq.client.AMQP
import akka.actor.ActorSystem
import com.rabbitmq.client.Channel
import akka.actor.ActorLogging
import com.rabbitmq.client.ShutdownSignalException
import com.rabbitmq.client.Connection
import scala.collection.mutable.HashSet
import akka.util.duration._
import akka.actor.ActorRef
import scala.util.control.Exception

case class Listen
case class Start
case class Error

case class Message(body: Array[Byte], deliveryTag: Long, channel: Channel, actorRef: ActorRef) { def sendAck() = channel.basicAck(deliveryTag, false) }

class ConsumerActor(queuName: String, f: (Message) => Any) extends Actor with ActorLogging {
  def receive = {
    case Listen => {
      try {
        val connection = RabbitConnector.getNewConnection
        val channel = connection.createChannel()
        log.info("enter receging")
        val consumer = new QueueingConsumer(channel)
        channel.queueDeclare(queuName, true, false, false, null)
        channel.basicConsume(queuName, false, consumer)
        log.info("queue {} declared and consumed", queuName)
        while (true) {
          // wait for the message
          val delivery = consumer.nextDelivery()
          log.info("getting next deliv")
          val msg = Message(delivery.getBody(), delivery.getEnvelope().getDeliveryTag(), channel, sender)
          // send the message to the provided callback function
          // and execute this in a subactor
          context.actorOf(Props(new Actor {
            def receive = {
              case msg: Message => {
                try { f(msg) } catch {
                  case e: Exception => log.error(e, "can't parse message")
                }
              }
              case _ => log.error("unkonwn message")
            }
          })) ! msg
        }
      } catch {
        case e: ShutdownSignalException => log.error(e, "ShutdownSignalException")
        case e: Exception => log.error(e, "Unkown exception")
      } finally {
        sender ! Error
      }
    }
  }
}