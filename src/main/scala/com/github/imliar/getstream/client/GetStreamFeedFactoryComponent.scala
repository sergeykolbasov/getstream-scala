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
     * Get feed
     */
    def feed(feedSlug: String, feedId: String): GetStreamFeed
  }
}

/**
 * Default implementation for feed factory
 */
trait GetStreamFeedFactoryDefaultComponent extends GetStreamFeedFactoryComponent {

  /**
   * Signer for tokens
   */
  //val signer: GetStreamSign

  //values for http mixin
  val serializer: GetStreamSerializer
  val host: String
  val location: String
  val httpClient: Service[HttpRequest, HttpResponse]
  val httpTimeout: Duration
  val apiData: ApiDataProvider

  /**
   * Get instance of feed factory
   */
  def feedFactory: GetStreamFeedFactory = new GetStreamFeedFactoryDefault

  class GetStreamFeedFactoryDefault extends GetStreamFeedFactory {

    /**
     * Get feed with automatically generated token
     */
    override def feed(slug: String, id: String): GetStreamFeed = {

      //val token = tokenOpt getOrElse signer.signature(slug + id)

      val data = apiData
      val ser = serializer
      val client = httpClient
      val timeout = timeout
      val h = host
      val l = location

      new GetStreamFeedImpl with GetStreamHttpClientDefaultComponent {
        override val feedSlug = slug
        override val feedId = id

        //values for http component
        override val httpClient = client
        override val httpTimeout = timeout
        override val serializer = ser
        override val host = h
        override val location = l
        override val apiData = data
      }
    }

  }
}
