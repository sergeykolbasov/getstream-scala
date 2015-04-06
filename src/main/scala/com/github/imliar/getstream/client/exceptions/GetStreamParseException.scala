package com.github.imliar.getstream.client.exceptions

import java.net.URI

case class GetStreamParseException(uri: URI, method: String, response: String, e: Throwable) extends Throwable
