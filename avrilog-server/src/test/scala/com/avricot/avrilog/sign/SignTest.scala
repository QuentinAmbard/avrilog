package com.avricot.avrilog.sign
import org.junit.Test
import org.junit.Assert
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.File

class SignTest {
  val traceContent = """{"id":"DA==","info":"info","clientDate":"2012-11-07T10:59:03.000+01:00","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"},"data":{"a":"aqsd"}}"""

  //        val source = getClass.getResourceAsStream("/certif/timestamp")
  //      val byteData = Iterator.continually(source.read).takeWhile(-1 !=).map(_.toByte).toArray
  //      source.close()
  //      byteData

  @Test def signTest(): Unit = {
    val signedData = Sign.sign(traceContent.getBytes())

    val writer = new BufferedOutputStream(new FileOutputStream(new File("/home/quentin/sign.test")));
    writer.write(signedData);
    writer.close();

    Assert.assertTrue(Sign.verifySign(traceContent.getBytes(), signedData))
    Assert.assertFalse(Sign.verifySign(("t" + traceContent).getBytes(), signedData))
  }

}