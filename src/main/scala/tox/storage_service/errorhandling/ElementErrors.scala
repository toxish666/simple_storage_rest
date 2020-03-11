package tox.storage_service.errorhandling

import cats.effect.IO
import org.http4s.{Response, Status}


sealed trait ElementErrors extends Exception

case class ElementAlreadyExist(id: Int) extends ElementErrors
case class InvalidElementId(id: Int) extends ElementErrors
case class InvalidSize(size: Int) extends ElementErrors
case class ElementNotFound(id: Int) extends ElementErrors
case class InvalidLimitOrOffset(limit: Int, offset: Int) extends ElementErrors

case class InvalidSortedParams() extends ElementErrors

object ElementErrors {
  implicit val errHandlerForIo: HttpErrorHandler[IO, Throwable] = HttpErrorHandler.mkInstance[IO, Throwable] {
    case ElementAlreadyExist(_: Int) => IO.pure(Response.apply(
      status = Status.Conflict,
      //body = elid.asJson
    ))
    case InvalidElementId(_: Int) => IO.pure(Response.apply(status = Status.BadRequest))
    case InvalidSize(_: Int) => IO.pure(Response.apply(status = Status.PreconditionFailed))
    case ElementNotFound(_: Int) => IO.pure(Response.apply(status = Status.BadRequest))
    case InvalidLimitOrOffset(_: Int, _: Int) => IO.pure(Response.apply(status = Status.BadRequest))
    case _ => IO.pure(Response.apply(status = Status.NotFound))
  }
}