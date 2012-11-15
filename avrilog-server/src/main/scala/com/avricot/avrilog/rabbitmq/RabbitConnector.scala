package com.avricot.avrilog.rabbitmq

import com.rabbitmq.client.ConnectionFactory

object RabbitConnector {
  val factory = new ConnectionFactory()
  factory.setHost(RabbitMQConfig.host)
  factory.setPort(RabbitMQConfig.port)

  def getNewConnection = factory.newConnection()
}