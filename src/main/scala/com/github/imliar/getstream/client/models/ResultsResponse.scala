package com.github.imliar.getstream.client.models

/**
 * Helper classes for painless response deserialization
 */
case class ResultsResponse[T](results: T)
case class MultipleActivities[T](activities: Seq[Activity[T]])
