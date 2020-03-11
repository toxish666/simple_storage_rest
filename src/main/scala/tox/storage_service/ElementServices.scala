package tox.storage_service

import cats.effect.Sync
import cats.syntax.all._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.headers.Allow
import tox.storage_service.element.Element
import tox.storage_service.errorhandling.HttpErrorHandler
import tox.storage_service.parammatching.paginationandsorting.LimitPar.LimitParMatcher
import tox.storage_service.parammatching.paginationandsorting.OffsetPar
import tox.storage_service.parammatching.paginationandsorting.OffsetPar.OffsetParMatcher
import tox.storage_service.parammatching.paginationandsorting.SortingPar.SortingParMatcher

abstract class Routes[F[_], E <: Throwable](implicit H: HttpErrorHandler[F, E]) extends Http4sDsl[F] {
  protected def httproutes: HttpRoutes[F]
  def routes: HttpRoutes[F] = H.handle(httproutes)
}

abstract class ElementRoutes[F[_]: HttpErrorHandler[*[_], E], E <: Throwable](elementAlg: ElementAlgebra[F, E]) extends Routes[F, E]

class ElementServices[F[_]: Sync: HttpErrorHandler[*[_], Throwable]]
          (elementAlgebra: ElementAlgebra[F, Throwable])
                extends ElementRoutes(elementAlgebra) {
  override val httproutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "catalogue"
      :? LimitParMatcher(limit)
      +& OffsetParMatcher(offset)
      +& SortingParMatcher(sortingPar) => {
      if (sortingPar.isInvalid) BadRequest()
      else elementAlgebra.retrieve(sortingPar.toEither.getOrElse(Seq()),
          limit.limit,
          offset.getOrElse(OffsetPar(0)).offset)
        .flatMap(elseq => Ok(elseq.asJson))
    }

    case GET -> Root / "catalogue" / IntVar(elementId) =>
      elementAlgebra.find(elementId).flatMap {
        case Some(element) => Ok(element.asJson)
        case None => BadRequest(elementId.asJson)
      }

    case req @ POST -> Root / "catalogue" =>
      req.as[Element].flatMap { element =>
        elementAlgebra.save(element) *> Created(element.id.asJson)
      }

    case DELETE -> Root / "catalogue" / IntVar(elementId) =>
      elementAlgebra.delete(elementId.toInt) *> Ok()

    case _ -> Root =>
      MethodNotAllowed(Allow(GET, POST, DELETE))
  }
}

