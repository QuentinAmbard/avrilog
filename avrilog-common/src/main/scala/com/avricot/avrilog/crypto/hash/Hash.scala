package com.avricot.avrilog.crypto.hash

import java.security.MessageDigest
import java.math.BigInteger
import java.io.InputStream

trait Hash {

  /**
   * Return the digest of the input stream.
   */
  def getRawHash(input: InputStream, algo: String) = {
    val md = MessageDigest.getInstance(algo);
    val buffer = new Array[Byte](1024)
    Stream.continually(input.read(buffer)).takeWhile(_ != -1).foreach(md.update(buffer, 0, _))
    md.digest()
  }

  /**
   * Return the digest of the input stream as a string value.
   */
  def getHash(input: InputStream, algo: String) = {
    val digest = getRawHash(input, algo)
    val i = new BigInteger(1, digest);
    String.format("%1$032x", i);
  }

  /**
   * Return the digest of the input as a string value.
   */
  def getHash(b: Array[Byte], algo: String) = {
    val digest = getRawHash(b, algo)
    val i = new BigInteger(1, digest);
    String.format("%1$032x", i);
  }

  /**
   * Return the digest of the input.
   */
  def getRawHash(b: Array[Byte], algo: String) = {
    val md = MessageDigest.getInstance(algo);
    md.digest(b)
  }

}