package tox.storage_service

import cats.effect.IO
import org.scalatest._
import io.circe._
import io.circe.literal._
import org.http4s._
import org.http4s.circe._
import tox.storage_service.ElementServicesTest.{DeleteServiceRouteTest, GetByIdServiceRouteTest, GetByPaginationSortingServiceRouteTest, PostServiceRouteTest}
import tox.storage_service.errorhandling.ElementErrors

import scala.util.Random

class ElementServicesTest extends Suites(
  new PostServiceRouteTest,
  new DeleteServiceRouteTest,
  new GetByIdServiceRouteTest,
  new GetByPaginationSortingServiceRouteTest
)

object ElementServicesTest {

  class PostServiceRouteTest extends FunSuite {
    val testjson: Json =
      json"""{
                            "elid": 1234,
                            "elname": "elname",
                            "elsize": 5555
                          }
                      """
    val req: Request[IO]#Self = Request[IO](Method.POST, Uri.unsafeFromString("/catalogue"))
      .withEntity(testjson)

    test("POST request with unique id should returns with 'created' response and same id") {
      import tox.storage_service.errorhandling.ElementErrors._
      val ioResp = for {
        elAlg <- ElementActions.create[IO]
        optResponse <- new ElementServices[IO](elAlg)
          .routes
          .run(req)
          .value
      } yield optResponse.getOrElse(Response.notFound)
      assert(ioResp.unsafeRunSync().status.code == 201)
    }

    test("POST request with same id should returns with 'conflict' response") {
      import tox.storage_service.errorhandling.ElementErrors._
      val ioResp = for {
        elAlg <- ElementActions.create[IO]
        _ <- new ElementServices[IO](elAlg)
          .routes
          .run(req)
          .value
        optResponseR <- new ElementServices[IO](elAlg)
          .routes
          .run(req)
          .value
      } yield optResponseR.getOrElse(Response.notFound)
      assert(ioResp.unsafeRunSync().status.code == 409)
    }

    test("POST request with incorrect size should returns with 'precondition failed' response") {
      import tox.storage_service.errorhandling.ElementErrors._
      val incorrectSizeJson: Json =
        json"""{
                            "elid": 1234,
                            "elname": "elname",
                            "elsize": -5555
                          }
                      """
      val reqInc = Request[IO](Method.POST, Uri.unsafeFromString("/catalogue"))
        .withEntity(incorrectSizeJson)
      val ioResp = for {
        elAlg <- ElementActions.create[IO]
        optResponse <- new ElementServices[IO](elAlg)
          .routes
          .run(reqInc)
          .value
      } yield optResponse.getOrElse(Response.notFound)
      assert(ioResp.unsafeRunSync().status.code == 412)
    }
  }

  class DeleteServiceRouteTest extends FunSuite {
    val elid = 5555
    val testjson: Json =
      json"""{
                            "elid": $elid,
                            "elname": "elname",
                            "elsize": 5555
                          }
                      """
    val reqP: Request[IO] = Request[IO](Method.POST, Uri.unsafeFromString("/catalogue"))
      .withEntity(testjson)
    val reqD: Request[IO] = Request[IO](Method.DELETE, Uri.unsafeFromString("/catalogue/" + elid))

    test("DELETE request with correct id to delete should be ok") {
      import tox.storage_service.errorhandling.ElementErrors._
      val ioResp = for {
        elAlg <- ElementActions.create[IO]
        _ <- new ElementServices[IO](elAlg)
          .routes
          .run(reqP)
          .value
        optResponse <- new ElementServices[IO](elAlg)
          .routes
          .run(reqD)
          .value
      } yield optResponse.getOrElse(Response.notFound)
      assert(ioResp.unsafeRunSync.status.code == 200)
    }

    test("DELETE request with correct id to delete should return bad request") {
      import tox.storage_service.errorhandling.ElementErrors._
      val reqIncor = Request[IO](Method.DELETE, Uri.unsafeFromString("/catalogue/" + 22))
      val ioResp = for {
        elAlg <- ElementActions.create[IO]
        _ <- new ElementServices[IO](elAlg)
          .routes
          .run(reqP)
          .value
        optResponse <- new ElementServices[IO](elAlg)
          .routes
          .run(reqIncor)
          .value
      } yield optResponse.getOrElse(Response.notFound)
      assert(ioResp.unsafeRunSync.status.code == 400)
    }
  }

