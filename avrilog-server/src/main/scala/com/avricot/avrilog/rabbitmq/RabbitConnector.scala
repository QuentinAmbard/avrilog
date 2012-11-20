package com.avricot.avrilog.rabbitmq

import com.rabbitmq.client.ConnectionFactory

object RabbitConnector {
  val factory = new ConnectionFactory()
  factory.setHost(RabbitMQConfig.host)
  factory.setPort(RabbitMQConfig.port)
  factory.setUsername(RabbitMQConfig.username)
  factory.setPassword(RabbitMQConfig.password)

  def getNewConnection = factory.newConnection()
}