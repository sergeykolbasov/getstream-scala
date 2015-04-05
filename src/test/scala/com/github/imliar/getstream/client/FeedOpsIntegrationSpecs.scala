package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models.Feed
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
}