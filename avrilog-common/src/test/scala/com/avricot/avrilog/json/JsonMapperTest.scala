package com.avricot.avrilog.json

import org.junit.Test
import com.avricot.avrilog.model.User
import org.joda.time.DateTime
import com.avricot.avrilog.model.ClientTrace
import junit.framework.Assert
import scala.collection.mutable.Map
import com.avricot.avrilog.model.Trace
import com.avricot.avrilog.model.TraceContent
import net.liftweb.json._
import net.liftweb.json.Serialization.{ read, write }

case class TestSer(test: TestSer2, bar: String)
case class TestSer2(foo: String, bar: Binary, dt: DateTime)

class JsonMapperTest {
  @Test def jsonTest(): Unit = {
    val user = User("userId", "firstname", "lastname", null, null, null)
    val d1 = new DateTime(1352282343000L)
    val ctrace = new ClientTrace(Array[Byte](12), null, null, "info", d1, false, false, user, Map[String, String]("a" -> "aqsd")) //
    val test2 = JsonMapper.mapper.writeValueAsString(ctrace)
    Assert.assertEquals("""{"id":"DA==","info":"info","clientDate":"2012-11-07T10:59:03.000+01:00","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"},"data":{"a":"aqsd"}}""", test2)
  }

  @Test def jsonTestMapp(): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val user = User("userId", "firstname", "lastname", null, null, null)
    val d1 = new DateTime(1352282343000L)
    val ctrace = new TraceContent(Binary(Array[Byte](12)), null, null, null, null, false, false, null, null) //
    val trace = Trace(ctrace)
    val traceJson2 = JsonMapper.mapper.writeValueAsString(trace)
    val test3 = JsonMapper.mapper.readValue(traceJson2, classOf[Trace])
    Assert.assertEquals(12, test3.content.id.bytes.head)

  }

}