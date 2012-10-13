package com.avricot.avrilog

import com.typesafe.config.ConfigFactory

object RabbitMQConfig {
  val config = ConfigFactory.load()
  val port = config.getInt("rabbitmq.port")
  val host = config.getString("rabbitmq.host")
  val queue = config.getString("rabbitmq.queue")
  val exchange = config.getString("rabbitmq.exchange")
}