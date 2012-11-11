package com.avricot.avrilog.hash

import java.security.MessageDigest
import java.math.BigInteger

trait Hash {
  def getHash(b: Array[Byte], algo: String) = {
    val md = MessageDigest.getInstance(algo);
    val sha = md.digest(b)
    val i = new BigInteger(1, sha);
    String.format("%1$032x", i);
  }

  def getRawHash(b: Array[Byte], algo: String) = {
    val md = MessageDigest.getInstance(algo);
    md.digest(b)
  }

}