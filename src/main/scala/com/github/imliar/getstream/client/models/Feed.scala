package com.github.imliar.getstream.client.models

/**
 * Basic feed
 */
case class Feed(feedId: String, feedSlug: String)

/**
 * Mixin applied to `Feed` only when its signed with API secret
 */
trait Tokenized {
  val token: String
}