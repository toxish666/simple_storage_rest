package tox.storage_service.parammatching.paginationandsorting

import org.http4s.QueryParameterValue
import org.scalatest.{FunSuite, Suites}
import tox.storage_service.parammatching.paginationandsorting.SortingParTest.{SortingParDecoderTest, SortingQueryParamDecoderTest}

class SortingParTest extends Suites(
  new SortingParDecoderTest,
  new SortingQueryParamDecoderTest
)

object SortingParTest {
  val sizeAscStr = "size,asc"
  val sizeDescStr = "size,desc"
  val nameAscStr = "name,asc"
  val incorrectStr = "ahaha"
  val halfCorrectStr1 = "size,nene"
  val halfCorrectStr2 = "asd,desc"
  class SortingParDecoderTest extends FunSuite{
    test("Right when parsing only NameOrSize and str is correct") {
      assert(SortingParDecoder.parseNameOrSize(sizeAscStr).isRight)
      assert(SortingParDecoder.parseNameOrSize(sizeDescStr).isRight)
      assert(SortingParDecoder.parseNameOrSize(nameAscStr).isRight)
    }
    test("Right when parsing only NameOrSize and first part of a str is correct") {
      assert(SortingParDecoder.parseNameOrSize(halfCorrectStr1).isRight)
    }
    test("Left when parsing only NameOrSize str is incorrect") {
      assert(SortingParDecoder.parseNameOrSize(incorrectStr).isLeft)
      assert(SortingParDecoder.parseNameOrSize(halfCorrectStr2).isLeft)
    }
    test("Right when parsing only AscOrDesc and str is correct") {
      assert(SortingParDecoder.parseAscOrDesc(sizeAscStr).isRight)
      assert(SortingParDecoder.parseAscOrDesc(sizeDescStr).isRight)
      assert(SortingParDecoder.parseAscOrDesc(nameAscStr).isRight)
    }
    test("Right when parsing only AscOrDesc and second part of a str is correct") {
      assert(SortingParDecoder.parseAscOrDesc(halfCorrectStr2).isRight)
    }
    test("Left when parsing only AscOrDesc str is incorrect") {
      assert(SortingParDecoder.parseAscOrDesc(incorrectStr).isLeft)
      assert(SortingParDecoder.parseAscOrDesc(halfCorrectStr1).isLeft)
    }
    test("Right when parsing NameOrSize and AscOrDesc and str is correct") {
      assert(SortingParDecoder.createDecode.decode(sizeAscStr).isRight)
      assert(SortingParDecoder.createDecode.decode(sizeDescStr).isRight)
      assert(SortingParDecoder.createDecode.decode(nameAscStr).isRight)
    }
    test("Left when parsing NameOrSize and AscOrDesc and str is incorrect") {
      assert(SortingParDecoder.createDecode.decode(incorrectStr).isLeft)
      assert(SortingParDecoder.createDecode.decode(halfCorrectStr1).isLeft)
      assert(SortingParDecoder.createDecode.decode(halfCorrectStr2).isLeft)
    }
  }
  class SortingQueryParamDecoderTest extends FunSuite{
    test("Valid decoding with valid query strings") {
      assert(SortingPar.sortingQueryParamDecoder.decode(QueryParameterValue(sizeAscStr)).isValid)
      assert(SortingPar.sortingQueryParamDecoder.decode(QueryParameterValue(sizeDescStr)).isValid)
      assert(SortingPar.sortingQueryParamDecoder.decode(QueryParameterValue(nameAscStr)).isValid)
    }
    test("Invalid decoding with invalid query strings") {
      assert(SortingPar.sortingQueryParamDecoder.decode(QueryParameterValue(incorrectStr)).isInvalid)
      assert(SortingPar.sortingQueryParamDecoder.decode(QueryParameterValue(halfCorrectStr1)).isInvalid)
      assert(SortingPar.sortingQueryParamDecoder.decode(QueryParameterValue(halfCorrectStr2)).isInvalid)
    }
  }
}