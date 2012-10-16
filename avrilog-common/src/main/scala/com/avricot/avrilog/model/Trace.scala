package com.avricot.avrilog.model
import org.apache.hadoop.hbase.client.Result
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes
import org.msgpack.annotation.Message
import org.msgpack.MessagePack
import org.msgpack.ScalaMPack;

import com.avricot.avrilog.serialize.DateTimeTemplate
import scala.collection.mutable.Map

@Message
case class User(var id: String, var firstname: String, var lastname: String, var email: String, var groupId: String) {
  def this() = this(null, null, null, null, null)
}

@Message
case class Trace(var id: String, var category: String, var info: String, var date: DateTime, var clientDate: DateTime, var user: User, var data: Map[String, String]) {
  def this() = this(null, null, null, null, null, null, Map[String, String]())

  def serialize(): Array[Byte] = {
    ScalaMPack.write(this)
  }
}

object Trace extends HBaseObject[Trace]("trace") {
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
    val trace = new Trace
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
    Option(trace)
  }

}
