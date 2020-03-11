package tox.storage_service.errorhandling

import cats._
import cats.data._
import cats.implicits._

trait ElementErrorChannel[F[_], E <: Throwable] {
  def raise[A](e: E): F[A]
}

object ElementErrorChannel {
  def apply[F[_], E <: Throwable](implicit ev: ElementErrorChannel[F, E]) = ev

  implicit def instance[F[_], E <: Throwable](implicit F: ApplicativeError[F, Throwable]): ElementErrorChannel[F, E] =
    new ElementErrorChannel[F, E] {
      override def raise[A](e: E) = F.raiseError(e)
    }

  object syntax {
    implicit class ErrorChannelOps[F[_]: ElementErrorChannel[*[_], E], E <: Throwable](e: E) {
      def raise[A]: F[A] = ElementErrorChannel[F, E].raise[A](e)
    }
  }
}