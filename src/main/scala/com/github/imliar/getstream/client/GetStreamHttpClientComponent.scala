package com.github.imliar.getstream.client

import java.net.{URI, URL}
import com.github.imliar.getstream.client.models.Tokenized
import com.twitter.finagle.Service
import com.twitter.finagle.http.RequestBuilder
import com.twitter.io.Charsets
import com.twitter.util.{JavaTimer, Duration}
import com.typesafe.config.Config
import org.apache.http.client.utils.{URIBuilder}
import org.apache.http.message.BasicNameValuePair
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpResponse, HttpRequest}
import org.jboss.netty.buffer.ChannelBuffers._
import scala.concurrent.Future
import util.Twitter._

trait GetStreamHttpClientComponent {

  /**
   * HTTP client
   */
  protected val http: GetStreamHttpClient

  trait GetStreamHttpClient {

    /**
     * Do request to getstream.io and await response of type T within given timeout
     */
    def makeHttpRequest[T <: AnyRef, A <: AnyRef](uri: URI,
                                        method: HttpMethod,
                                        data: A,
                                        params: Seq[BasicNameValuePair] = Seq.empty)
                                                  (implicit m1: Manifest[A], m2: Manifest[T]): Future[T]
  }

}

trait GetStreamHttpClientDefaultComponent extends GetStreamHttpClientComponent { self: GetStreamFeed with Tokenized =>

  /**
   * GetStream config
   */
  protected val config: Config

  /**
   * Finagle Http client
   */
  protected val httpClient: Service[HttpRequest, HttpResponse]

  /**
   * Max timeout for Http client
   */
  protected val httpTimeout: Duration

  /**
   * JSON (de)serializer for data
   */
  protected val serializer: GetStreamSerializer

  /**
   * HTTP client
   */
  protected val http = new GetStreamHttpDefaultClient(httpClient, httpTimeout)

  class GetStreamHttpDefaultClient(httpClient: Service[HttpRequest, HttpResponse],
                                   httpTimeout: Duration) extends GetStreamHttpClient {

    /**
     * Do request to getstream.io and await response of type T within given timeout
     */
    override def makeHttpRequest[T <: AnyRef, A <: AnyRef](uri: URI,
                                     method: HttpMethod,
                                     data: A,
                                     params: Seq[BasicNameValuePair] = Seq.empty)
                                                (implicit m1: Manifest[A], m2: Manifest[T]): Future[T] = {
      val requestBuilder = RequestBuilder()
      val payload = Some(serializer serialize data).map(d => wrappedBuffer(d.getBytes))
      val url = buildRequestUrl(uri, params)

      val request = requestBuilder
        .addHeaders(Map(
        "Authorization" -> s"$feedSlug$feedId $token",
        "Content-Type" -> "application/json"
        ))
        .url(url)
        .build(method, payload)

      httpClient(request).within(new JavaTimer, httpTimeout).map{ response =>
        serializer.deserialize[T](response.getContent toString Charsets.Utf8)
      }.asScala
    }

    /**
     * Build request url with respect to all parameters, location, api key and host
     */
    private def buildRequestUrl(uri: URI, params: Seq[BasicNameValuePair]): URL = {
      import scala.collection.JavaConversions._

      val location = config.getString("getstream.http.location")
      val host = config.getString("getstream.http.host")
      val apiKey = config.getString("getstream.api.apiKey")

      val path = Some(uri.getPath).filter(_.nonEmpty).map("/" + _ + "/").getOrElse("")

      val builder = new URIBuilder()
      builder
        .setScheme("https")
        .setHost(s"$location.$host")
        .setPath(s"api/feed/$feedSlug/$feedId$path")

      builder.addParameters(params)
      builder.addParameter("api_key", apiKey)

      builder.build().toURL
    }
  }
}
