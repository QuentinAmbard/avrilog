package com.avricot.avrilog.sign
import org.junit.Test
import org.junit.Assert
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.File

class RemoteSignIntegrationTest {
  val traceContent = """{"id":"DA==","info":"info","clientDate":"2012-11-07T10:59:03.000+01:00","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"},"data":{"a":"aqsd"}}"""

  @Test def signRemoteIntegrationTest(): Unit = {
    val signedData = Sign.signWithRemoteTimestamp(traceContent.getBytes())
    val writer3 = new BufferedOutputStream(new FileOutputStream(new File("/home/quentin/tracepkcs7Universign")));
    writer3.write(signedData);
    writer3.close();

    Assert.assertTrue(Sign.verifySign(traceContent.getBytes(), signedData))
    Assert.assertFalse(Sign.verifySign(("t" + traceContent).getBytes(), signedData))

  }
}