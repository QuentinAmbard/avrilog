package com.avricot.avrilog.sign

import java.util.ArrayList
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cert.jcajce.JcaCertStore
import java.security.KeyStore
import java.io.FileInputStream
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.cms.CMSSignedDataGenerator
import java.security.cert.Certificate
import java.io.IOException
import java.util.Arrays
import java.security.cert.CertStore
import java.security.cert.CollectionCertStoreParameters
import java.security.PrivateKey
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import java.security.cert.X509Certificate
import java.io.ByteArrayInputStream
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.bouncycastle.operator.ContentSigner
import javax.security.cert.CertificateEncodingException
import org.bouncycastle.cms.SignerInformation
import com.avricot.avrilog.timestamp.Timestamping
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.tsp.TimeStampToken
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.ASN1EncodableVector
import java.util.Hashtable
import org.bouncycastle.asn1.cms.AttributeTable
import org.bouncycastle.asn1.DERObjectIdentifier
import org.bouncycastle.cms.SignerInformationStore
import com.typesafe.config.ConfigFactory
import com.avricot.avrilog.hash.Hash
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.slf4j.LoggerFactory
import org.bouncycastle.tsp.TimeStampTokenGenerator
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.bouncycastle.tsp.TSPAlgorithms
import java.math.BigInteger
import java.util.Random
import java.util.Date
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.CMSSignedDataParser
import org.bouncycastle.cms.CMSTypedStream
import org.bouncycastle.cms.CMSSignerDigestMismatchException

object Sign extends Hash {
  val logger = LoggerFactory.getLogger(Sign.getClass())

  /**
   * Init the bouncy castle api.
   */
  val timestampingOID = new DERObjectIdentifier("1.2.840.113549.1.9.16.2.14")
  val TSA_POLICY_ID = "1.3.6.1.4.1.3029.54.11940.54"
  val tsaPolicyId = new ASN1ObjectIdentifier(TSA_POLICY_ID)
  val config = ConfigFactory.load()
  val pkcs12Path = config.getString("pkcs12.path")
  val pkcsTsa12Path = config.getString("pkcs12.tsa.path")
  val algo = config.getString("pkcs12.algo")
  val signatureAlgorithm = algo + "withRSA"
  val providerName = "BC"

  //Match the correct algo (get the associated OID)
  val timestampTSPAlgorithms = algo match {
    case "SHA1" => TSPAlgorithms.SHA1
    case "SHA224" => TSPAlgorithms.SHA224
    case "SHA256" => TSPAlgorithms.SHA256
    case "SHA384" => TSPAlgorithms.SHA384
    case "SHA512" => TSPAlgorithms.SHA512
    case "MD5" => TSPAlgorithms.MD5
    case _ => throw new IllegalArgumentException("algo " + algo + " isn't supported. Only SHA1 SHA224 SHA256 SHA384 SHA512 MD5")
  }

