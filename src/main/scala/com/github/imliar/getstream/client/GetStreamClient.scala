package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models.Feed
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
   * Get feed with specified slug/id. Token will be created if not provided.
   */
  def feed(feed: Feed): GetStreamFeed
}

/**
 * Basic implementation
 */
trait GetStreamClientImpl extends GetStreamClient { self: GetStreamFeedFactoryComponent =>

  /**
   * Get feed with specified slug/id. Token will be created if not provided.
   */
  override def feed(feed: Feed): GetStreamFeed = {
      feedFactory.feed(feed)
  }

}