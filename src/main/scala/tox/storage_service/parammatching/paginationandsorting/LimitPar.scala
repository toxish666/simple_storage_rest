package tox.storage_service.parammatching.paginationandsorting

import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher

case class LimitPar(limit: Int) extends AnyVal

object LimitPar {
  implicit val yearQueryParamDecoder: QueryParamDecoder[LimitPar] =
    QueryParamDecoder[Int].map(LimitPar(_))

  object LimitParMatcher extends QueryParamDecoderMatcher[LimitPar]("limit")
}

