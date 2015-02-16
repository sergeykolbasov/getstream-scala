package com.github.imliar.getstream.client

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.apache.commons.codec.binary.Base64

/**
 * Signer for requests to getstream api
 */
case class GetStreamSign(key: String) {

  private val b64 = new Base64(true)
  private val md = java.security.MessageDigest.getInstance("SHA-1")
  private val hashedKey = md.digest(key.getBytes("UTF-8"))

  /**
   * Encode byte array to Base64
   */
  private def safeB64encode(value: Array[Byte]): String = b64.encodeToString(value)

  /**
   * Get HMAC for given API secret key and value
   */
  private def hmac(value: String, key: Array[Byte]): Array[Byte] = {
    val secret = new SecretKeySpec(key, "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(secret)
    mac.doFinal(value.getBytes())
  }

  /**
   * Returns URL-safe base64 representation of HMAC sign built on top of API secret key
   */
  def signature(value: String): String = {
    val digest = hmac(value, hashedKey)
    safeB64encode(digest).trim
  }

}
