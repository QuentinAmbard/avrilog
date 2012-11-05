package com.avricot.avrilog.model
import org.apache.hadoop.hbase.client.Result
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import org.msgpack.annotation.Message
import org.msgpack.MessagePack
import scala.collection.mutable.Map
import org.msgpack.AvrilogMPack
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonInclude
import scala.reflect.BeanProperty
import scala.reflect.Manifest.ClassTypeManifest
import scala.collection.immutable.List
import scala.collection.mutable.Seq
import scala.collection.mutable.HashMap
//import com.codahale.jerkson.Json._

/**
 * User, build from clients message (compressed with msg pack), embedded in a trace.
 */
@Message
case class User(var id: String, var firstname: String, var lastname: String, var email: String, var groupId: String, var ip: String) {
  def this() = this(null, null, null, null, null, null)
}

/**
 * Client trace, build from clients message (compressed with msg pack)
 */
@Message
case class ClientTrace(var id: Array[Byte], var category: String, var info: String, var clientDate: DateTime, var sign: Boolean, var horodate: Boolean, var user: User, var data: Map[String, String]) { //
  def this() = this(null, null, null, null, false, false, null, Map[String, String]()) //
  def serialize(): Array[Byte] = {
    AvrilogMPack.write(this)
  }
}

/**
 * A trace, stored in the database.
 */
case class Trace(id: Array[Byte], category: String, info: String, clientDate: DateTime, sign: Boolean, horodate: Boolean, user: User, data: Map[String, String], date: DateTime = null, timestampingContent: Array[Byte] = null, signContent: Array[Byte] = null) extends HormObject {
  def this(clientTrace: ClientTrace) = {
    this(clientTrace.id, clientTrace.category, clientTrace.info, clientTrace.clientDate, clientTrace.sign, clientTrace.horodate, clientTrace.user, clientTrace.data, new DateTime(), null, null)
  }
  def this(clientTrace: ClientTrace, timestampingContent: Array[Byte], signContent: Array[Byte]) = {
    this(clientTrace.id, clientTrace.category, clientTrace.info, clientTrace.clientDate, clientTrace.sign, clientTrace.horodate, clientTrace.user, clientTrace.data, new DateTime(), timestampingContent, signContent)
  }
  def toJson() = {
    Trace.mapper.writeValueAsString(this)
  }
  def getHBaseId() = id
}

object Trace extends HormObject[Trace] {
  val mapper = new ObjectMapper()
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
  mapper.registerModule(DefaultScalaModule)

}
