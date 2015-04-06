package com.github.imliar.getstream.client.models

case class ResultsResponse[T](results: T)
case class MultipleActivities[T](activities: Seq[Activity[T]])
