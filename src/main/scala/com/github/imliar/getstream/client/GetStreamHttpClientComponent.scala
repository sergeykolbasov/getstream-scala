package com.github.imliar.getstream.client

import java.net.URL

import com.twitter.finagle.Service
import com.twitter.util.Duration
import org.apache.http.message.BasicNameValuePair
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpResponse, HttpRequest}

import scala.concurrent.Future

trait GetStreamHttpClientComponent {

  /**
   * HTTP client
   */
  val http: GetStreamHttpClient

  trait GetStreamHttpClient {

    /**
     * Do request to getstream.io
     */
    def makeHttpRequest[A <: AnyRef](url: URL,
                                     method: HttpMethod,
                                     data: A,
                                     params: Seq[BasicNameValuePair] = Seq.empty)(implicit man: Manifest[A]): Future[String]
  }

}

trait GetStreamHttpClientDefaultComponent extends GetStreamHttpClientComponent {

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

  val http = new GetStreamHttpDefaultClient(httpClient, httpTimeout)

  class GetStreamHttpDefaultClient(httpClient: Service[HttpRequest, HttpResponse],
                                   httpTimeout: Duration) extends GetStreamHttpClient {

    override def makeHttpRequest[A <: AnyRef](url: URL,
                                     method: HttpMethod,
                                     data: A,
                                     params: Seq[BasicNameValuePair] = Seq.empty)(implicit man: Manifest[A]): Future[String] = ???
  }
}
