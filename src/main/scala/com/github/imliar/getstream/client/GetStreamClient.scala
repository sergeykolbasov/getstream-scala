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
   * Finagle Http Client
   */
  val httpClient: Service[HttpRequest, HttpResponse]

  /**
   * Maximum timeout duration
   */
  val httpTimeout: Duration

  /**
   * Get feed with specified slug/id. Token will be created if not provided.
   */
  def feed(feedSlug: String, id: String, tokenOpt: Option[String] = None): GetStreamFeed
}

/**
 * Basic implementation
 */
trait GetStreamClientImpl extends GetStreamClient { self: GetStreamFeedFactoryComponent =>

  /**
   * Get feed with specified slug/id. Token will be created if not provided.
   */
  override def feed(feedSlug: String, feedId: String, tokenOpt: Option[String]): GetStreamFeed = {
      feedFactory.feed(apiKey, apiVersion, feedSlug, feedId, tokenOpt)(httpClient, httpTimeout)
  }

}