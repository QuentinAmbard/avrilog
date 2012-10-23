package com.avricot.avrilog.model

import org.joda.time.DateTime
import org.junit.Test
import org.junit.Assert
import org.msgpack.ScalaMessagePack._
import org.msgpack.annotation.Message
import scala.collection.mutable.Map
import org.msgpack.AvrilogMPack
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.codehaus.jackson.annotate.JsonIgnore
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.format.ISODateTimeFormat

class SerializationTest {

  @Test def jsonTest() = {

    val user = User("userId", "firstname", "lastname", null, null, null)
    val d1 = new DateTime(15654564L)
    val ctrace = new ClientTrace(Array[Byte](12), "cat", "info", d1, false, false, user, Map[String, String]("a" -> "aqsd")) //
    val trace = new Trace(ctrace, null, null)
    val isoFormatter = ISODateTimeFormat.dateTime();

    def findType(family: String, name: String, value: Any): Unit = {
      val bytes = value match {
        case v if v == null => Unit
        case v: Int => v.asInstanceOf[Int]
        case v: Long => v.asInstanceOf[Long]
        case v: Float => v.asInstanceOf[Float]
        case v: String => v.asInstanceOf[String]
        case v: Boolean => v.asInstanceOf[Boolean]
        case v: DateTime => isoFormatter.print(v.asInstanceOf[DateTime])
        case v: Array[Byte] => v.asInstanceOf[Array[Byte]]
        case v: Map[Any, Any] => {
          for ((k, v) <- v.asInstanceOf[Map[Any, Any]]) {
            findType(name, k.toString, v)
          }
          Unit
        }
        case v: Any => exploreObj(name, v); Unit
        case _ => Unit
      }
      if (bytes != Unit) println(family + " field=" + name + " value=" + bytes)
    }

    def exploreObj(family: String, obj: Any) = {
      for (field <- obj.getClass.getDeclaredFields) {
        field.setAccessible(true)
        val t = field.getType()
        val value = field.get(obj)
        findType(family, field.getName(), value)
      }
    }
    exploreObj("info", trace)

    //    val test2 = mapper.writeValueAsString(trace)
    //    println(test2)
    //println(trace.toJson)

  }
  @Test
  def testSerialization() = {
    val user = User("userId", "firstname", "lastname", "email", "groupId", "ip")
    val d1 = new DateTime(15654564L)
    val trace = new ClientTrace(Array[Byte](12), "category", "info", d1, false, false, user, Map[String, String]("a" -> "aqsd")) //
    val b = trace.serialize
    val newTrace = AvrilogMPack.read[ClientTrace](b)
    Assert.assertEquals("category", newTrace.category)
    Assert.assertEquals(15654564L, newTrace.clientDate.getMillis())
    Assert.assertEquals("aqsd", newTrace.data.get("a").get)
    Assert.assertEquals("userId", newTrace.user.id)
    Assert.assertEquals("firstname", newTrace.user.firstname)
    Assert.assertEquals("lastname", newTrace.user.lastname)
    Assert.assertEquals("email", newTrace.user.email)
    Assert.assertEquals("groupId", newTrace.user.groupId)

  }
}