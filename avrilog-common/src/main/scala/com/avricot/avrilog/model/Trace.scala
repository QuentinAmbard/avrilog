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
    val objArgs = scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Array[Byte]]]();
    for (kv <- result.raw()) {
      val splitKV = kv.split()
      val hBaseFamily = Bytes.toString(splitKV.getFamily())
      val fieldName = Bytes.toString(splitKV.getQualifier())
      val fieldValues = objArgs.get(hBaseFamily) getOrElse scala.collection.mutable.Map[String, Array[Byte]]();
      fieldValues(fieldName) = splitKV.getValue()
      objArgs(hBaseFamily) = fieldValues
    }

    def buildObject(klass: Class[_], family: String): Any = {
      println("----------" + family)
      if (!objArgs.contains(family)) return null
      val args = scala.collection.mutable.MutableList[Object]()
      //val mapArgs
      //scan all the object field
      klass match {
        //Map constructor
        case M => {
          val map = scala.collection.mutable.Map[String, String]();
          if (!objArgs.contains(family)) return null
          println("ok i'm a map family " + family)
          for ((k, v) <- objArgs.get(family).get) {
            map(k) = Bytes.toString(v)
          }
          map
        }
        //AnyRef constructor
        case _ => {
          for (field <- klass.getDeclaredFields()) {
            println("objArgs.get(" + family + ").get(" + field.getName() + ")")
            //If the field exist (AnyRef)
            if (objArgs.get(family).get.contains(field.getName())) {
              val value = getValue(field.getType(), objArgs.get(family).get(field.getName())).asInstanceOf[Object]
              args += value
            } else { //Else, it might be null or an embbed object, or a map
              args += buildObject(field.getType(), family + "." + field.getName()).asInstanceOf[Object]
            }
          }
          println("construct " + klass.getName())
          val constructor = klass.getConstructors.head
          constructor.newInstance(args: _*)
        }
      }
    }

    val t = buildObject(classOf[Trace], "info").asInstanceOf[Trace]
    println("category:" + t.category)
    println("category:" + t.user)
    println("category:" + t.data)
    None
  }

}
