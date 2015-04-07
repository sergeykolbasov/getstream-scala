package com.github.imliar.getstream.client.models

import org.joda.time.DateTime

/**
 * Basic activity
 */
case class Activity[T](
  id: Option[String] = None,
  actor: String,
  verb: String,
  `object`: T,
  target: Option[String] = None,
  time: Option[DateTime] = None,
  to: Seq[Feed] = Seq.empty,
  foreignId: Option[String] = None
)