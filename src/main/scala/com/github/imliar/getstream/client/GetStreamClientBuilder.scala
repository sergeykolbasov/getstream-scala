package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models.ApiDataProvider
import com.twitter.conversions.time._
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.service.RetryPolicy
import com.twitter.util.Duration
import com.typesafe.config.{Config, ConfigFactory}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

/**
 * Immutable builder for GetStreamClient
 * @param config Application config with required parameters set
 * @param httpClient Finagle HTTP client
 * @param httpTimeout Default HTTP timeout
 */
class GetStreamClientBuilder(config: Config,
                             httpClient: Option[Service[HttpRequest, HttpResponse]] = None,
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
  def withHttpClient(httpClient: Service[HttpRequest, HttpResponse]): GetStreamClientBuilder = {
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
           httpClient: Option[Service[HttpRequest, HttpResponse]] = this.httpClient,
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

    val httpBuilder = GetStreamClientBuilder.defaultHttpClient(config)

    val client = httpClient.getOrElse(httpBuilder.build())

    val ser = serializer

    new GetStreamClientImpl with GetStreamFeedFactoryDefaultComponent {

      override val apiData: ApiDataProvider = ApiDataProvider(
        apiVersion = version,
        apiKey = key,
        token = None,
        secret = secret
      )

      override val host: String = httpBuilder.host
      override val location: String = httpBuilder.location
      override val httpClient: Service[HttpRequest, HttpResponse] = client
      override val httpTimeout: Duration = 30.seconds

      //override val signer: GetStreamSign = new GetStreamSign(apiSecret)
      override val serializer: GetStreamSerializer = ser
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

  def defaultHttpClient(config: Config): HttpClientBuilder = {
    val withFallback = config.withFallback(defaultConfig)

    val host = withFallback.getString("getstream.http.host")
    val location = withFallback.getString("getstream.http.location")

    val retries = withFallback.getInt("getstream.http.retries")
    val connectionLimit = withFallback.getInt("getstream.http.connectionLimit")

    HttpClientBuilder(host, location, retries, connectionLimit)
  }

}

private case class HttpClientBuilder(host: String, location: String, retries: Int, connectionLimit: Int) {

  def build(): Service[HttpRequest, HttpResponse] = {
    ClientBuilder()
      .codec(Http())
      .tls(host)
      .hosts(s"{$location}.{$host}:443")
      .hostConnectionLimit(connectionLimit)
      .retryPolicy(RetryPolicy.tries(retries, RetryPolicy.ChannelClosedExceptionsOnly))
      .build()
  }

}
