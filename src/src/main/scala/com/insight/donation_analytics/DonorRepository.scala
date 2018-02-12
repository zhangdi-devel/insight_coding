/*
 * Copyright 2018 Zhang Di
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.insight.donation_analytics

import scala.collection.mutable

trait DonorRepository {
  def update(transaction: Transaction): Boolean
  def size: Int
}

final case class InMemoryDonorRepository(data: mutable.HashMap[String, Short])
  extends DonorRepository {

  def size = data.size

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
    InMemoryDonorRepository(mutable.HashMap[String, Short]())
  }

}