package com.insight.donation_analytics

import scala.collection.mutable

trait DonorRepository {
  def update(transaction: Transaction): Boolean
}

final case class InMemoryDonorRepository(data: mutable.OpenHashMap[String, Short])
  extends DonorRepository {
  def update(transaction: Transaction): Boolean = {
    /**
      * this method takes a transaction
      * return true if the donor has donated in a prior year
      *
      * if the donor is not in the repo or the year is not a prior year
      * update the year to the current record
      *
      * */
    transaction match {
      case Transaction.Invalid => false
      case Transaction.Valid(_, donor, zipCode, year, _) =>
        val key = s"$donor|$zipCode"
        data.get(key) match {
          case None =>
            data(key) = year
            false
          case Some(date) =>
            if (date < year) {
              true
            } else {
              data(key) = year
              false
            }
        }
    }
  }
}

object DonorRepository {

  def apply(): DonorRepository = {
    InMemoryDonorRepository(mutable.OpenHashMap[String, Short]())
  }

}