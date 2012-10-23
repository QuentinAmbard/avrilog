package com.avricot.avrilog.timestamp

import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.binary.Base64
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.math.BigInteger
import java.net.URLEncoder
import sun.misc.IOUtils
import scalax.io.Resource

object Timestamping {
  val config = ConfigFactory.load()
  val url = config.getString("timestamping.url")
  val useBasicAuth = config.getBoolean("timestamping.useBasicAuth")
  val method = config.getString("timestamping.method")
  val contentType = config.getString("timestamping.contentType")
  val encoding = config.getString("timestamping.encoding")
  val username = config.getString("timestamping.username")
  val password = config.getString("timestamping.password")
  val exchange = config.getString("timestamping.exchange")
  val algo = config.getString("timestamping.algo")
  val rawParams = config.getString("timestamping.params")

  val authString = username + ":" + password;
  val authStringEnc = Base64.encodeBase64URLSafeString(authString.getBytes());
  val params = interpolate(rawParams, Map("algo" -> algo, "username" -> username, "password" -> password, "exchange" -> exchange))

  def interpolate(text: String, vars: Map[String, String]) = (text /: vars) { (t, kv) => t.replace("${" + kv._1 + "}", URLEncoder.encode(kv._2, encoding)) }

  def timestamp(str: String): Array[Byte] = {
    timestamp(str.getBytes())
  }

  def timestamp(b: Array[Byte]): Array[Byte] = {
    val hash = getHash(b)
    val parameters = interpolate(params, Map("hash" -> hash))
    val conn = new URL(url).openConnection().asInstanceOf[HttpURLConnection];
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestMethod(method);
    conn.setRequestProperty("Content-Type", contentType);
    if (useBasicAuth) {
      conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
    }
    val out = conn.getOutputStream();
    out.write(params.getBytes(encoding));
    out.flush();
    Resource.fromInputStream(conn.getInputStream()).byteArray
  }

  private def getHash(b: Array[Byte]) = {
    val md = MessageDigest.getInstance(algo);
    val sha = md.digest(b)
    val i = new BigInteger(1, sha);
    String.format("%1$032x", i);
  }
}