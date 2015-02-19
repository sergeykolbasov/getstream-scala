package com.github.imliar.getstream.client.models

import org.joda.time.DateTime

case class GetStreamActivity(
  id: Option[String] = None,
  actor: String,
  verb: String,
  `object`: String,
  target: Option[String] = None,
  time: Option[DateTime] = None,
  to: Seq[String] = Seq.empty,
  foreignId: Option[String] = None
)