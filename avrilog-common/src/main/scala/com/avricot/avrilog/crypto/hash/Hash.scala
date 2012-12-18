package com.avricot.avrilog.crypto.hash

import java.security.MessageDigest
import java.math.BigInteger
import java.io.InputStream
import java.util.Formatter
import com.google.common.io.Files
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import java.io.File
import com.google.common.hash.HashFunction

trait Hash {
  /**
   * Return the digest of the input stream.
   */
  def getRawHash(input: File, algo: String) = {
    Files.hash(input, algo).asBytes()
  }

  /**
   * Return the digest of the input stream as a string value.
   */
  def getHash(input: File, algo: String) = {
    Files.hash(input, algo).toString()
  }

  /**
   * Return the digest of the input as a string value.
   */
  def getHash(b: Array[Byte], algo: String) = {
    algo.hashBytes(b).toString()
  }

  /**
   * Return the digest of the input.
   */
  def getRawHash(b: Array[Byte], algo: String) = {
    algo.hashBytes(b).asBytes()
  }

  /**
   * Convert the algo to the matching guava HashFunction.
   */
  private implicit def getHashFunction(algo: String): HashFunction = {
    algo match {
      case "SHA1" => Hashing.sha1()
      case "SHA256" => Hashing.sha256()
      case "SHA512" => Hashing.sha512()
      case "MD5" => Hashing.md5()
    }
  }

}