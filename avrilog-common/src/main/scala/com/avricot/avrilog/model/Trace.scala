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
//import com.codahale.jerkson.Json._

@Message
case class User(var id: String, var firstname: String, var lastname: String, var email: String, var groupId: String, var ip: String) {
  def this() = this(null, null, null, null, null, null)
}

@Message
case class ClientTrace(var id: Array[Byte], var category: String, var info: String, var clientDate: DateTime, var sign: Boolean, var horodate: Boolean, var user: User, var data: Map[String, String]) { //
  def this() = this(null, null, null, null, false, false, null, Map[String, String]()) //
  def serialize(): Array[Byte] = {
    AvrilogMPack.write(this)
  }
}

case class Trace(id: Array[Byte], category: String, info: String, clientDate: DateTime, sign: Boolean, horodate: Boolean, user: User, data: Map[String, String], date: DateTime = null, timestampingContent: Array[Byte] = null, signContent: Array[Byte] = null) {
  def this(clientTrace: ClientTrace) = {
    this(clientTrace.id, clientTrace.category, clientTrace.info, clientTrace.clientDate, clientTrace.sign, clientTrace.horodate, clientTrace.user, clientTrace.data, new DateTime(), null, null)
  }
  def this(clientTrace: ClientTrace, timestampingContent: Array[Byte], signContent: Array[Byte]) = {
    this(clientTrace.id, clientTrace.category, clientTrace.info, clientTrace.clientDate, clientTrace.sign, clientTrace.horodate, clientTrace.user, clientTrace.data, new DateTime(), timestampingContent, signContent)
  }
  def toJson() = {
    Trace.mapper.writeValueAsString(this)
  }
}

object Trace extends HBaseObject[Trace]("trace") {
  val mapper = new ObjectMapper()
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
  mapper.registerModule(DefaultScalaModule)

  /**
   * Return a trace by it's id.
   */
  def findLast(id: String): Option[Trace] = {
    val get = new Get(Bytes.toBytes(id))
    val result = table.get(get)
    getFromResult(result)
  }

  /**
   * Build a trace from a result
   */
  private def getFromResult(result: Result): Option[Trace] = {
    if (result.isEmpty()) {
      return None
    }
    //    val trace = new Trace(new DateTime(), null, null)
    //    trace.info = "";
    //val trace = new Trace
    //trace.id = getStr(result, "info", "id")
    // trace.category = getStr(result, "info", "category")
    //    trace.clientDate = getDate(result, "info", "clientDate")
    //    trace.date = getDate(result, "info", "date")
    // trace.info = getStr(result, "info", "info")

    // trace.data = getColumnFamilyAsMap(result, "data")

    //    trace.user.userId = getStr(result, "user", "userId")
    //    trace.user.firstname = getStr(result, "user", "firstname")
    //    trace.user.lastname = getStr(result, "user", "lastname")
    //    trace.user.email = getStr(result, "user", "email")
    //    trace.user.groupId = getStr(result, "user", "groupId")
    //    trace.user.user = User(userId, firstname, lastname, email, groupId)
    null //Option(trace)
  }

}
