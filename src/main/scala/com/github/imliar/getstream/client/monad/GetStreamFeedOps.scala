package com.github.imliar.getstream.client.monad

import com.github.imliar.getstream.client.models.{Activity, Feed}
import scala.concurrent.Future


/**
 * Operations to work with provided feed
 */
trait GetStreamFeedOps extends HttpHelper { self: Injectable =>

  /**
   * Feed to operate
   */
  val feed: Feed

  /**
   * Add new activity
   * @return Future containing `Activity` with id, provided by getstream.io
   */
  def addActivity[T](activity: Activity[T])(implicit m: Manifest[T], bindings: Bindings): Future[Activity[T]] = ???

  /**
   * Add new activities
    @return Future containing sequence of activities with ids, provided by getstream.io
   */
  def addActivities[T](activities: Seq[Activity[T]])(implicit m: Manifest[T], bindings: Bindings): Future[Seq[Activity[T]]] = ???

  /**
   * Get activities from feed
   * @param from start request from this activity id (using range-based pagination)
   * @param limit limit number of requests
   */
  def getActivities[T](from: Option[String] = None, limit: Int = 25)(implicit m: Manifest[T], bindings: Bindings): Future[Seq[Activity[T]]] = ???

  /**
   * Follow provided feed
   */
  def followFeed(feedToFollow: Feed)(implicit bindings: Bindings): Future[Boolean] = ???

  /**
   * Unfollow provided feed
   */
  def unfollowFeed(feedToUnfollow: Feed)(implicit bindings: Bindings): Future[Boolean] = ???

  /**
   * Get current feed followers
   */
  def followers(from: Option[String], limit: Int = 25)(implicit bindings: Bindings): Future[Seq[Feed]] = ???

  /**
   * Get list of following feeds
   */
  def following(from: Option[String], limit: Int = 25)(implicit bindings: Bindings): Future[Seq[Feed]] = ???

  /**
   * Drop current feed
   */
  def deleteFeed()(implicit bindings: Bindings): Future[Boolean] = ???

}




//case class Get

