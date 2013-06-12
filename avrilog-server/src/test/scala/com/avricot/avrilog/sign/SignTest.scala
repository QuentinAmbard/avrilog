package com.avricot.avrilog.sign
import org.junit.Test
import org.junit.Assert
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.File
import com.avricot.avrilog.json.JsonMapper
import com.avricot.avrilog.crypto.sign.Sign
import com.avricot.avrilog.crypto.sign.VerificationStatus
import com.avricot.avrilog.util.FileUtil

class SignTest {
  val traceContent = """{"id":"AVURT8sajOOsf////4L0Hxo=","applicationName":"myprocurement-web","entityId":"9804","category":"SUPPLIER_UPLOAD_DOC","info":"c12024f54f29a261c310a5af251faba4","clientDate":"2012-12-19T19:15:08.368+01:00","sign":true,"horodate":false,"user":{"id":"1722","firstname":"comm","lastname":"commercial","email":"qambard@myprocurement.fr","groupId":"1","groupName":"MY PROCUREMENT","ip":"82.225.204.206"},"data":{"docName":"Certificats de qualifications individuelles","docTypeId":"17","mongoDocId":"50d2042c3c72e3ac1b067015","uploadDate":"2012-12-19T19:15+0100","validityEndDate":null,"validityStartDate":"2012-12-01T00:00+0100"},"date":"2012-12-19T19:15:08.524+01:00"}"""

  @Test def signTest(): Unit = {
    val signedData = Sign.sign(traceContent.getBytes())
    //    val writer2 = new BufferedOutputStream(new FileOutputStream(new File("/home/quentin/traceBinanry")));
    //    writer2.write(traceContent.getBytes());
    //    writer2.close();
    //
    //    val writer = new BufferedOutputStream(new FileOutputStream(new File("/home/quentin/tracepkcs7")));
    //    writer.write(signedData);
    //    writer.close();

    Assert.assertEquals(VerificationStatus.OK.toString, Sign.verifySign(traceContent.getBytes(), signedData).status)
    val verif = Sign.verifySign(("t" + traceContent).getBytes(), signedData)
    Assert.assertEquals(VerificationStatus.KO.toString, verif.status)
    Assert.assertEquals("Signed digest doesn't match : message-digest attribute value does not match calculated value", verif.info)
  }

}

