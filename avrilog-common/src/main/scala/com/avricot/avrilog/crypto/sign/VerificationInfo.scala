package com.avricot.avrilog.crypto.sign

object VerificationStatus extends Enumeration {
  type VerificationStatus = Value
  val OK = Value("OK")
  val KO = Value("KO")
}

case class VerificationInfo(status: String, info: String = null)

object VerificationInfo {
  def getKo(info: String) = VerificationInfo(VerificationStatus.KO.toString, info)
  def getOk(info: String) = VerificationInfo(VerificationStatus.OK.toString, info)
}