  class GetByIdServiceRouteTest extends FunSuite {
    val elid = 400
    test("GET id request with correct id should return element record in json") {
      import tox.storage_service.errorhandling.ElementErrors._
      val testjson: Json =
        json"""{
                            "elid": $elid,
                            "elname": "elname",
                            "elsize": 5555
                          }
                      """
      val reqP = Request[IO](Method.POST, Uri.unsafeFromString("/catalogue"))
        .withEntity(testjson)
      val reqG = Request[IO](Method.GET, Uri.unsafeFromString("/catalogue/" + elid))
      val ioResp = for {
        elAlg <- ElementActions.create[IO]
        _ <- new ElementServices[IO](elAlg)
          .routes
          .run(reqP)
          .value
        optResponse <- new ElementServices[IO](elAlg)
          .routes
          .run(reqG)
          .value
      } yield optResponse.getOrElse(Response.notFound)
      val byteV = ioResp.unsafeRunSync.body.compile.toVector.unsafeRunSync
      val acceptedJsonStr: String = byteV.map(_.toChar).mkString
      val expectedJsonStr: String = testjson.toString().filterNot(_.isWhitespace)
      assert(acceptedJsonStr == expectedJsonStr)
    }

    test("GET id request with incorrect id should return badrequest") {
      import tox.storage_service.errorhandling.ElementErrors._
      val reqG = Request[IO](Method.GET, Uri.unsafeFromString("/catalogue/" + 55))
      val ioResp = for {
        elAlg <- ElementActions.create[IO]
        optResponse <- new ElementServices[IO](elAlg)
          .routes
          .run(reqG)
          .value
      } yield optResponse.getOrElse(Response.notFound)
      assert(ioResp.unsafeRunSync.status.code == 400)
    }
  }

  class GetByPaginationSortingServiceRouteTest extends FunSuite{
    //"/catalogue?limit=10&sort=size,asc"
    import tox.storage_service.errorhandling.ElementErrors._
    val elid = 5000
    def singleIn(i: Int, elAlg: ElementAlgebra[IO, ElementErrors]): IO[Response[IO]] = {
      val eln = elid + i
      val elname = Random.alphanumeric(i) + "elname"
      val testjson: Json =
        json"""{
                            "elid": $eln,
                            "elname": $elname,
                            "elsize": $eln
                          }
                      """
      val reqP = Request[IO](Method.POST, Uri.unsafeFromString("/catalogue"))
        .withEntity(testjson)
      for {
        optResponse <- new ElementServices[IO](elAlg)
          .routes
          .run(reqP)
          .value
      } yield optResponse.getOrElse(Response.notFound)
    }

    def monadicLoop(t: Int, acc: IO[Response[IO]], elAlg: ElementAlgebra[IO, ElementErrors]): IO[Response[IO]]= {
      IO.suspend{
        if (t <= 1) acc
        else monadicLoop(t-1, acc.flatMap(_ => singleIn(t, elAlg)), elAlg)
      }
    }

    def testBody(url: String): Response[IO] = {
      val reqG = Request[IO](Method.GET, Uri.unsafeFromString(url))
      val elAlgI = ElementActions.create[IO]
      val ioResp = for {
        elAlg <- elAlgI
        _ <- monadicLoop(5, singleIn(0,elAlg), elAlg)
        optResponse <- new ElementServices[IO](elAlg)
          .routes
          .run(reqG)
          .value
      } yield optResponse.getOrElse(Response.notFound)
      ioResp.unsafeRunSync()
    }

    test ("GET is OK when correct query is given and a request body is not empty") {
      val ioRespUnpacked = testBody("/catalogue?limit=3&offset=1&sort=name,desc&sort=size,asc")
      assert (ioRespUnpacked.status.code == 200)
      assert (ioRespUnpacked.body.compile.toVector.unsafeRunSync().nonEmpty)
    }

    test ("GET is not found when incorrect query is given") {
      val ioRespUnpacked = testBody("/catalogue?limit=0&offset=1&sort=name,desc&sort=size,asc")
      assert (ioRespUnpacked.status.code == 400)
    }
  }
}
