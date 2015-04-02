package com.github.imliar.getstream.client

import com.twitter.conversions.time._
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.httpx.{Request, Response, Http}
import com.twitter.finagle.service.RetryPolicy
import com.twitter.util.Duration
import com.typesafe.config.{Config, ConfigFactory}

/**
 * Immutable builder for GetStreamClient
 * @param config Application config with required parameters set
 * @param httpClient Finagle HTTP client
 * @param httpTimeout Default HTTP timeout
 */
class GetStreamClientBuilder(config: Config,
                             httpClient: Option[Service[Request, Response]] = None,
                             httpTimeout: Duration = 30.seconds,
                             serializer: GetStreamSerializer = GetStreamDefaultSerializer) {
  /**
   * Pass another config
   */
  def withConfig(config: Config): GetStreamClientBuilder = {
    copy(config = config)
  }

  /**
   * Pass custom HTTP client instead of default one
   */
  def withHttpClient(httpClient: Service[Request, Response]): GetStreamClientBuilder = {
    copy(httpClient = Some(httpClient))
  }

  /**
   * Use custom HTTP timeout instead of default one
   */
  def withHttpTimeout(httpTimeout: Duration): GetStreamClientBuilder = {
    copy(httpTimeout = httpTimeout)
  }

  def withSerializer(serializer: GetStreamSerializer): GetStreamClientBuilder = {
    copy(serializer = serializer)
  }

  def copy(config: Config = this.config,
           httpClient: Option[Service[Request, Response]] = this.httpClient,
           httpTimeout: Duration = this.httpTimeout,
           serializer: GetStreamSerializer = this.serializer) = {
    new GetStreamClientBuilder(config, httpClient, httpTimeout, serializer)
  }

  /**
   * Build and get instance of GetStreamClient
   */
  def build(): GetStreamClient = {

    val key = config.getString("getstream.api.key")
    val secret = config.getString("getstream.api.secret")
    val version = config.getString("getstream.api.version")

    val client = httpClient.getOrElse(GetStreamClientBuilder.defaultHttpClient(config))

    val ser = serializer

    new GetStreamClientImpl with GetStreamFeedFactoryDefaultComponent {

      override val httpClient: Service[Request, Response] = client
      override val httpTimeout: Duration = 30.seconds

      //override val signer: GetStreamSign = new GetStreamSign(apiSecret)
      override val serializer: GetStreamSerializer = ser

      override val config: Config = config withFallback GetStreamClientBuilder.defaultConfig

      override val signer: GetStreamSign = new GetStreamSign(secret)
    }
  }

}

private object GetStreamClientBuilder {

  def defaultConfig = {
    import scala.collection.JavaConversions.mapAsJavaMap

    ConfigFactory.parseMap(Map(
      "getstream.http.connectionLimit" -> "10",
      "getstream.http.retries" -> "3",
      "getstream.http.host" -> "getstream.io",
      "getstream.http.location" -> "eu-west-api"
    ))
  }

  def defaultHttpClient(config: Config): Service[Request, Response] = {
    val withFallback = config.withFallback(defaultConfig)

    val host = withFallback.getString("getstream.http.host")
    val location = withFallback.getString("getstream.http.location")

    val retries = withFallback.getInt("getstream.http.retries")
    val connectionLimit = withFallback.getInt("getstream.http.connectionLimit")

    ClientBuilder()
      .codec(Http())
      .tls(host)
      .hosts(s"{$location}.{$host}:443")
      .hostConnectionLimit(connectionLimit)
      .retryPolicy(RetryPolicy.tries(retries, RetryPolicy.ChannelClosedExceptionsOnly))
      .build()
  }
}
