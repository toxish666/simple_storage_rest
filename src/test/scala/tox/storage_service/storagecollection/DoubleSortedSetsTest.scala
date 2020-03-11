package tox.storage_service.storagecollection

import org.scalatest.{FunSuite, Suites}

import scala.math.Ordering.comparatorToOrdering
import cats.implicits.{catsKernelOrderingForOrder => _, _}
import tox.storage_service.element.Element
import tox.storage_service.parammatching.paginationandsorting.{BEmpty, BName, BSize, OAsc, ODesc, OEmpty, SingleSortingPar}
import tox.storage_service.storagecollection.DoubleSortedSetsTest.SortOrderingOnElementsTest

import scala.util.Try

class DoubleSortedSetsTest extends Suites(
  new SortOrderingOnElementsTest
)

object DoubleSortedSetsTest {

  class SortOrderingOnElementsTest extends FunSuite {

    val unsortedSS: DoubledSortedSets[Element] = DoubledSortedSets.emptySortedSets(
      Ordering.by[Element, (String,Int,Int)](el => (el.name,el.size,el.id)),
      Ordering.by[Element, (Int,String,Int)](el => (el.size,el.name,el.id)))
      .updated(Element.apply(1, "AB", 1))
      .updated(Element.apply(2, "AC", 2))
      .updated(Element.apply(3, "BB", 2))
      .updated(Element.apply(4, "BD", 3))

    def isSorted[T](s: Seq[T])(implicit ord: Ordering[T]): Boolean = s match {
      case Seq() => true
      case Seq(_) => true
      case _ => s.sliding(2).forall { case Seq(x, y) => ord.lteq(x, y) }
    }

    test("Check if the ordering is correct in accordance with Soring Pars") {
      val limit = 5
      val offset = 0

      val sortedPairs1 = Seq(SingleSortingPar(BName, OAsc))
      val sortedSS1 = unsortedSS.retrieveSortedBy[Try, Throwable](sortedPairs1, limit, offset).get
      assert(isSorted(sortedSS1)(Ordering.by[Element, String](el => el.name)))

      val sortedPairs2 = Seq(SingleSortingPar(BName, ODesc))
      val sortedSS2 = unsortedSS.retrieveSortedBy[Try, Throwable](sortedPairs2, limit, offset).get
      assert(isSorted(sortedSS2)(Ordering.by[Element, String](el => el.name).reverse))

      val sortedPairs3 = Seq(SingleSortingPar(BSize, OAsc))
      val sortedSS3 = unsortedSS.retrieveSortedBy[Try, Throwable](sortedPairs3, limit, offset).get
      assert(isSorted(sortedSS3)(Ordering.by[Element, Int](el => el.size)))

      val sortedPairs4 = Seq(SingleSortingPar(BName, ODesc), SingleSortingPar(BSize, OAsc))
      val sortedSS4 = unsortedSS.retrieveSortedBy[Try, Throwable](sortedPairs4, limit, offset).get
      assert(isSorted(sortedSS4)(comparatorToOrdering(
        Ordering.by[Element, String](el => el.name)
          .reverse
          .thenComparing(Ordering.by[Element, Int](el => el.size)))
        )
      )

      val sortedPairs5 = Seq(SingleSortingPar(BSize, ODesc), SingleSortingPar(BSize, ODesc), SingleSortingPar(BSize, OAsc))
      val sortedSS5 = unsortedSS.retrieveSortedBy[Try, Throwable](sortedPairs5, limit, offset).get
      assert(isSorted(sortedSS5)(Ordering.by[Element, Int](el => el.size).reverse))
    }

    test("Check if the limit and offset parameters working properly") {
      val sortedPairs = Seq(SingleSortingPar(BEmpty, OEmpty))

      val limit1 = 1
      val offset1 = 0
      val sortedSS1 = unsortedSS.retrieveSortedBy[Try, Throwable](sortedPairs, limit1, offset1).get
      assert(sortedSS1.size == 1)

      val limit2 = 100
      val offset2 = 2
      val sortedSS2 = unsortedSS.retrieveSortedBy[Try, Throwable](sortedPairs, limit2, offset2).get
      assert(sortedSS2.size == (unsortedSS.sortedSetByFst.size - offset2))
    }
  }

}