  private def getEmptyStringAsNull(str: String): Array[Char] = {
    val conf = config.getString(str)
    if (conf == "") null else conf.toCharArray()
  }
  val pkcs12Password = getEmptyStringAsNull("pkcs12.password")
  val privateKeyPassword = getEmptyStringAsNull("pkcs12.pkeyPassword")

  Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())

  val signKeyStore = new KeyStore(pkcs12Path, pkcs12Password, privateKeyPassword, providerName)
  val timestampKeyStore = new KeyStore(pkcsTsa12Path, pkcs12Password, privateKeyPassword, providerName)
  /**
   * Sign the input and add a timestamp on the signed data.
   */
  def signWithTimestamp(input: Array[Byte]): Array[Byte] = {
    val signedData = getSignedData(input, false)
    if (signedData == null) {
      return null
    }
    try {
      val signers = signedData.getSignerInfos().getSigners()
      val firstSigner = signers.iterator().next().asInstanceOf[SignerInformation]
      val timestampData = Timestamping.timestamp(firstSigner.getSignature())
      val signedDataWithTimestamp = addTimestampToSign(timestampData, signedData, firstSigner)
      signedDataWithTimestamp.getEncoded()
    } catch {
      case e: Throwable => {
        logger.error("can't add the timestamp to the data : ", e)
        try {
          signedData.getEncoded()
        } catch {
          case ioe: Throwable => logger.error("can't retrive original data : ", ioe); null
        }
      }
    }
  }

  /**
   * Sign the data. Just add a local timestamp, do not call remote service.
   */
  def sign(input: Array[Byte]): Array[Byte] = {
    getSignedData(input, true).getEncoded()
  }

  def verifySign(input: Array[Byte], signBlock: Array[Byte]): Boolean = {
    val hash = getRawHash(input, algo)
    val sign = new CMSSignedDataParser(new CMSTypedStream(new ByteArrayInputStream(input)), signBlock)
    val signedContent = sign.getSignedContent()
    signedContent.drain()
    val certStore = sign.getCertificates()
    val it = sign.getSignerInfos().getSigners().iterator()
    //val contentVerifierProvider = new JcaContentVerifierProviderBuilder().setProvider("BC").build(signKeyStore.publickey)
    while (it.hasNext()) {
      val signer = it.next().asInstanceOf[SignerInformation]
      val signerHash = signer.getSignature()
      val sid = signer.getSID()
      val certCollection = certStore.getMatches(null)
      val certIt = certCollection.iterator()
      val cert = certIt.next().asInstanceOf[X509CertificateHolder]
      try {
        if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(signKeyStore.cert))) {
          return false
        }
        logger.trace("sign ok, digest match.")
      } catch {
        case e: CMSSignerDigestMismatchException => return false
      }
      //Check timestamp data.
      val attrs = signer.getUnsignedAttributes()
      if (attrs != null) {
        val att = attrs.get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken)
        val t = PKCSObjectIdentifiers.id_aa_signatureTimeStampToken
        if (att != null) {
          val dob = att.getAttrValues().getObjectAt(0)
          val tto = new TimeStampToken(new CMSSignedData(dob.getDERObject().getEncoded()))
          val digest = tto.getTimeStampInfo().getMessageImprintDigest()
          if (tto.getTimeStampInfo().getMessageImprintDigest().deep != signer.getSignature().deep) {
            return false
          }
          logger.trace("timestamp digest match")
        } else {
          logger.trace("timestamp can't be found on this sign ({})", PKCSObjectIdentifiers.id_aa_signatureTimeStampToken)
        }
      } else {
        logger.trace("unsigned attributes can't be found on this sign ")
      }
    }
    true
  }

  /**
   * Sign the data. Add a local timestamp if needed.
   */
  private def getSignedData(input: Array[Byte], addLocalTimestamp: Boolean) = {
    try {
      //doesn't seem to be thread safe.
      val sha1Signer = new JcaContentSignerBuilder(signatureAlgorithm).setProvider(providerName).build(signKeyStore.privatekey).asInstanceOf[ContentSigner]
      val signerInfo = new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider(providerName).build()).build(sha1Signer, signKeyStore.cert)
      val signGen = new CMSSignedDataGenerator()
      signGen.addCertificates(signKeyStore.store)
      signGen.addSignerInfoGenerator(signerInfo)
      val content = new CMSProcessableByteArray(input)
      val signedData = signGen.generate(content)
      if (addLocalTimestamp) {
        val tsaSigner = new JcaContentSignerBuilder(signatureAlgorithm).setProvider(providerName).build(timestampKeyStore.privatekey).asInstanceOf[ContentSigner]
        val tsaSignerInfo = new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider(providerName).build()).build(tsaSigner, timestampKeyStore.cert)
        val reqGen = new TimeStampRequestGenerator()
        val signers = signedData.getSignerInfos().getSigners()
        val firstSigner = signers.iterator().next().asInstanceOf[SignerInformation]
        val rand = new Random()
        val timestampRequest = reqGen.generate(timestampTSPAlgorithms, firstSigner.getSignature())
        val timestampGenerator = new TimeStampTokenGenerator(tsaSignerInfo, tsaPolicyId)
        val timestamp = timestampGenerator.generate(timestampRequest, BigInteger.valueOf(rand.nextLong()), new Date())
        val signedDataWithTimestamp = addTimestampToSign(timestamp.getEncoded(), signedData, firstSigner)
        signedDataWithTimestamp
      } else {
        signedData
      }
    } catch {
      case e: Throwable => logger.error("can't sign the data : ", e); null
    }
  }

  /**
   * Add a timestamp to a signer info, and return a CMSSignedData with the timestamp in the given signer info.
   */
  private def addTimestampToSign(timestampData: Array[Byte], signedData: CMSSignedData, firstSigner: SignerInformation): CMSSignedData = {
    if (timestampData != null) {
      val cmsData = new CMSSignedData(timestampData)
      val tok = new TimeStampToken(cmsData)
      val asn1InputStream = new ASN1InputStream(tok.getEncoded())
      val tstDER = asn1InputStream.readObject()
      val ds = new DERSet(tstDER)
      val unsignAtt = new Attribute(timestampingOID, ds)
      val dv = new ASN1EncodableVector()
      dv.add(unsignAtt)
      val at = new AttributeTable(dv)
      val newFirstSigner = SignerInformation.replaceUnsignedAttributes(firstSigner, at)
      val newSigners = new ArrayList[SignerInformation]()
      newSigners.add(newFirstSigner)
      val sis = new SignerInformationStore(newSigners)
      val newSignedData = CMSSignedData.replaceSigners(signedData, sis)
      return newSignedData
    }
    signedData
  }
}