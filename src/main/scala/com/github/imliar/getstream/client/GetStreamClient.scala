package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models.Feed
import com.twitter.conversions.time._
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.httpx.{Http, Request, Response}
import com.twitter.finagle.service.RetryPolicy
import com.twitter.util.Duration
import com.typesafe.config.{Config, ConfigFactory}

/**
 * GetStreamClient
 *
 * Default client to deal with getstream.io servers.
 * It has predefined values and ready to work, but its easy to override them on/after instantiation
 *
 * @param config Config with required values (api key, token)
 * @param httpTimeout Max timeout for HTTP request
 * @param httpClient HTTP Client
 * @param ops Feed operations implementation
 * @param serializer Request/Response JSON serializer
 */
case class GetStreamClient(config: Config = DefaultValues.defaultConfig,
                           httpTimeout: Duration = 30.seconds,
                           httpClient: Config => HttpClient = DefaultValues.defaultHttpClient,
                           ops: Feed => Bindings => GetStreamFeedOps = DefaultValues.defaultOps,
                           serializer: GetStreamSerializer = GetStreamDefaultSerializer) { self =>


  /**
   * List of bindings to use inside operations etc
   */
  private implicit val bindings = new Bindings {
    override val config: Config = self.config
    override val httpClient: HttpClient = self.httpClient(config)
    override val serializer: GetStreamSerializer = self.serializer
    override val httpTimeout: Duration = self.httpTimeout
    override val signer: GetStreamSign = new GetStreamSign(config getString "getstream.api.secret")
  }

  /**
   * Pass another config
   */
  def withConfig(config: Config): GetStreamClient = {
    copy(config = config)
  }

  /**
   * Pass custom HTTP client instead of default one
   */
  def withHttpClient(httpClient: Service[Request, Response]): GetStreamClient = {
    copy(httpClient = _ => httpClient)
  }

  /**
   * Pass custom feed operations trait
   */
  def withOps(ops: Feed => Bindings => GetStreamFeedOps): GetStreamClient = {
    copy(ops = ops)
  }

  /**
   * Pass custom (de)serializer
   */
  def withSerializer(serializer: GetStreamSerializer): GetStreamClient = {
    copy(serializer = serializer)
  }

  /**
   * Apply feed and get available operations for it
   * @param feed
   * @return
   */
  def apply(feed: Feed): GetStreamFeedOps = {
    ops(feed)(bindings)
  }

}

object DefaultValues {

  def defaultConfig = {
    import scala.collection.JavaConversions.mapAsJavaMap

    ConfigFactory.parseMap(Map(
      "getstream.http.connectionLimit" -> "10",
      "getstream.http.retries" -> "3",
      "getstream.http.host" -> "getstream.io",
      "getstream.http.location" -> "eu-west-api"
    ))
  }

  def defaultHttpClient(config: Config): HttpClient = {
    val withFallback = config.withFallback(defaultConfig)

    val host = withFallback.getString("getstream.http.host")
    val location = withFallback.getString("getstream.http.location")

    val retries = withFallback.getInt("getstream.http.retries")
    val connectionLimit = withFallback.getInt("getstream.http.connectionLimit")

    ClientBuilder()
      .codec(Http())
      .tls(host)
      .hosts(s"$location.$host:443")
      .hostConnectionLimit(connectionLimit)
      .retryPolicy(RetryPolicy.tries(retries, RetryPolicy.ChannelClosedExceptionsOnly))
      .build()
  }

  def defaultOps(feed: Feed)(bindings: Bindings): GetStreamFeedOps = {
    val f = feed
    val b = bindings
    new GetStreamFeedOps with Injectable {
      override val feed: Feed = f
      override protected implicit val bindings: Bindings = b
    }
  }

}