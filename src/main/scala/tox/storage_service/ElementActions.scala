package tox.storage_service

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._
import tox.storage_service.element.Element
import tox.storage_service.errorhandling.{ElementAlreadyExist, ElementErrorChannel, ElementErrors, ElementNotFound, InvalidLimitOrOffset, InvalidSize}
import tox.storage_service.parammatching.paginationandsorting.SingleSortingPar
import tox.storage_service.storagecollection.DoubledSortedSets


object ElementActions {
  def create[F[_]: Sync](implicit error: ElementErrorChannel[F, ElementErrors]): F[ElementAlgebra[F, ElementErrors]] =
    Ref.of[F, DoubledSortedSets[Element]](DoubledSortedSets
      .emptySortedSets(
        Ordering.by[Element, (String,Int,Int)](el => (el.name,el.size,el.id)),
        Ordering.by[Element, (Int,String,Int)](el => (el.size,el.name,el.id))))
      .map { state =>
      new ElementAlgebra[F, ElementErrors] {

        private def validateSize(size: Int): F[Unit] =
          if (size <= 0) error.raise(InvalidSize(size)) else ().pure[F]

        private def validateLimitOffset(limit: Int, offset: Int): F[Unit] =
              if (limit <= 0 || offset < 0) error.raise(InvalidLimitOrOffset(limit, offset)) else ().pure[F]

        override def find(elid: Int): F[Option[Element]] =
          state.get.map(_.sortedSetByFst.find(el => el.id == elid))

        override def retrieve(sortingPar: Seq[SingleSortingPar], limit: Int, offset: Int): F[Seq[Element]] = {
          validateLimitOffset(limit, offset) *>
            state.get.flatMap((dss: DoubledSortedSets[Element]) =>
              dss.retrieveSortedBy(sortingPar, limit, offset)
            )
        }

        override def save(element: Element): F[Unit] =
          validateSize(element.size) *>
            find(element.id).flatMap {
              case Some(_) =>
                error.raise[Unit](ElementAlreadyExist(element.id))
              case None =>
                state.update(_.updated(element))
            }

        override def delete(elid: Int): F[Unit] =
          find(elid).flatMap {
            case Some(el) =>
              state.update(_.removed(el))
            case None =>
              error.raise[Unit](ElementNotFound(elid))
          }
      }
    }
}
