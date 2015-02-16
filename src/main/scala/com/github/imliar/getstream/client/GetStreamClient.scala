package com.github.imliar.getstream.client

import com.twitter.finagle.Service
import com.twitter.util.Duration
import com.twitter.conversions.time._
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest}

/**
 * Basic trait for GetStream client.
 * Single instance of GetStreamClient could be used just like a HTTP client
 */
trait GetStreamClient {
  /**
   * API key for getstream app
   */
  val apiKey: String

  /**
   * API secret for getstream app
   */
  val apiSecret: String

  /**
   * API version
   */
  val apiVersion: String

  /**
   * Get feed with specified slug/id. Token will be created if not provided.
   */
  def feed(feedSlug: String, id: String, tokenOpt: Option[String] = None): GetStreamFeed
}

/**
 * Basic implementation
 */
case class GetStreamClientImpl(
                            apiKey: String,
                            apiSecret: String,
                            apiVersion: String
                            )(httpClient: Service[HttpRequest, HttpResponse], httpTimeout: Duration) extends GetStreamClient {

  private val signer = GetStreamSign(apiKey)

  /**
   * Get feed with specified slug/id. Token will be created if not provided.
   */
  override def feed(feedSlug: String, id: String, tokenOpt: Option[String]): GetStreamFeed = {
    val token = tokenOpt match {
      case Some(str) => str
      case _ => signer.signature(feedSlug + id)
    }
    GetStreamFeed(client = this, feedSlug = feedSlug, feedId = id, token = token, apiKey = apiKey)(httpClient, httpTimeout)
  }
}