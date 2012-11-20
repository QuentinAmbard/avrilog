package com.avricot.avrilog.model

import org.msgpack.annotation.Message
import org.joda.time.DateTime
import com.avricot.avrilog.serialize._
import scala.collection.mutable.Map
import org.msgpack.AvrilogMPack

/**
 * User, built from clients message (compressed with msg pack), embedded in a trace.
 */
case class User(var id: String, var firstname: String, var lastname: String, var email: String, var groupId: String, var ip: String) {
  def this() = this(null, null, null, null, null, null)
}

/**
 * Client trace, built from clients message (compressed with msg pack)
 */
case class ClientTrace(var id: Array[Byte], var applicationName: String, var entityId: String, var category: String, var info: String, var clientDate: DateTime, var sign: Boolean, var horodate: Boolean, var user: User, var data: Map[String, String]) { //
  def this() = this(null, null, null, null, null, null, false, false, null, Map[String, String]()) //
  def serialize(): Array[Byte] = {
    AvrilogMPack.write(this)
  }
}