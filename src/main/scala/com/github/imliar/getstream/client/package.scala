package com.github.imliar.getstream

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.{Request, Response}

package object client {
  type HttpClient = Service[Request, Response]
}
