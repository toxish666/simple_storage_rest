package tox.storage_service.element

import org.scalatest.{FunSuite, Suites}
import tox.storage_service.element.ElementTest.{ElementEncodingDecodingTest}
import io.circe.parser.decode
import io.circe.syntax._

class ElementTest extends Suites(
  new ElementEncodingDecodingTest
)

object ElementTest {
  class ElementEncodingDecodingTest extends FunSuite {
    val el = Element(1, "test", 10)

    test("Field names are correct") {
      import Element._
      val encodedEl = el.asJson
      assert(encodedEl.hcursor.get[Int]("elid").isRight)
      assert(encodedEl.hcursor.get[String]("elname").isRight)
      assert(encodedEl.hcursor.get[Int]("elsize").isRight)
    }

    test("Decoding of encoded entity is equal to that entity") {
      val el = Element(1, "test", 10)
      import Element._
      val encodedEl = el.asJson
      val decodedEl = decode[Element](encodedEl.spaces2).getOrElse(empty())
      assert(decodedEl == el)
    }
  }
}
