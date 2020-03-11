package tox.storage_service.parammatching.paginationandsorting

import cats.Alternative
import cats.data.StateT
import cats.implicits._
import org.http4s.dsl.impl.OptionalMultiQueryParamDecoderMatcher
import org.http4s.{ParseFailure, QueryParamDecoder}
import scala.collection.immutable


sealed trait SortBy

object BName extends SortBy
object BSize extends SortBy
object BEmpty extends SortBy

sealed trait SortOrder

object OAsc extends SortOrder
object ODesc extends SortOrder
object OEmpty extends SortOrder


case class SingleSortingPar(sortBy: SortBy, sortOrder: SortOrder)
case class SortingPar(sortingPar: immutable.Seq[SingleSortingPar])

object SortingPar {
  implicit val sortingQueryParamDecoder: QueryParamDecoder[SingleSortingPar] =
    QueryParamDecoder[String].emap(
      SortingParDecoder.createDecode
        .decode(_)
        .bimap[ParseFailure,SingleSortingPar]({_: Throwable => new ParseFailure("Failure parsing sorting query","")},
              {case (sb: SortBy,so: SortOrder) => SingleSortingPar(sb,so) })
    )
  object SortingParMatcher extends OptionalMultiQueryParamDecoderMatcher[SingleSortingPar](name="sort")
}

trait SortingParDecoder[A] {
  def decode(in: String): Either[Throwable, A]
}

object SortingParDecoder {

  implicit val decoderAlternative: Alternative[SortingParDecoder] = new Alternative[SortingParDecoder] {
    def pure[A](a: A): SortingParDecoder[A] = SortingParDecoder.from(Function.const(Right(a)))

    def empty[A]: SortingParDecoder[A] = SortingParDecoder.from(Function.const(Left(new Error("Empty"))))

    def combineK[A](l: SortingParDecoder[A], r: SortingParDecoder[A]): SortingParDecoder[A] =
      (in: String) => l.decode(in).orElse(r.decode(in))

    def ap[A, B](ff: SortingParDecoder[A => B])(fa: SortingParDecoder[A]): SortingParDecoder[B] =
      (in: String) => fa.decode(in) ap ff.decode(in)
  }

  type Parser[A] = StateT[Option, String, A]

  def from[A](f: String => Either[Throwable, A]): SortingParDecoder[A] =
    (in: String) => f(in)

  val createDecode: SortingParDecoder[(SortBy, SortOrder)] =
    SortingParDecoder.from[SortBy](x=>parseNameOrSize(x)) product SortingParDecoder.from[SortOrder](z=>parseAscOrDesc(z))

  /*
  query in format &sort=size,asc | size,desc | name,asc ...
  parse only size or name here
   */
  def parseNameOrSize(in: String): Either[Throwable, SortBy] = {
    val queryparser: Parser[SortBy] = for {
      fStr <- StateT.get[Option, String]
      stAr = fStr.split(',')
      _ <- if (stAr.length != 2)
        ().raiseError[Parser, String]
      else
        StateT.modify[Option, String] { _ => stAr(0)}
      nameOrSizeStr <- StateT.get[Option, String]
      _ <- if (!Seq("name", "size").contains(nameOrSizeStr))
        ().raiseError[Parser, String]
      else
        StateT.get[Option, String]
    } yield {
      nameOrSizeStr match {
          case "name" => BName
          case "size" => BSize
        }
    }
    (queryparser runA in).toRight[Throwable](NameOrSizeError(in))
  }

  /*
    query in format &sort=size,asc | size,desc | name,asc ...
    parse only asc or desc here
     */
  def parseAscOrDesc(in: String): Either[Throwable, SortOrder] = {
    val queryparser: Parser[SortOrder] = for {
      fStr <- StateT.get[Option, String]
      stAr = fStr.split(',')
      _ <- if (stAr.length != 2)
        ().raiseError[Parser, String]
      else
        StateT.modify[Option, String] { _ => stAr(1)}
      ascOrDescStr <- StateT.get[Option, String]
      _ <- if (!Seq("asc", "desc").contains(ascOrDescStr))
        ().raiseError[Parser, String]
      else
        StateT.get[Option, String]
    } yield {
      ascOrDescStr match {
        case "asc" => OAsc
        case "desc" => ODesc
      }
    }
    (queryparser runA in).toRight[Throwable](AscOrDescError(in))
  }
}
