package com.avricot.avrilog.crypto.timestamp

import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.binary.Base64
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import scalax.io.Resource
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.File
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.tsp.TimeStampToken
import org.slf4j.LoggerFactory
import java.io.IOException
import com.avricot.avrilog.crypto.hash.Hash

object Timestamping extends Hash {
  val logger = LoggerFactory.getLogger(Timestamping.getClass())

  val config = ConfigFactory.load()
  val url = config.getString("integrity.remoteTimestamping.url")
  val useBasicAuth = config.getBoolean("integrity.remoteTimestamping.useBasicAuth")
  val method = config.getString("integrity.remoteTimestamping.method")
  val contentType = config.getString("integrity.remoteTimestamping.contentType")
  val encoding = config.getString("integrity.remoteTimestamping.encoding")
  val username = config.getString("integrity.remoteTimestamping.username")
  val password = config.getString("integrity.remoteTimestamping.password")
  val exchange = config.getString("integrity.remoteTimestamping.exchange")
  val algo = config.getString("integrity.algo")
  val rawParams = interpolate(config.getString("integrity.remoteTimestamping.params"), Map(
    "integrity.algo" -> algo,
    "integrity.remoteTimestamping.encoding" -> encoding,
    "integrity.remoteTimestamping.exchange" -> exchange,
    "integrity.remoteTimestamping.password" -> password,
    "integrity.remoteTimestamping.username" -> username))

  val authString = username + ":" + password;
  val authStringEnc = Base64.encodeBase64URLSafeString(authString.getBytes());

  def interpolate(text: String, vars: Map[String, String]) = (text /: vars) { (t, kv) => t.replace("${" + kv._1 + "}", URLEncoder.encode(kv._2, encoding)) }

  def timestamp(str: String): Array[Byte] = {
    timestamp(str.getBytes())
  }

  def timestamp(b: Array[Byte]): Array[Byte] = {
    try {
      val hash = getHash(b, algo)
      val parameters = interpolate(rawParams, Map("hash" -> hash))
      val conn = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
      conn.setDoOutput(true)
      conn.setDoInput(true)
      conn.setRequestMethod(method)
      conn.setRequestProperty("Content-Type", contentType)
      if (useBasicAuth) {
        conn.setRequestProperty("Authorization", "Basic " + authStringEnc)
      }
      val out = conn.getOutputStream()
      out.write(parameters.getBytes(encoding))
      out.flush()
      val ba = Resource.fromInputStream(conn.getInputStream()).byteArray
      val writer = new BufferedOutputStream(new FileOutputStream(new File("/home/quentin/jeton.test")));
      writer.write(ba);
      writer.close();
      ba
    } catch {
      case ioe: IOException => logger.error("can't access to timestamping webservice : ", ioe); null
      case e: Throwable => logger.error("can't timestamp the data : ", e); null
    }
  }

  /**
   * Check if a timestamp has the same hash value as expected.
   */
  def verifyTimestamp(timestamp: Array[Byte], hash: Array[Byte]) = {
    val signedData = new CMSSignedData(timestamp)
    val tto = new TimeStampToken(signedData)
    tto.getTimeStampInfo().getMessageImprintDigest().deep == hash.deep
  }

}