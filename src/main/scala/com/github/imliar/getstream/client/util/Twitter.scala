package com.github.imliar.getstream.client.util

import com.twitter.util.{Future => TwFuture, Promise => TwPromise, Try => TwTry, Throw, Return}
import scala.util.{Try => ScTry}
import scala.concurrent.{Future => ScFuture, promise => scPromise, ExecutionContext}

/**
 * Twitter <=> Scala future convert
 */
object Twitter {

  implicit class TwFutureToScala[T](val tf: TwFuture[T]) extends AnyVal {
    def asScala: ScFuture[T] = {
      val prom = scPromise[T]()

      tf.onSuccess { prom success _ }
      tf.onFailure { prom failure _ }

      prom.future
    }
  }

  implicit class ScFutureToTwitter[T](val sf: ScFuture[T]) extends AnyVal {
    def asTwitter(implicit ec: ExecutionContext): TwFuture[T] = {
      val prom = TwPromise[T]()

      // type inference issue
      sf onSuccess PartialFunction(prom.setValue)
      sf onFailure { case t => prom setException t }

      prom
    }
  }

  implicit class TwTryToScala[T](val tt: TwTry[T]) extends AnyVal {
    def asScala: ScTry[T] = tt match {
      case Throw(t) => new util.Failure(t)
      case Return(r) => new util.Success(r)
    }
  }

  implicit class ScTryToTwitter[T](val st: ScTry[T]) extends AnyVal {
    def asTwitter: TwTry[T] = st match {
      case util.Failure(t) => Throw(t)
      case util.Success(r) => Return(r)
    }
  }

}