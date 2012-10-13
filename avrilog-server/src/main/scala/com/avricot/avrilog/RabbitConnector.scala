package com.avricot.avrilog

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Connection

object RabbitConnector {
  val factory = new ConnectionFactory()
  factory.setHost(RabbitMQConfig.host)
  factory.setPort(RabbitMQConfig.port)
  val connection = factory.newConnection()
}