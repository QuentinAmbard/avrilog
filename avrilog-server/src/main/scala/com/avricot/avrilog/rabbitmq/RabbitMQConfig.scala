package com.avricot.avrilog.rabbitmq

import com.typesafe.config.ConfigFactory
import java.util.HashMap

object RabbitMQConfig {
  val config = ConfigFactory.load()
  val port = config.getInt("rabbitmq.port")
  val host = config.getString("rabbitmq.host")
  val password = config.getString("rabbitmq.password")
  val username = config.getString("rabbitmq.username")

  object Trace {
    val queue = config.getString("rabbitmq.trace.queue")
    val exchange = config.getString("rabbitmq.trace.exchange")
    val durable = config.getBoolean("rabbitmq.trace.durable")
    val exclusive = config.getBoolean("rabbitmq.trace.exclusive")
    val autodelete = config.getBoolean("rabbitmq.trace.autodelete")
    val haMode = config.getString("rabbitmq.trace.ha-mode")
    val haParams = config.getString("rabbitmq.trace.ha-params")
    val haConfig = new HashMap[String, Object]()
    if (haMode != "") {
      haConfig.put("ha-mode", haMode)
    }
    if (haParams != "") {
      haConfig.put("ha-params", haParams)
    }
  }
}