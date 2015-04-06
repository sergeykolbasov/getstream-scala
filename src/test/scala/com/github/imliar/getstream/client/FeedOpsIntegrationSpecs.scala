package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models.{Activity, Feed}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.{ScalaFutures, Futures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, FlatSpec}
import java.util.UUID.randomUUID

class FeedOpsIntegrationSpecs extends FlatSpec with Matchers with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))
  implicit val ec = scala.concurrent.ExecutionContext.global

  val client = GetStreamClient(ConfigFactory.load())

  def randomFeed(slug: String) = Feed(randomUUID.toString, slug)
  def randomUserFeed = randomFeed("user")

  def followFeed(follower: Feed, following: Feed): Boolean = {
    val opsFollower = client(follower)
    opsFollower.followFeed(following).futureValue shouldBe true
    Thread.sleep(2000) //(un)follow ops are async
    true
  }

  def unfollowFeed(follower: Feed, following: Feed): Boolean = {
    val opsFollower = client(follower)
    opsFollower.unfollowFeed(following).futureValue shouldBe true
    Thread.sleep(2000) //(un)follow ops are async
    true
  }

  def randomActivity: Activity[String] = {
    Activity(
      actor = "actor",
      verb = "verb",
      `object` = randomUUID.toString
    )
  }

  "FeedOps" should "get empty activities from new feed" in {
    val feed = randomUserFeed

    val activities = client(feed).getActivities()

    activities.futureValue shouldBe Seq.empty
  }

  it should "follow and unfollow user feed" in {
    val follower = randomUserFeed
    val following = randomUserFeed
    val opsFollower = client(follower)
    val opsFollowing = client(following)

    followFeed(follower, following)

    opsFollower.following().futureValue.head shouldBe following
    opsFollowing.followers().futureValue.head shouldBe follower

    unfollowFeed(follower, following)

    opsFollower.following().futureValue.size shouldBe 0
    opsFollowing.followers().futureValue.size shouldBe 0
  }

  it should "add single activity and get it back" in {
    val feed = randomUserFeed
    val activity = randomActivity

    val activityWithId = client(feed).addActivity(activity)

    activityWithId.map(_.id.nonEmpty).futureValue shouldBe true
    activityWithId.map(_.copy(id = None)).futureValue shouldBe activity
  }

  it should "add multiple activities and get em back" in {
    val feed = randomUserFeed
    val activities = for(i <- 1 to 10) yield randomActivity

    val activitiesWithId = client(feed).addActivities(activities)

    activitiesWithId.map{ _.map (_.copy(id = None)) }.futureValue shouldBe activities
  }

  it should "add activity to targeted feeds" in {
    val feed = randomUserFeed
    val mentionedFeed = randomUserFeed
    val activity = randomActivity.copy[String](to = Seq(mentionedFeed))

    val activityWithId = client(feed).addActivity(activity)
    client(mentionedFeed).getActivities[String]().map(_.headOption).futureValue shouldBe Some(activityWithId.futureValue)
  }
}