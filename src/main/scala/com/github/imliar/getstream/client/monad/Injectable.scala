package com.github.imliar.getstream.client.monad

trait Injectable {
  protected implicit val bindings: Bindings
}
