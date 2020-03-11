package tox.storage_service.element

import io.circe.{Decoder, Encoder, HCursor}
import io.circe.literal._


case class Element(id: Int, name: String, size: Int)

object Element {
  def apply(id: Int, name: String, size: Int) =
    new Element(id, name, size)

  def empty() =
    new Element(0, "", 0)

  implicit val elementCircleEncoding: Encoder[Element] = {
    Encoder.instance { el: Element =>
      json"""{
              "elid": ${el.id},
              "elname": ${el.name},
              "elsize": ${el.size}
             }
          """
    }
  }
  implicit val elementCircleDecoding: Decoder[Element] = {
    Decoder.instance { hcursor: HCursor => {
      for {
        elid <- hcursor.get[Int]("elid")
        elname <- hcursor.get[String]("elname")
        elsize <- hcursor.get[Int]("elsize")
        } yield Element(elid, elname, elsize)
      }
    }
  }
}