package tox.storage_service.parammatching.paginationandsorting

trait ParameterParsingError extends Exception

case class NameOrSizeError(str: String) extends ParameterParsingError
case class AscOrDescError(str: String) extends ParameterParsingError


