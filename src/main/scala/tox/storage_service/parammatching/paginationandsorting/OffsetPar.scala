package tox.storage_service.parammatching.paginationandsorting

import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

case class OffsetPar(offset: Int) extends AnyVal

object OffsetPar {
  implicit val yearQueryParamDecoder: QueryParamDecoder[OffsetPar] =
    QueryParamDecoder[Int].map(OffsetPar(_))

  object OffsetParMatcher extends OptionalQueryParamDecoderMatcher[OffsetPar]("offset")
}

