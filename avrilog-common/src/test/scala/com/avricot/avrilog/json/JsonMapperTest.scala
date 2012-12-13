package com.avricot.avrilog.json

import org.junit.Test
import com.avricot.avrilog.model.User
import org.joda.time.DateTime
import com.avricot.avrilog.model.ClientTrace
import junit.framework.Assert
import scala.collection.mutable.Map
import com.avricot.avrilog.model.Trace
import com.avricot.avrilog.model.TraceContent

case class TestSer(test: TestSer2, bar: String)
case class TestSer2(foo: String, bar: Array[Byte], dt: scala.collection.Map[String, String])

class JsonMapperTest {

  @Test def jsonTestMap(): Unit = {
    val t = TestSer2("ee", Array[Byte](2), scala.collection.Map[String, String]("aa" -> "az"))
    val traceJson2 = JsonMapper.mapper.writeValueAsString(t)
    println(traceJson2)
    val test3 = JsonMapper.mapper.readValue(traceJson2, classOf[TestSer2])
    Assert.assertEquals("az", test3.dt("aa"))
  }

  @Test def jsonTest(): Unit = {
    val user = User("userId", "firstname", "lastname", null, null, null, null)
    val d1 = new DateTime(1352282343000L)
    val ctrace = new ClientTrace(Array[Byte](12), null, null, null, "info", d1, false, false, user, Map[String, String]("a" -> "aqsd")) //
    val test2 = JsonMapper.mapper.writeValueAsString(ctrace)
    Assert.assertEquals("""{"id":"DA==","info":"info","clientDate":"2012-11-07T10:59:03.000+01:00","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"},"data":{"a":"aqsd"}}""", test2)
  }

  @Test def jsonTestMapp(): Unit = {
    val user = User("userId", "firstname", "lastname", null, null, null, null)
    val d1 = new DateTime(1352282343000L)
    val trace = Trace(new TraceContent(Array[Byte](12), null, "qadeaz", "qazeaze", "aeazooo", d1, false, false, user, null, d1))
    val traceJson2 = JsonMapper.mapper.writeValueAsString(trace)
    println(traceJson2)
    val test3 = JsonMapper.mapper.readValue(traceJson2, classOf[Trace])
    Assert.assertEquals(12, test3.content.id.head)

  }

  @Test def jsonMap(): Unit = {
    val str = """{"content":{"id":"AAAAATsFa0EY","info":"info","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"},"data":{"a":"aqsd"}}}"""
    val test3 = JsonMapper.mapper.readValue(str, classOf[Trace])
  }

}