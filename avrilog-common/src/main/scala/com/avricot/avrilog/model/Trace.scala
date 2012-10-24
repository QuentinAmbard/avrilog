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
case class Trace(id: Array[Byte], category: String, info: String, clientDate: DateTime, sign: Boolean, horodate: Boolean, user: User, data: Map[String, String], date: DateTime = null, timestampingContent: Array[Byte] = null, signContent: Array[Byte] = null) extends BaseHBaseObject {
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

object Trace extends HBaseObject[Trace]("trace") {
  val mapper = new ObjectMapper()
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
  mapper.registerModule(DefaultScalaModule)

  /**
   * Return a trace by it's id.
   */
  def find(id: Array[Byte]): Option[Trace] = {
    val get = new Get(id)
    val result = table.get(get)
    getFromResult(result)
  }

  /**
   * Build a trace from a result
   */
  private def getFromResult(result: Result): Option[Trace] = {
    val I = classOf[Int]
    val L = classOf[Long]
    val F = classOf[Float]
    val S = classOf[String]
    val B = classOf[Boolean]
    val D = classOf[DateTime]
    val A = classOf[Array[Byte]]
    val M = classOf[Map[_, _]]

    if (result.isEmpty()) {
      return None
    }
    val klass = classOf[Trace]

    def getValue(klass: Class[_], fieldValue: Array[Byte]) = {
      klass match {
        case I => Bytes.toInt(fieldValue)
        case L => Bytes.toLong(fieldValue)
        case F => Bytes.toFloat(fieldValue)
        case S => Bytes.toString(fieldValue)
        case B => Bytes.toBoolean(fieldValue)
        case D => isoFormatter.parseDateTime(Bytes.toString(fieldValue))
        case A => fieldValue
        case M => {
          println("i'm a map")
          null
        }
        case _ => println("i'm nothing" + klass); null
      }
    }

    val objectArgs = scala.collection.mutable.Set[String]();
    for (field <- klass.getDeclaredFields()) {
      objectArgs += field.getName()
    }

    def buildObject(klass: Class[_], family: String): Any = {
      val hbaseArgs = scala.collection.mutable.Map[String, Any]()
      for (kv <- result.raw()) {
        val splitKV = kv.split()
        val hBaseFamily = Bytes.toString(splitKV.getFamily())
        val fieldName = Bytes.toString(splitKV.getQualifier())
        println(fieldName)
        println(hBaseFamily + " " + family + " " + hBaseFamily.startsWith(family) + " " + (hBaseFamily.count(_ == '.') + 1))
        if (family == hBaseFamily) {
          val field = klass.getDeclaredField(fieldName)
          println(fieldName + " ===> " + field.getType())
          hbaseArgs(fieldName) = getValue(field.getType(), splitKV.getValue())
        } else if (hBaseFamily.startsWith(family) && hBaseFamily.count(_ == '.') == family.count(_ == '.') + 1) {
          val field = klass.getDeclaredField(hBaseFamily.substring(family.size + 1))
          println(fieldName + " ===> " + field.getType() + " " + hBaseFamily)
          hbaseArgs(hBaseFamily) = field.getType() match {
            case M => {
              val map = if (hbaseArgs.contains(hBaseFamily)) hbaseArgs.get(hBaseFamily).asInstanceOf[Map[String, String]] else scala.collection.mutable.Map[String, String]()
              map(fieldName) = Bytes.toString(splitKV.getValue())
              map
            }
            case _ => buildObject(field.getType(), hBaseFamily)
          }
        }
      }
      val args = scala.collection.mutable.MutableList[Object]()
      for (field <- klass.getDeclaredFields) {
        println(field.getName() + " = " + hbaseArgs.get(field.getName()))
        args += (hbaseArgs.getOrElse(field.getName(), null)).asInstanceOf[Object]
      }
      val constructor = klass.getConstructors.head
      constructor.newInstance(args: _*)
    }
    val t = buildObject(classOf[Trace], "info").asInstanceOf[Trace]
    println("category:" + t.category)
    //        val data = scala.collection.mutable.Map[String, String]();
    //        for (kv <- result.raw()) {
    //          val splitKV = kv.split()
    //          if (Bytes.toString(splitKV.getFamily()) == columnFamily) {
    //            val qualifier = Bytes.toString(splitKV.getQualifier())
    //            val value = Bytes.toString(splitKV.getValue())
    //            data(qualifier) = value
    //          }
    //        }
    //        data.toMap
    None
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
    //null //Option(trace)
  }

}
