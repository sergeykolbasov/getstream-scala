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

  "FeedOps" should "get empty activities from new feed" in {
    val feed = randomFeed("user")

    val activities = client(feed).getActivities()

    activities.futureValue shouldBe Seq.empty
  }
}