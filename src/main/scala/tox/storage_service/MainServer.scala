package tox.storage_service

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s.{HttpApp, HttpRoutes, Response}
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext

object MainServer extends IOApp.WithContext {
  implicit val ec = ExecutionContext.global

  override protected def executionContextResource: Resource[SyncIO, ExecutionContext] =
    Resource.liftF(SyncIO(ec))

  override def run(args: List[String]): IO[ExitCode] = {
    import tox.storage_service.errorhandling.ElementErrors._
      ElementActions.create[IO]
        .map(elAlg => new ElementServices[IO](elAlg).routes)
        .map(routes => routes.orNotFound)
        .flatMap((httpapp: HttpApp[IO]) =>
          BlazeServerBuilder[IO]
            .bindHttp(8080, "0.0.0.0")
            .withHttpApp(httpapp)
            .serve
            .compile
            .drain
            .as(ExitCode.Success)
        )
  }
}
