package tox.storage_service

import tox.storage_service.element.Element
import tox.storage_service.errorhandling.ElementErrorChannel
import tox.storage_service.parammatching.paginationandsorting.SingleSortingPar

abstract class ElementAlgebra[F[_]: ElementErrorChannel[*[_], E], +E <: Throwable] {
  def find(elid: Int): F[Option[Element]]
  def save(element: Element): F[Unit]
  def delete(elid: Int): F[Unit]
  def retrieve(sortingPar: Seq[SingleSortingPar], limit: Int, offset: Int): F[Seq[Element]]
}
