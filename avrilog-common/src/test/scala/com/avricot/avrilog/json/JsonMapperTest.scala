package com.avricot.avrilog.json

import org.junit.Test
import com.avricot.avrilog.model.User
import org.joda.time.DateTime
import com.avricot.avrilog.model.ClientTrace
import junit.framework.Assert
import scala.collection.mutable.Map

class JsonMapperTest {
  @Test def jsonTest() = {
    val user = User("userId", "firstname", "lastname", null, null, null)
    val d1 = new DateTime(1352282343000L)
    val ctrace = new ClientTrace(Array[Byte](12), null, null, "info", d1, false, false, user, Map[String, String]("a" -> "aqsd")) //
    val test2 = JsonMapper.mapper.writeValueAsString(ctrace)
    Assert.assertEquals("""{"id":"DA==","info":"info","clientDate":"2012-11-07T10:59:03.000+01:00","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"},"data":{"a":"aqsd"}}""", test2)
    println(test2)
  }

}