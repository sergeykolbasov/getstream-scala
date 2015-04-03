package com.github.imliar.getstream.client

trait Injectable {
  protected implicit val bindings: Bindings
}
