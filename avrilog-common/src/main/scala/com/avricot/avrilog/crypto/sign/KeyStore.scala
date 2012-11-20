package com.avricot.avrilog.crypto.sign

import java.io.FileInputStream
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.ArrayList
import org.bouncycastle.cert.jcajce.JcaCertStore
import com.avricot.avrilog.util.FileUtil

class KeyStore(pkcs12Path: String, pkcs12Password: Array[Char], privateKeyPassword: Array[Char], providerName: String) {
  val keystore = java.security.KeyStore.getInstance("PKCS12")
  keystore.load(FileUtil.getInputStream(pkcs12Path), pkcs12Password);

  val aliasesEnum = keystore.aliases()
  def findAlias(): String = {
    while (aliasesEnum.hasMoreElements()) {
      val aliasName = aliasesEnum.nextElement()
      if (keystore.isKeyEntry(aliasName)) {
        return aliasName
      }
    }
    return null
  }

  val alias = findAlias()
  val privatekey = keystore.getKey(alias, privateKeyPassword).asInstanceOf[PrivateKey]
  val cert = keystore.getCertificate(alias).asInstanceOf[X509Certificate]
  val certList = new ArrayList[X509Certificate]()
  certList.add(cert);
  val publickey = keystore.getCertificate(alias).getPublicKey()
  //val certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), providerName)
  val store = new JcaCertStore(certList);
}