/*
case class GetStreamFeed(feedSlug: String,
                         feedId: String,
                         apiKey: String,
                         apiVersion: String,
                         token: String)
                        (httpClient: Service[HttpRequest, HttpResponse], timeout: Duration) { self: GetStreamFeedFactoryComponent =>

  require(feedSlug.matches("^\\w+$"), "feedSlug can only contain alphanumeric characters or underscores")

  //implicit val formats = JsonObjectUtils.formats
  import GetStreamFeed._

  private val baseFeedUrl = s"feed/$feedSlug/$feedId"

  private def buildRequestUrl(url: String, requestParams: Map[String, Any] = Map.empty) = {
    val queryParams = (requestParams.map {
      case (param, value) => s"$param=${value.toString}"
    }.toSeq :+ s"api_key=$apiKey").mkString("&")
    val urlString = s"${GetStreamFeed.API_ENDPOINT}/${apiVersion}/$url"

    if (queryParams.nonEmpty) {
      new URL(urlString + "?" + queryParams)
    } else {
      new URL(urlString)
    }
  }

  def signToField(to: Seq[String]): Seq[String] = {
    to.map { t =>
      val feedAndId = t.split(":")
      val (feedSlug, feedId) = (feedAndId(0), feedAndId(1))
      val recipientFeed = feedFactory.feed(feedSlug, feedId)
      val recipientToken = recipientFeed.token
      s"""$feedSlug:$feedId $recipientToken"""
    }.toSeq
  }

  private def signActivity(activity: Activity) = {
    activity.copy(to = signToField(activity.to))
  }

  def addActivity(activity: Activity): Future[Activity] = {
    val signedActivity = signActivity(activity)

    makeHttpRequest(baseFeedUrl + "/", HttpMethod.POST, Some(JsonObjectUtils.toJson(signedActivity))).map(result => {
      Try(result.extract[Activity]) match {
        case Success(activity) => activity
        case Failure(e) => throw GetStreamParseException(e, result)
      }
    })
  }

  /*
  def addActivities(activities: Seq[Activity]): Future[JValue] = {
    val signedActivities = activities.map(signActivity)
    val data = Map("activities" -> signedActivities)

    makeHttpRequest(baseFeedUrl + "/", HttpMethod.POST, Some(JsonObjectUtils.toJson(data)))
  }*/

  def removeActivity(activityId: String, foreignId: Boolean = false): Future[Boolean] = {
    val queryParams = foreignId match {
      case true => Map("foreign_id" -> true)
      case _ => Map.empty[String, Boolean]
    }

    makeHttpRequest(s"$baseFeedUrl/$activityId/", HttpMethod.DELETE, None, queryParams).map { result =>
      Try((result \ "removed").extract[String] == activityId) match {
        case Success(r) => r
        case Failure(e) => throw GetStreamParseException(e, result)
      }
    }
  }

  def getActivities[T](from: Option[String], limit: Long = 25): Future[Seq[Activity]] = {
    val queryParams =
      Map("limit" -> limit) ++
        from.map {
          fromId => Map("id_lt" -> fromId)
        }.getOrElse(Map.empty)

    makeHttpRequest(baseFeedUrl + "/", HttpMethod.GET, None, queryParams).map(result => {
      Try((result \ "results").extract[Seq[Activity]]) match {
        case Success(r) => r
        case Failure(e) => throw GetStreamParseException(e, result)
      }
    })
  }

  def followFeed(feed: GetStreamFeedDescriptor): Future[Boolean] = followFeed(feed.slug, feed.id)

  def followFeed(targetFeedSlug: String, targetId: String): Future[Boolean] = {
    val targetFeedId = s"$targetFeedSlug:$targetId"
    val targetToken = client.feed(targetFeedSlug, targetId).token

    val data = Map("target" -> targetFeedId, "target_token" -> targetToken)

    makeHttpRequest(s"$baseFeedUrl/follows/", HttpMethod.POST, Some(JsonObjectUtils.toJson(data))).map { result =>
      Try((result \ "duration").toOption.isDefined) match {
        case Success(r) => r
        case Failure(e) => throw GetStreamParseException(e, result)
      }
    }
  }

  //scalastyle:off
  def followers(offset: Long = 0, limit: Long = 25): Future[JValue] = {
    //scalastyle:on
    val queryParams = Map("limit" -> limit, "offset" -> offset)

    makeHttpRequest(s"$baseFeedUrl/followers/", HttpMethod.GET, None, queryParams)
  }

  //scalastyle:off
  def following(offset: Long = 0, limit: Long = 25, filter: Seq[String] = Seq.empty): Future[Seq[TimelineFeed]] = {
    //scalastyle:on
    val queryParams = Map(
      "limit" -> limit,
      "offset" -> offset,
      "filter" -> filter.mkString(",")
    )

    makeHttpRequest(s"$baseFeedUrl/follows/", HttpMethod.GET, None, queryParams).map { result =>
      Try((result \ "results").as[Seq[TimelineFeed]]) match {
        case Success(r) => r
        case Failure(e) => throw GetStreamParseException(e, result)
      }
    }
  }

  def unfollowFeed(feed: GetStreamFeedDescriptor): Future[Boolean] = {
    unfollowFeed(feed.slug, feed.id)
  }

  def unfollowFeed(targetFeedSlug: String, targetId: String): Future[Boolean] = {
    val targetFeed = s"$targetFeedSlug:$targetId"
    makeHttpRequest(s"$baseFeedUrl/follows/$targetFeed/", HttpMethod.DELETE, None).map { result =>
      Try((result \ "duration").toOption.isDefined) match {
        case Success(r) => r
        case Failure(e) => throw GetStreamParseException(e, result)
      }
    }
  }

  def delete: Future[JValue] = {
    makeHttpRequest(baseFeedUrl + "/", HttpMethod.DELETE, None)
  }

  private def makeHttpRequest(url: String,
                              method: HttpMethod,
                              data: Option[String],
                              queryParams: Map[String, Any] = Map.empty[String, String]): Future[JValue] = {

    val requestBuilder = RequestBuilder()
    val payload = data.map(d => wrappedBuffer(d.getBytes))

    val urlObj = buildRequestUrl(url, queryParams)
    val request = requestBuilder
      .addHeaders(Map(
      "Authorization" -> s"$feedSlug$feedId $token",
      "Content-Type" -> "application/json"
    ))
      .url(urlObj)
      .build(method, payload)

    httpClient(request).within(new JavaTimer, timeout).map { response =>
      JsonObjectUtils readAsJValue response.getContent.toString(Charset.forName("UTF-8"))
    }
  }
}



case class GetStreamParseException(e: Throwable, response: JValue) extends Throwable {
  override def toString: String = s"GetStreamParseException(e=$e, response=${JsonObjectUtils.toJson(response)})"
}



object GetStreamFeed {
  val API_HOST: String = "eu-west-api.getstream.io"
  val API_ENDPOINT: String = s"https://$API_HOST/api"

  implicit val timelineFeedReader = new Reader[TimelineFeed] {
    override def read(value: json4s.JValue): TimelineFeed = {
      value match {
        case JObject(feedObj) =>
          feedObj.collectFirst {
            case ("target_id", JString(target_id)) =>
              ru.glopart.services.timeline.getstream.GetStreamFeedDescriptor.unapply(target_id)
          } match {
            case Some(Some(tf)) => tf
            case _ => throw new MappingException("Can't convert %s to BigInt." format value)
          }
        case _ => throw new MappingException("Can't convert %s to BigInt." format value)
      }
    }
  }
  implicit val timelineFeedSeqReader: Reader[Seq[TimelineFeed]] = DefaultReaders.traversableReader[Seq, TimelineFeed]
}
*/