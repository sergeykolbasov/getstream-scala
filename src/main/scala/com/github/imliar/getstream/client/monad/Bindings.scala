package com.github.imliar.getstream.client.monad

import com.github.imliar.getstream.client.{GetStreamSign, GetStreamSerializer}
import com.twitter.util.Duration
import com.typesafe.config.Config

trait Bindings {

  val httpClient: HttpClient
  val config: Config
  val serializer: GetStreamSerializer
  val httpTimeout: Duration
  val signer: GetStreamSign

}
