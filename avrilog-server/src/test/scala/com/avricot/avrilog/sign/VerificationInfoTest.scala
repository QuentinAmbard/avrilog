package com.avricot.avrilog.sign
import org.junit.Test
import org.junit.Assert
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.File
import com.avricot.avrilog.json.JsonMapper
import com.avricot.avrilog.crypto.sign.VerificationInfo

class VerificationInfoTest {

  @Test def signTest(): Unit = {
    Assert.assertEquals("KO", VerificationInfo.getKo("test").status)
    Assert.assertEquals("test", VerificationInfo.getKo("test").info)
    Assert.assertEquals("OK", VerificationInfo.getOk("test2").status)
    Assert.assertEquals("test2", VerificationInfo.getOk("test2").info)
  }

}