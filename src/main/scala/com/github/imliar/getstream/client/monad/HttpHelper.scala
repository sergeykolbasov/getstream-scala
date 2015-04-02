package com.github.imliar.getstream.client.monad

import java.net.{URI, URL}
import com.twitter.finagle.httpx.{Method, RequestBuilder}
import com.twitter.io.Buf.ByteArray
import com.twitter.util.{JavaTimer}
import org.apache.http.client.utils.{URIBuilder}
import org.apache.http.message.BasicNameValuePair
import scala.concurrent.Future
import com.github.imliar.getstream.client.util.Twitter._

trait HttpHelper { self: Injectable with GetStreamFeedOps =>

  private val serializer = bindings.serializer
  private val httpClient = bindings.httpClient
  private val httpTimeout = bindings.httpTimeout
  private val config = bindings.config
  private val signer = bindings.signer

  /**
   * Do request to getstream.io and await response of type T within given timeout
   */
  def makeHttpRequest[T <: AnyRef, A <: AnyRef](uri: URI,
                                                method: Method,
                                                data: A,
                                                params: Seq[BasicNameValuePair] = Seq.empty)
                                               (implicit m1: Manifest[A], m2: Manifest[T]): Future[T] = {
    val requestBuilder = RequestBuilder()
    val payload = Some(serializer serialize data) map (d => ByteArray.Owned(d.getBytes))
    val url = buildRequestUrl(uri, params)

    val token = signer signature feed

    val request = requestBuilder
      .addHeaders(Map(
      "Authorization" -> s"${feed.feedSlug}${feed.feedId} $token",
      "Content-Type" -> "application/json"
    ))
      .url(url)
      .build(method, payload)

    httpClient(request).within(new JavaTimer, httpTimeout).map { response =>
      serializer.deserialize[T](response.contentString)
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
      .setPath(s"api/feed/${feed.feedSlug}/${feed.feedId}$path")

    builder.addParameters(params)
    builder.addParameter("api_key", apiKey)

    builder.build().toURL
  }
}
