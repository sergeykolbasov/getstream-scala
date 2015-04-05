package com.github.imliar.getstream.client

import java.net.URI

import com.github.imliar.getstream.client.models.{ResultsResponse, Activity, Feed}
import com.twitter.finagle.httpx.Method.{Delete, Get, Post}
import org.apache.http.message.BasicNameValuePair

import scala.concurrent.{ExecutionContext, Future}


/**
 * Operations to work with provided feed
 */
trait GetStreamFeedOps extends HttpHelper { self: Injectable =>

  /**
   * Shortcuts for simple request/response types
   */
  private type MRequest = Map[String, String]
  private type MResponse = Map[String, Any]
  private type No = Option[Nothing]

  /**
   * Feed to operate
   */
  val feed: Feed

  /**
   * Add new activity
   * @return Future containing `Activity` with id, provided by getstream.io
   */
  def addActivity[T](activity: Activity[T])(implicit m: Manifest[T]): Future[Activity[T]] = {
    //@TODO sign To field
    makeHttpRequest(new URI(""), Post, activity)
  }

  /**
   * Add new activities
    @return Future containing sequence of activities with ids, provided by getstream.io
   */
  def addActivities[T](activities: Seq[Activity[T]])(implicit m: Manifest[T]): Future[Seq[Activity[T]]] = {
    //@TODO sign To field
    val activitiesMap = Map("activities" -> activities)
    makeHttpRequest(new URI(""), Post, activitiesMap)
  }

  /**
   * Get activities from feed
   * @param from get activities before this id (using range-based pagination)
   * @param limit limit number of requests
   */
  def getActivities[T](from: Option[String] = None, limit: Int = 25)(implicit m: Manifest[T], ec: ExecutionContext): Future[Seq[Activity[T]]] = {
    val params = limitOffsetParams(from, limit)
    type ResResponse = ResultsResponse[Seq[Activity[T]]]
    makeHttpRequest[No, ResResponse](new URI(""), Get, None, params).map(_.results)
  }

  /**
   * Follow provided feed
   */
  def followFeed(feedToFollow: Feed)(implicit ec: ExecutionContext): Future[Boolean] = {
    val targetSlug = feedToFollow.feedSlug
    val targetId = feedToFollow.feedId
    val targetFeedId = s"$targetSlug:$targetId"
    val targetToken = bindings.signer.signature(feedToFollow)

    val data = Map("target" -> targetFeedId, "target_token" -> targetToken)

    makeHttpRequest[MRequest, MResponse](new URI("follows"), Post, data) map {
      _.contains("duration")
    }
  }

  /**
   * Unfollow provided feed
   */
  def unfollowFeed(feedToUnfollow: Feed)(implicit ec: ExecutionContext): Future[Boolean] = {
    val targetSlug = feedToUnfollow.feedSlug
    val targetId = feedToUnfollow.feedId
    val targetFeed = s"$targetSlug:$targetId"

    makeHttpRequest[No, MResponse](new URI(s"follows/$targetFeed"), Delete, None) map {
      _.contains("duration")
    }
  }

  /**
   * Get current feed followers
   */
  def followers(offset: Option[String], limit: Int = 25): Future[MResponse] = {
    val params = limitOffsetParams(offset, limit)
    makeHttpRequest[No, MResponse](new URI("followers"), Get, None, params)
  }

  /**
   * Get list of following feeds
   */
  def following(offset: Option[String], limit: Int = 25): Future[MResponse] = {
    val params = limitOffsetParams(offset, limit)
    makeHttpRequest[No, MResponse](new URI("followers"), Get, None, params)
  }

  /**
   * Drop current feed
   */
  def deleteFeed(): Future[MResponse] = {
    makeHttpRequest[No, MResponse](new URI(""), Delete, None)
  }

  /**
   * Build default param sequence for offset and limit
   */
  protected def limitOffsetParams(offset: Option[String], limit: Int): Seq[BasicNameValuePair] = {
    val limitParam = Some(new BasicNameValuePair("limit", limit.toString))
    val offsetParam = offset map (o => new BasicNameValuePair("offset", o))
    val params = limitParam :: offsetParam :: Nil
    params.flatten
  }

}