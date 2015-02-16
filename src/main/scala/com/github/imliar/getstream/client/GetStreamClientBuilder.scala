package com.github.imliar.getstream.client

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.service.RetryPolicy
import com.typesafe.config.{ConfigFactory, Config}
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest}
import com.twitter.util.Duration
import com.twitter.conversions.time._

/**
 * Immutable builder for GetStreamClient
 * @param config Application config with required parameters set
 * @param httpClient Finagle HTTP client
 * @param httpTimeout Default HTTP timeout
 */
class GetStreamClientBuilder(config: Config,
                                  httpClient: Option[Service[HttpRequest, HttpResponse]] = None,
                                  httpTimeout: Duration = 30.seconds) {
  /**
   * Pass another config
   */
  def withConfig(config: Config): GetStreamClientBuilder = {
    copy(config = config)
  }

  /**
   * Pass custom HTTP client instead of default one
   */
  def withHttpClient(httpClient: Service[HttpRequest, HttpResponse]) = {
    copy(httpClient = Some(httpClient))
  }

  /**
   * Use custom HTTP timeout instead of default one
   */
  def withHttpTimeout(httpTimeout: Duration) = {
    copy(httpTimeout = httpTimeout)
  }

  def copy(config: Config = this.config,
           httpClient: Option[Service[HttpRequest, HttpResponse]] = this.httpClient,
           httpTimeout: Duration = this.httpTimeout) = {
    new GetStreamClientBuilder(config, httpClient, httpTimeout)
  }

  /**
   * Build and get instance of GetStreamClient
   */
  def build(): GetStreamClient = {

    val apiKey = config.getString("getstream.api.key")
    val apiSecret = config.getString("getstream.api.secret")
    val apiVersion = config.getString("getstream.api.version")

    val httpClient = this.httpClient.getOrElse(GetStreamClientBuilder.defaultHttpClient(config))

    new GetStreamClientImpl(
      apiKey = apiKey,
      apiSecret = apiSecret,
      apiVersion = apiVersion
    )(httpClient = httpClient, httpTimeout = httpTimeout)
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

  def defaultHttpClient(config: Config): Service[HttpRequest, HttpResponse] = {
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
