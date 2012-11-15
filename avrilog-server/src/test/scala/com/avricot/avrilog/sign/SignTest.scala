package com.avricot.avrilog.sign
import org.junit.Test
import org.junit.Assert
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.File
import com.avricot.avrilog.json.JsonMapper
import com.avricot.avrilog.crypto.sign.Sign
import com.avricot.avrilog.crypto.sign.VerificationStatus

class SignTest {
  val traceContent = """{"id":"DA==","info":"info","clientDate":"2012-11-07T10:59:03.000+01:00","sign":false,"horodate":false,"user":{"id":"userId","firstname":"firstname","lastname":"lastname"},"data":{"a":"aqsd"}}"""

  @Test def signTest(): Unit = {
    val signedData = Sign.sign(traceContent.getBytes())

    val writer2 = new BufferedOutputStream(new FileOutputStream(new File("/home/quentin/traceBinanry")));
    writer2.write(traceContent.getBytes());
    writer2.close();

    val writer = new BufferedOutputStream(new FileOutputStream(new File("/home/quentin/tracepkcs7")));
    writer.write(signedData);
    writer.close();

    Assert.assertEquals(VerificationStatus.OK.toString, Sign.verifySign(traceContent.getBytes(), signedData).status)
    val verif = Sign.verifySign(("t" + traceContent).getBytes(), signedData)
    Assert.assertEquals(VerificationStatus.KO.toString, verif.status)
    Assert.assertEquals("Signed digest doesn't match : message-digest attribute value does not match calculated value", verif.info)
  }

}

