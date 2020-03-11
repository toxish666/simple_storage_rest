package tox.storage_service.storagecollection

import cats._
import cats.data._
import cats.implicits.{catsKernelOrderingForOrder=>_,_}
import tox.storage_service.element.Element
import tox.storage_service.errorhandling.InvalidSortedParams

import scala.collection.SortedSet
import tox.storage_service.parammatching.paginationandsorting.{BEmpty, BName, BSize, OAsc, ODesc, OEmpty, SingleSortingPar}

class DoubledSortedSets[A](val sortedSetByFst: SortedSet[A],
                           val sortedSetBySnd: SortedSet[A]) {self =>
  def updated(el: A): DoubledSortedSets[A] =
    DoubledSortedSets(sortedSetByFst | Set(el), sortedSetBySnd | Set(el))

  def removed(el: A): DoubledSortedSets[A] = {
    DoubledSortedSets(sortedSetByFst &~ Set(el), sortedSetBySnd &~ Set(el))
  }

  def retrieveSortedBy[F[_]: ApplicativeError[*[_], E], E >: Throwable](sortingPar: Seq[SingleSortingPar], limit: Int, offset: Int)
                                                                       (implicit ev: A =:= Element): F[Seq[A]] = {
    val env = implicitly[ApplicativeError[F,E]]
    type SeqAndOrd[A] = (Seq[A],Option[Ordering[A]])

    def sortSingle(sortingP: SingleSortingPar): F[SeqAndOrd[A]] = {
      sortingP match {
        case SingleSortingPar(BName, OAsc) => env.pure((sortedSetByFst.toSeq, sortedSetByFst.ordering.pure[Option]))
        case SingleSortingPar(BSize, OAsc) => env.pure((sortedSetBySnd.toSeq, sortedSetBySnd.ordering.pure[Option]))
        case SingleSortingPar(BName, ODesc) => env.pure((sortedSetByFst.toSeq, sortedSetByFst.ordering.reverse.pure[Option]))
        case SingleSortingPar(BSize, ODesc) => env.pure((sortedSetBySnd.toSeq, sortedSetBySnd.ordering.reverse.pure[Option]))
        case SingleSortingPar(BEmpty, OEmpty) => env.pure((sortedSetByFst.toSeq, None))
        case _ => env.raiseError[SeqAndOrd[A]](InvalidSortedParams())
      }
    }

    def sortAdditional(sset: Seq[A],
                       sortingPAdd: SingleSortingPar,
                       ordfstOpt: Option[Ordering[A]]
                      ): Seq[A] = {
      import scala.math.Ordering.comparatorToOrdering
      (sortingPAdd, ordfstOpt) match {
        case (_, None) => sset
        case (SingleSortingPar(BName, OAsc), Some(ordfst)) => sset
            .sorted(
              comparatorToOrdering(
                ordfst
                  .thenComparing(Ordering.by[A,String]((e:A) => e.name))
              ))
        case (SingleSortingPar(BSize, OAsc), Some(ordfst)) => sset
          .sorted(
            comparatorToOrdering(
              ordfst
                .thenComparing(Ordering.by[A,Int]((e:A) => e.id))
            ))
        case (SingleSortingPar(BName, ODesc), Some(ordfst)) => sset
          .sorted(
            comparatorToOrdering(
              ordfst
                .thenComparing(Ordering.by[A,String]((e:A) => e.name).reverse)
            ))
        case (SingleSortingPar(BSize, ODesc), Some(ordfst)) => sset
          .sorted(
            comparatorToOrdering(
              ordfst
                .thenComparing(Ordering.by[A,Int]((e:A) => e.id).reverse)
            ))
        case (_, Some(ordfst)) => sset.sorted(ordfst)
      }
    }
      val fstSortingPar: SingleSortingPar = sortingPar.applyOrElse(0, (_: Int) => SingleSortingPar(BName, OAsc))
      val sndSortingPar: SingleSortingPar = sortingPar.applyOrElse(1, (_: Int) => SingleSortingPar(BEmpty, OEmpty))
      sortSingle(fstSortingPar).map[Seq[A]]{ case (fseq: Seq[A], ford: Option[Ordering[A]]) =>
        sortAdditional(fseq, sndSortingPar, ford).slice(offset, offset + limit)
      }
  }
}

object DoubledSortedSets {
  def apply[A](sortedSetByFst: SortedSet[A], sortedSetBySnd: SortedSet[A]) =
    new DoubledSortedSets(sortedSetByFst, sortedSetBySnd)

  def emptySortedSets[A](orderingFst: Ordering[A],
                         orderingSnd: Ordering[A]): DoubledSortedSets[A] =
    new DoubledSortedSets(SortedSet.empty[A](orderingFst), SortedSet.empty[A](orderingSnd))
}
