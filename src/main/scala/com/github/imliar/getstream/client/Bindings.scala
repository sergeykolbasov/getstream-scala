package com.github.imliar.getstream.client

import com.twitter.util.Duration
import com.typesafe.config.Config

trait Bindings {

  val httpClient: HttpClient
  val config: Config
  val serializer: GetStreamSerializer
  val signer: GetStreamSign

}
