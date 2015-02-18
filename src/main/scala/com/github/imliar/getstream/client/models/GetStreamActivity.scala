package com.github.imliar.getstream.client.models

import org.joda.time.DateTime

trait GetStreamActivity {
  val id: Option[String] = None
  val actor: String
  val verb: String
  val `object`: String
  val target: Option[String] = None
  val time: Option[DateTime] = None
  val to: Seq[String] = Seq.empty
  val foreignId: Option[String] = None

  def copyWithId(id: String): GetStreamActivity
}
