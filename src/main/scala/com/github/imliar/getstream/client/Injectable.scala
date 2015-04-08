package com.github.imliar.getstream.client

/**
 * Mixin to ensure bindings existence
 */
trait Injectable {
  protected implicit val bindings: Bindings
}
