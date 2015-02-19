package com.github.imliar.getstream.client

import java.net.URL
import com.github.imliar.getstream.client.models.ApiDataProvider
import com.twitter.finagle.Service
import com.twitter.finagle.http.RequestBuilder
import com.twitter.util.Duration
import org.apache.http.message.BasicNameValuePair
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpResponse, HttpRequest}
import org.jboss.netty.buffer.ChannelBuffers._
import scala.concurrent.Future

trait GetStreamHttpClientComponent {

  /**
   * HTTP client
   */
  val http: GetStreamHttpClient

  trait GetStreamHttpClient {

    /**
     * Do request to getstream.io and await response of type T
     */
    def makeHttpRequest[T, A <: AnyRef](url: URL,
                                     method: HttpMethod,
                                     data: A,
                                     params: Seq[BasicNameValuePair] = Seq.empty)(implicit man: Manifest[A]): Future[T]
  }

}

trait GetStreamHttpClientDefaultComponent extends GetStreamHttpClientComponent { self: GetStreamFeed =>

  /**
   * End-point host
   */
  val host: String

  /**
   * End-point location
   */
  val location: String

  /**
   * Finagle Http client
   */
  val httpClient: Service[HttpRequest, HttpResponse]

  /**
   * Max timeout for Http client
   */
  val httpTimeout: Duration

  /**
   * JSON (de)serializer for data
   */
  val serializer: GetStreamSerializer

  val apiData: ApiDataProvider

  val http = new GetStreamHttpDefaultClient(httpClient, httpTimeout)

  class GetStreamHttpDefaultClient(httpClient: Service[HttpRequest, HttpResponse],
                                   httpTimeout: Duration) extends GetStreamHttpClient {

    //private val baseFeedUrl = new URL()//s"feed/$feedSlug/$feedId"
    //httpClient.

    override def makeHttpRequest[T, A <: AnyRef](url: URL,
                                     method: HttpMethod,
                                     data: A,
                                     params: Seq[BasicNameValuePair] = Seq.empty)(implicit man: Manifest[A]): Future[T] = {
      val requestBuilder = RequestBuilder()
      val payload = Some(serializer serialize data).map(d => wrappedBuffer(d.getBytes))
    }

    /*private def buildRequestUrl(url: String, requestParams: Map[String, Any] = Map.empty) = {
      val queryParams = (requestParams.map {
        case (param, value) => s"$param=${value.toString}"
      }.toSeq :+ s"api_key=$apiKey").mkString("&")
      val urlString = s"${GetStreamFeed.API_ENDPOINT}/${client.apiVersion}/$url"

      if (queryParams.nonEmpty) {
        new URL(urlString + "?" + queryParams)
      } else {
        new URL(urlString)
      }
    }*/
  }
}
