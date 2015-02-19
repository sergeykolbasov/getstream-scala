package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models.ApiDataProvider
import com.twitter.finagle.Service
import com.twitter.util.Duration
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

/**
 * Trait for feed factory.
 * Use kind of cake-pattern to avoid usage of 3rd-party DI libraries
 * and allow to easily override factory/feed to implement own behaviour
 */
trait GetStreamFeedFactoryComponent {

  /**
   * Get instance of feed factory
   */
  def feedFactory: GetStreamFeedFactory

  trait GetStreamFeedFactory {

    /**
     * Get feed. Token will be created if not provided.
     */
    def feed(apiKey: String, apiVersion: String, feedSlug: String, feedId: String, tokenOpt: Option[String] = None)
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

  val serializer: GetStreamSerializer

  /**
   * Get instance of feed factory
   */
  def feedFactory: GetStreamFeedFactory = new GetStreamFeedFactoryDefault

  class GetStreamFeedFactoryDefault extends GetStreamFeedFactory {

    /**
     * Get feed with automatically generated token
     */
    override def feed(apiKey: String, apiVersion: String, slug: String, id: String, tokenOpt: Option[String] = None)
                     (client: Service[HttpRequest, HttpResponse], timeout: Duration): GetStreamFeed = {

      val token = tokenOpt getOrElse signer.signature(slug + id)
      val data = ApiDataProvider(apiVersion, apiKey, token)
      val sig = signer
      val ser = serializer

      new GetStreamFeedImpl with GetStreamFeedFactoryDefaultComponent with GetStreamHttpClientDefaultComponent {
        val feedSlug = slug
        val feedId = id
        val apiData = data

        val httpClient = client
        val httpTimeout = timeout

        val signer = sig
        val serializer: GetStreamSerializer = ser
      }
    }

  }
}
