package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models._
import org.json4s
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.ext._
import org.json4s.jackson.Serialization.write

/**
 * Serializer for requests/responses to GetStream
 */
trait GetStreamSerializer {

  def serialize[A <: AnyRef](a: A)(implicit m: Manifest[A]): String

  def deserialize[A <: AnyRef](s: String)(implicit m: Manifest[A]): A

}

/**
 * Default serializer uses json4s
 */
object GetStreamDefaultSerializer extends GetStreamSerializer {

  private implicit val formats = DefaultFormats ++ JodaTimeSerializers.all + FeedSerializer

  override def serialize[A <: AnyRef](a: A)(implicit m: Manifest[A]): String = {
    write(a)
  }

  override def deserialize[A <: AnyRef](s: String)(implicit m: Manifest[A]): A = {
    val json = parse(s)
    json.extract[A]
  }

  /**
   * Feed depends on its own serialization since getstream operates with slug:id string format
   */
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

}
