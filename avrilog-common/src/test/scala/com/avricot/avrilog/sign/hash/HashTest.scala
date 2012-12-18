package com.avricot.avrilog.sign.hash

import org.junit.Test
import com.avricot.avrilog.model.User
import org.joda.time.DateTime
import com.avricot.avrilog.model.ClientTrace
import junit.framework.Assert
import scala.collection.mutable.Map
import com.avricot.avrilog.model.Trace
import com.avricot.avrilog.model.TraceContent
import scala.collection.immutable.TreeMap
import com.avricot.avrilog.crypto.hash.Hash
import scala.util.Random

class HashTest extends Hash {

  @Test def jsonHash(): Unit = {
    val b = "azeazeaze".getBytes("UTF-8")
    val hash = getHash(b, "SHA1")
    Assert.assertEquals("94d53fbe11fa48a71877301d104e2b0409ba9822", hash)
    val b2 = "The quick brown fox jumps over the lazy dog".getBytes("UTF-8")
    val hash2 = getHash(b2, "SHA1")
    Assert.assertEquals("2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", hash2)
    val b3 = "".getBytes("UTF-8")
    val hash3 = getHash(b3, "SHA1")
    Assert.assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", hash3)
    val b0 = "azzersdf".getBytes("UTF-8")
    val hash0 = getHash(b0, "SHA1")
    Assert.assertEquals("003926d71187a3096c0cfbddbea39d245cacb290", hash0)
  }

  @Test def testRawHash(): Unit = {
    val b = "azeazeaze".getBytes("UTF-8")
    val hash = getRawHash(b, "SHA1")
    Assert.assertEquals(20, hash.size)
    val b2 = "The quick brown fox jumps over the lazy dog".getBytes("UTF-8")
    val hash2 = getRawHash(b2, "SHA1")
    Assert.assertEquals(20, hash2.size)
    val b3 = "".getBytes("UTF-8")
    val hash3 = getRawHash(b3, "SHA1")
    Assert.assertEquals(20, hash3.size)
    val b0 = "azzersdf".getBytes("UTF-8")
    val hash0 = getRawHash(b0, "SHA1")
    Assert.assertEquals(20, hash0.size)
  }

  @Test def hashTestSize(): Unit = {
    val rand = Random
    for (i <- 0 to 100) {
      val b3 = rand.nextString(10)
      println(b3)
      val hash3 = getHash(b3.getBytes("UTF-8"), "SHA1")
      println(hash3)
      Assert.assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709".size, hash3.size)
    }
  }

}