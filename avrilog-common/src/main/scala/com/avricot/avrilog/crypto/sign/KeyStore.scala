package com.avricot.avrilog.crypto.sign

import java.security.KeyStore
import java.io.FileInputStream
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.ArrayList
import org.bouncycastle.cert.jcajce.JcaCertStore

class KeyStore(pkcs12Path: String, pkcs12Password: Array[Char], privateKeyPassword: Array[Char], providerName: String) {
  val classPathConfig = "classpath:"
  val keystore = KeyStore.getInstance("PKCS12")
  val in = pkcs12Path match {
    case path if path.startsWith("classpath:") => this.getClass().getClassLoader().getResourceAsStream(pkcs12Path.substring(classPathConfig.size))
    case _ => new FileInputStream(pkcs12Path)
  }
  keystore.load(in, pkcs12Password);

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