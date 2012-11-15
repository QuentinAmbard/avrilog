package com.avricot.avrilog.json

case class Binary(bytes: Array[Byte])

object BinaryConversion {
  implicit def BinaryByte(value: Array[Byte]): Binary = new Binary(value)
  implicit def ByteBinary(b: Binary): Array[Byte] = b.bytes
}