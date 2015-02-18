package com.github.imliar.getstream.client

import com.twitter.finagle.Service
import com.twitter.util.Duration
import com.twitter.conversions.time._
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest}

/**
 * Basic trait for GetStream client.
 * Single instance of GetStreamClient could be used just like a HTTP client
 */
trait GetStreamClient { self: GetStreamFeedFactoryComponent =>
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
                            )(httpClient: Service[HttpRequest, HttpResponse], httpTimeout: Duration)
  extends GetStreamClient with GetStreamFeedFactoryDefaultComponent {


  /**
   * Get feed with specified slug/id. Token will be created if not provided.
   */
  override def feed(feedSlug: String, feedId: String, tokenOpt: Option[String]): GetStreamFeed = {
    tokenOpt match {
      case Some(token) => feedFactory.feed(apiKey, apiVersion, feedSlug, feedId, token)(httpClient, httpTimeout)
      case _ => feedFactory.feed(apiKey, apiVersion, feedSlug, feedId)(httpClient, httpTimeout)
    }
  }

  /**
   * Signer for tokens
   */
  override val signer: GetStreamSign = new GetStreamSign(apiSecret)
}