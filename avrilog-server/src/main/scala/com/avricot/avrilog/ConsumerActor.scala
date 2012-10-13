package com.avricot.avrilog

import com.rabbitmq.client.QueueingConsumer
import akka.actor.Props
import akka.actor.Actor
import com.rabbitmq.client.AMQP
import akka.actor.ActorSystem
import com.rabbitmq.client.Channel

class ConsumerActor(queuName: String, f: (String) => Any) extends Actor {
  def receive = {
    case _ => startReceving
  }

  def startReceving = {
    //Setup trace queue consumer.
    val channel = RabbitConnector.connection.createChannel()
    val consumer = new QueueingConsumer(channel)
    channel.queueDeclare(queuName, true, false, false, null)
    channel.basicConsume(queuName, false, consumer)
    while (true) {
      // wait for the message
      val delivery = consumer.nextDelivery()
      val msg = new String(delivery.getBody())

      // send the message to the provided callback function
      // and execute this in a subactor
      context.actorOf(Props(new Actor {
        def receive = {
          case some: String => f(some);
          case _ => {}
        }
      })) ! msg
    }
  }
}