package com.avricot.avrilog.json

import org.junit.Test
import com.avricot.avrilog.model.User
import org.joda.time.DateTime
import com.avricot.avrilog.model.ClientTrace
import junit.framework.Assert
import scala.collection.mutable.Map
import com.avricot.avrilog.model.Trace
import com.avricot.avrilog.model.TraceContent
import scala.collection.immutable.TreeMap

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
    val user = User("userId", "firséèàtname", "lastname", null, null, null, null)
    val d1 = new DateTime(1352282343000L)
    val ctrace = new ClientTrace(Array[Byte](12), null, null, null, "info", d1, false, false, user, Map[String, String]("i" -> "aqsd")) //
    val test2 = JsonMapper.mapper.writeValueAsString(ctrace)
    Assert.assertEquals("""{"id":"DA==","info":"info","clientDate":"2012-11-07T10:59:03.000+01:00","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firséèàtname","lastname":"lastname"},"data":{"i":"aqsd"}}""", test2)
  }

  //Also check TreeMap String order.
  @Test def jsonTestMapp(): Unit = {
    val user = User("userId", "firstname", "lastname", null, null, null, null)
    val d1 = new DateTime(1352282343000L)
    val trace = Trace(new TraceContent(Array[Byte](12), null, "qadeaz", "qazeaze", "aeazooo", d1, false, false, user, TreeMap[String, String]("i" -> "aqsd", "kk" -> "ff", "zz" -> "aa", "bb" -> "aqsd"), d1))
    val traceJson2 = JsonMapper.mapper.writeValueAsString(trace)
    Assert.assertEquals("""{"content":{"id":"DA==","entityId":"qadeaz","category":"qazeaze","info":"aeazooo","clientDate":"2012-11-07T10:59:03.000+01:00","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"},"data":{"bb":"aqsd","i":"aqsd","kk":"ff","zz":"aa"},"date":"2012-11-07T10:59:03.000+01:00"}}""", traceJson2)
    val test3 = JsonMapper.mapper.readValue(traceJson2, classOf[Trace])
    Assert.assertEquals(12, test3.content.id.head)

  }

  @Test def jsonMap(): Unit = {
    val str = """{"content":{"id":"AAAAATsFa0EY","info":"info","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"}}}"""
    val test = JsonMapper.mapper.readValue(str, classOf[Trace])
    Assert.assertEquals(str, test.toJson)
    val str2 = """{"content":{"id":"AFUVq4uDpHUUf////4/BMwE=","applicationName":"test","entityId":"entityId","category":"test24","info":"info","clientDate":"2012-12-16T11:54:36.746+01:00","sign":true,"horodate":false,"user":{"firstname":"firstname"},"data":{"key":"value"},"date":"2012-12-16T11:54:36.900+01:00"},"signContent":"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"}"""
    val test2 = JsonMapper.mapper.readValue(str2, classOf[Trace])
    Assert.assertEquals("test", test2.content.applicationName)
    Assert.assertEquals("entityId", test2.content.entityId)
    Assert.assertEquals("test24", test2.content.category)
    Assert.assertEquals("info", test2.content.info)
    Assert.assertEquals("firstname", test2.content.user.firstname)
    Assert.assertEquals(null, test2.content.user.lastname)
  }

}