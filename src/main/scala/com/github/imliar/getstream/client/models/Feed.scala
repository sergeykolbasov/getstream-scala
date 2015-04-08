package com.github.imliar.getstream.client.models

/**
 * Basic feed
 */
case class Feed(feedSlug: String, feedId: String)

/**
 * Mixin applied to `Feed` only when its signed with API secret
 */
trait Tokenized {
  val token: String
}

object Feed {
  def apply(desc: String): Feed = {
    val split = desc.split(":")
    Feed(split.head, split.tail.mkString(":"))
  }
}