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

    val test2 = mapper.writeValueAsString(trace)
    println(test2)
    println(trace.toJson)

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