package com.github.imliar.getstream.client

import com.twitter.finagle.Service
import com.twitter.util.Duration
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

/**
 * Trait for feed factory. Implement cake-pattern to avoid usage of 3rd-party DI libraries
 */
trait GetStreamFeedFactoryComponent {

  /**
   * Get instance of feed factory
   */
  def feedFactory: GetStreamFeedFactory

  trait GetStreamFeedFactory {

    /**
     * Get feed with automatically generated token
     */
    def feed(apiKey: String, apiVersion: String, feedSlug: String, feedId: String)
            (httpClient: Service[HttpRequest, HttpResponse], httpTimeout: Duration): GetStreamFeed

    /**
     * Get feed with provided token
     */
    def feed(apiKey: String, apiVersion: String, feedSlug: String, feedId: String, token: String)
            (httpClient: Service[HttpRequest, HttpResponse], httpTimeout: Duration): GetStreamFeed
  }
}

/**
 * Default implementation for feed factory
 */
trait GetStreamFeedFactoryDefaultComponent extends GetStreamFeedFactoryComponent {

  /**
   * Signer for tokens
   */
  val signer: GetStreamSign

  /**
   * Get instance of feed factory
   */
  def feedFactory: GetStreamFeedFactory = new GetStreamFeedFactoryDefault

  class GetStreamFeedFactoryDefault extends GetStreamFeedFactory {

    /**
     * Get feed with automatically generated token
     */
    override def feed(apiKey: String, apiVersion: String, feedSlug: String, feedId: String)
                     (httpClient: Service[HttpRequest, HttpResponse], httpTimeout: Duration): GetStreamFeed = {

      val token = signer.signature(feedSlug + feedId)
      val apiData = ApiDataProvider(apiVersion, apiKey, token)
      val s = signer

      new GetStreamFeedImpl(feedSlug, feedId, apiData, httpClient, httpTimeout)
        with GetStreamFeedFactoryDefaultComponent {
          val signer: GetStreamSign = s
        }
    }

    /**
     * Get feed with provided token
     */
    override def feed(apiKey: String, apiVersion: String, feedSlug: String, feedId: String, token: String)
                     (httpClient: Service[HttpRequest, HttpResponse], httpTimeout: Duration): GetStreamFeed = {

      val apiData = ApiDataProvider(apiVersion, apiKey, token)
      val s = signer

      new GetStreamFeedImpl(feedSlug, feedId, apiData, httpClient, httpTimeout)
        with GetStreamFeedFactoryDefaultComponent {
          val signer = s
        }
    }
  }
}
