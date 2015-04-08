package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models._
import org.joda.time.DateTime
import org.json4s
import org.json4s.JsonAST.JValue
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.ext._
import org.json4s.jackson.Serialization.write
import org.json4s.JsonDSL._

import scala.util.{Failure, Success, Try}

trait GetStreamSerializer {

  def serialize[A <: AnyRef](a: A)(implicit m: Manifest[A]): String

  def deserialize[A <: AnyRef](s: String)(implicit m: Manifest[A]): A

}

object GetStreamDefaultSerializer extends GetStreamSerializer {

  private implicit val formats = DefaultFormats ++ JodaTimeSerializers.all + FeedSerializer

  override def serialize[A <: AnyRef](a: A)(implicit m: Manifest[A]): String = {
    write(a)
  }

  override def deserialize[A <: AnyRef](s: String)(implicit m: Manifest[A]): A = {
    val json = parse(s)
    json.extract[A]
  }

  object FeedSerializer extends Serializer[Feed] {
    import DefaultReaders._

    private val FeedClass = classOf[Feed]

    override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, json4s.JValue), Feed] = {
      case (TypeInfo(FeedClass, _), json) => {
        json match {
          case JString(str) => Feed(str)
          case JArray(values) => Feed(values.head.as[String])
        }
      }
    }

    override def serialize(implicit format: Formats): PartialFunction[Any, json4s.JValue] = {
      case tokenized: Feed with Tokenized => JString(s"${tokenized.feedSlug}:${tokenized.feedId} ${tokenized.token}")
      case feed: Feed => JString(s"${feed.feedSlug}:${feed.feedId}")
    }
  }

  /*
  private def serializeFeeds(feeds: Seq[Feed]): Seq[String] = {
    feeds map {
      case ft: Feed with Tokenized => s"${ft.feedSlug}:${ft.feedId} ${ft.token}"
      case Feed(feedId, feedSlug) => s"$feedSlug:$feedId"
    }
  }

  class ActivitySerializer[T](man: Manifest[T]) extends Serializer[Activity[T]] {

    import DefaultReaders._
    private val ActivityClass = classOf[Activity[T]]

    override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, json4s.JValue), Activity[T]] = {
      case (TypeInfo(ActivityClass, _), json) =>
        Try(
          json match {
            case jObject: JObject => {
              val id = (json \ "id").as[String]
              val actor = (json \ "actor").as[String]
              val verb = (json \ "verb").as[String]
              val to = (json \ "to").as[Seq[String]]
              val `object` = (json \ "object").extract[T](format, man)
              val target = json \ "target" match {
                case JNull | JNothing => None
                case JString(str) => Some(str)
              }
              val time = json \ "time" match {
                case JNull | JNothing => None
                case JString(t) => Some(DateTime.parse(t))
              }
              val foreignId = json \ "foreign_id" match {
                case JNull | JNothing => None
                case JString(str) => Some(str)
              }
              val toSeq = to map Feed.apply
              Activity(Some(id), actor, verb, `object`, target, time, toSeq, foreignId)
            }
          }
        ) match {
          case Success(r) => {
            r
          }
          case Failure(e) => {
            throw e
          }
        }
    }

    override def serialize(implicit format: Formats): PartialFunction[Any, json4s.JValue] = {
      case act: Activity[T] => {
        val feeds = serializeFeeds(act.to)
        ("id" -> act.id) ~ ("actor" -> act.actor) ~ ("verb" -> act.verb) ~ ("object" -> Extraction.decompose(act.`object`)) ~
          ("target" -> act.target) ~ ("time" -> Extraction.decompose(act.time)) ~ ("to" -> feeds) ~ ("foreign_id" -> act.foreignId)
      }
    }
  }*/
}
