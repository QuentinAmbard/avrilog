package com.avricot.avrilog.model

import org.joda.time.DateTime
import org.junit.Test
import org.junit.Assert
import org.msgpack.ScalaMPack;
import org.msgpack.ScalaMessagePack._
import org.msgpack.annotation.Message
import com.avricot.avrilog.serialize.MPack
import scala.collection.mutable.Map
class SerializationTest {

  @Test
  def testSerialization() = {
    val user = User("userId", "firstname", "lastname", "email", "groupId")
    val d1 = new DateTime(15654564L)
    val d2 = new DateTime(5454231L)
    val trace = new Trace("id", "category", "info", d1, d2, user, Map[String, String]("a" -> "aqsd"))
    val b = trace.serialize
    val newTrace = ScalaMPack.read[Trace](b)
    Assert.assertEquals("id", newTrace.id)
    Assert.assertEquals("category", newTrace.category)
    Assert.assertEquals(15654564L, newTrace.date.getMillis())
    Assert.assertEquals(5454231L, newTrace.clientDate.getMillis())
    Assert.assertEquals("aqsd", newTrace.data.get("a").get)
    Assert.assertEquals("userId", newTrace.user.id)
    Assert.assertEquals("firstname", newTrace.user.firstname)
    Assert.assertEquals("lastname", newTrace.user.lastname)
    Assert.assertEquals("email", newTrace.user.email)
    Assert.assertEquals("groupId", newTrace.user.groupId)

  }
}