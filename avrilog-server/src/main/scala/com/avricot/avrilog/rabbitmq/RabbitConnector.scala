package com.avricot.avrilog.rabbitmq

import com.rabbitmq.client.ConnectionFactory
import java.util.ArrayList
import com.rabbitmq.client.Address
import org.slf4j.LoggerFactory

object RabbitConnector {
  val logger = LoggerFactory.getLogger(RabbitConnector.getClass())

  val factory = new ConnectionFactory()
  //Simple node mode
  val addresses = new ArrayList[Address]()
  val hosts = RabbitMQConfig.host.split(",")
  hosts.foreach(s => {
    val host = s.substring(0, s.indexOf(":"))
    val port = s.substring(s.indexOf(":") + 1)
    addresses.add(new Address(host, port.toInt))
    logger.debug("rabbitmq node address : {}:{}", host, port)
  })
  factory.setUsername(RabbitMQConfig.username)
  factory.setPassword(RabbitMQConfig.password)
  val addressesArray = addresses.toArray(new Array[Address](addresses.size()))

  def getNewConnection = factory.newConnection(addressesArray)
}