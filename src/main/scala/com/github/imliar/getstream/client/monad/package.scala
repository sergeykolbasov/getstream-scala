package com.github.imliar.getstream.client

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.{Response, Request}

package object monad {
  type HttpClient = Service[Request, Response]
}
