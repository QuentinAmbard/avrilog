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
import org.junit.Ignore

class TraceIntegrationTest {

  @Test def read() = {
    val t = Trace.find(Array[Byte](22))
    println(t)

  }
  @Ignore @Test def writeReadDelete() = {
    //Write
    val user = User("userId", "firstname", "lastname", "email", "groupId", "ip")
    val d1 = new DateTime(15654564L)
    val cTrace = new ClientTrace(Array[Byte](22), "category", "info", d1, true, true, user, Map[String, String]("a" -> "aqsd"))
    val trace = new Trace(cTrace)
    Trace.save(trace)

    //    //Read
    //    val t = Trace.find(Array[Byte](22))
    //    println(t)
  }

}