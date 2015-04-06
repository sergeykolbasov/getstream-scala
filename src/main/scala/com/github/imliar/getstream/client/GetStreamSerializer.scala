package com.github.imliar.getstream.client

import com.github.imliar.getstream.client.models.{Tokenized, Feed, Activity}
import org.json4s.JsonAST.JValue
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.ext._
import org.json4s.jackson.Serialization.write
import org.json4s.JsonDSL._

trait GetStreamSerializer {

  def serialize[A <: AnyRef](a: A)(implicit m: Manifest[A]): String

  def deserialize[A <: AnyRef](s: String)(implicit m: Manifest[A]): A

}

object GetStreamDefaultSerializer extends GetStreamSerializer {

    implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

    override def serialize[A <: AnyRef](a: A)(implicit m: Manifest[A]): String = {
      a match {
        //we have to customize activity serialization
        //since Activity.to field is a sequence of `Feed` rather than strings
        case act: Activity[_] if act.to.nonEmpty => {
          val withoutTo = act.copy(to = Seq.empty)
          val jValue = Extraction.decompose(withoutTo)
          val feeds = serializeFeeds(act.to)
          val transformed = jValue transformField {
            case ("to", JArray(_)) => "to" -> feeds
          }
          compact(transformed)
        }
        case _ => write(a)
      }
    }

    override def deserialize[A <: AnyRef](s: String)(implicit m: Manifest[A]): A = {
      val json = parse(s)
      if(m <:< manifest[Activity[_]]) {
        //custom deserialization of `to` field
        json.transformField{
          case ("to", JArray(values)) if values.nonEmpty => "to" -> values.map {
            case JString(feed) => Extraction decompose Feed(feed)
          }
        }.extract[A]
      } else {
        parse(s).extract[A]
      }
    }

    private def serializeFeeds(feeds: Seq[Feed]): Seq[String] = {
      feeds map {
        case ft: Feed with Tokenized => s"${ft.feedSlug}:${ft.feedId} ${ft.token}"
        case Feed(feedId, feedSlug) => s"$feedSlug:$feedId"
      }
    }
}
