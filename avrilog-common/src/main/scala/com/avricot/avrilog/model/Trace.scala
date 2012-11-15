package com.avricot.avrilog.model
import org.apache.hadoop.hbase.client.Result
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import org.msgpack.annotation.Message
import org.msgpack.MessagePack
import scala.collection.mutable.Map
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonInclude
import scala.reflect.BeanProperty
import scala.reflect.Manifest.ClassTypeManifest
import scala.collection.immutable.List
import scala.collection.mutable.Seq
import scala.collection.mutable.HashMap
import com.avricot.horm.HormObject
import com.avricot.horm.HormObject
import com.avricot.horm.HormBaseObject
import com.avricot.avrilog.json.JsonMapper
import com.avricot.avrilog.json.JsonObj
import org.msgpack.AvrilogMPack
import com.avricot.avrilog.json.Binary
//import com.codahale.jerkson.Json._

/**
 * A trace, stored in the database.
 */
case class Trace(content: TraceContent, timestampingContent: Binary = null, signContent: Binary = null) extends HormBaseObject with JsonObj {
  def getHBaseId() = content.id.bytes
  def serialize(): Array[Byte] = {
    AvrilogMPack.write(this)
  }
}

object Trace extends HormObject[Trace] {
  def apply(content: TraceContent, timestampingContent: Array[Byte], signContent: Array[Byte]) = {
    new Trace(content, Binary(timestampingContent), Binary(signContent))
  }
}

case class TraceContent(id: Binary, entityId: String, category: String, info: String, clientDate: DateTime, sign: Boolean, horodate: Boolean, user: User, data: Map[String, String], date: DateTime = null) extends JsonObj {
  //def apply(clientTrace: ClientTrace) = (Binary(clientTrace.id), clientTrace.entityId, clientTrace.category, clientTrace.info, clientTrace.clientDate, clientTrace.sign, clientTrace.horodate, clientTrace.user, clientTrace.data, new DateTime())
}

/**
 * Trance content builder. Can't be defined as a constructor in the TraceContent because of json deserialization issues.
 */
object TraceContent {
  def apply(clientTrace: ClientTrace) = {
    new TraceContent(Binary(clientTrace.id), clientTrace.entityId, clientTrace.category, clientTrace.info, clientTrace.clientDate, clientTrace.sign, clientTrace.horodate, clientTrace.user, clientTrace.data, new DateTime())
  }
}
