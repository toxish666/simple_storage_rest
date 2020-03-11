package tox.storage_service.errorhandling

import cats.ApplicativeError
import cats.data.{ OptionT, ReaderT }
import cats.implicits._
import org.http4s._

trait HttpErrorHandler[F[_], E <: Throwable] {
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}

object RoutesHttpErrorHandler {
  def apply[F[_]: ApplicativeError[*[_], E], E <: Throwable](routes: HttpRoutes[F])
                                                            (handler: E => F[Response[F]]): HttpRoutes[F] = {
    ReaderT { req: Request[F] =>
      OptionT {
        routes
          .run(req)
          .value
          .handleErrorWith { e: E => handler(e) map (Option(_)) }
      }
    }
  }
}

object HttpErrorHandler {
  def apply[F[_], E <: Throwable](implicit ev: HttpErrorHandler[F, E]): HttpErrorHandler[F, E] = ev

  def mkInstance[F[_]: ApplicativeError[*[_], E], E <: Throwable](handler: E => F[Response[F]]) : HttpErrorHandler[F, E] =
    (routes: HttpRoutes[F]) => RoutesHttpErrorHandler(routes)(handler)
}
