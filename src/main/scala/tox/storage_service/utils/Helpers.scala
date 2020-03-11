package tox.storage_service.utils

import cats._
import cats.data._
import cats.implicits._
import tox.storage_service.errorhandling.ElementErrors

object Helpers {
  implicit class IntWithTimes[A,B](tup: (A, B)) {
    def fuseTup2[C](fuser: (A,B) => C): C = fuser(tup._1, tup._2)
  }
}
