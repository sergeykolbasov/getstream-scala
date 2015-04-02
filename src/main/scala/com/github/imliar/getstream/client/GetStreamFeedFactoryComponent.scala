package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models.{Feed, Tokenized}
import com.twitter.finagle.Service
import com.twitter.finagle.httpx.{Response, Request}
import com.twitter.util.Duration
import com.typesafe.config.Config
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
  protected def feedFactory: GetStreamFeedFactory

  trait GetStreamFeedFactory {

    /**
     * Get feed
     */
    def feed(feed: Feed): GetStreamFeed with Tokenized
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

  //values for http mixin
  protected val serializer: GetStreamSerializer
  protected val httpClient: Service[Request, Response]
  protected val httpTimeout: Duration
  protected val config: Config

  /**
   * Get instance of feed factory
   */
  protected def feedFactory: GetStreamFeedFactory = new GetStreamFeedFactoryDefault

  class GetStreamFeedFactoryDefault extends GetStreamFeedFactory {

    /**
     * Get feed with automatically generated token
     */
    override def feed(feed: Feed): GetStreamFeed with Tokenized= {

      //val token = tokenOpt getOrElse signer.signature(slug + id)

      val ser = serializer
      val client = httpClient
      val timeout = httpTimeout
      val cfg = config

      new GetStreamFeedImpl with GetStreamHttpClientDefaultComponent with Tokenized {
        override val feedSlug = feed.feedSlug
        override val feedId = feed.feedId

        //values for http component
        override val httpClient = client
        override val httpTimeout = timeout
        override val serializer = ser
        override val config: Config = cfg

        //token
        override val token: String = signer.signature(feed)
      }
    }

  }
}
