package com.github.imliar.getstream.client

import java.net.{URI, URL}

import com.github.imliar.getstream.client.exceptions.GetStreamParseException
import com.github.imliar.getstream.client.util.Twitter._
import com.twitter.finagle.httpx.{Method, RequestBuilder}
import com.twitter.io.Buf.ByteArray
import com.twitter.util.JavaTimer
import org.apache.http.client.utils.URIBuilder
import org.apache.http.message.BasicNameValuePair
import com.twitter.conversions.time._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait HttpHelper {
  self: Injectable with GetStreamFeedOps =>

  private def serializer = bindings.serializer
  private def httpClient = bindings.httpClient
  private def httpTimeout = config.getInt("getstream.http.timeout").seconds
  private def config = bindings.config
  private def signer = bindings.signer

  /**
   * Do request to getstream.io and await response of type T within given timeout
   */
  def makeHttpRequest[A <: AnyRef, T <: AnyRef](uri: URI,
                                                method: Method,
                                                data: A,
                                                params: Seq[BasicNameValuePair] = Seq.empty)
                                               (implicit m1: Manifest[A], m2: Manifest[T]): Future[T] = {
    val requestBuilder = RequestBuilder()
    val payload = try {
      Some(serializer serialize data) map (d => ByteArray.Owned(d.getBytes))
    } catch {
      case e: Throwable => None
    }
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
      val content = response.contentString
      Try(serializer.deserialize[T](content)) match {
        case Success(result) => result
        case Failure(e) => {
          throw new GetStreamParseException(uri, method.toString, content, e)
        }
      }
    }.asScala
  }

  /**
   * Build request url with respect to all parameters, location, api key and host
   */
  private def buildRequestUrl(uri: URI, params: Seq[BasicNameValuePair]): URL = {
    import scala.collection.JavaConversions._

    val location = config.getString("getstream.http.location")
    val host = config.getString("getstream.http.host")
    val apiKey = config.getString("getstream.api.key")
    val apiVersion = config.getString("getstream.api.version")

    val path = Some(uri.getPath).filter(_.nonEmpty).map("/" + _ + "/").getOrElse("/")

    val builder = new URIBuilder()
    builder
      .setScheme("https")
      .setHost(s"$location.$host")
      .setPath(s"/api/$apiVersion/feed/${feed.feedSlug}/${feed.feedId}$path")

    builder.addParameters(params)
    builder.addParameter("api_key", apiKey)

    builder.build().toURL
  }
}
