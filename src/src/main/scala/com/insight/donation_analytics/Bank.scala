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
import com.insight.donation_analytics.Bank.{Account, emptyAccount}
import org.slf4j.{Logger, LoggerFactory}

trait Bank {
  def size: (Int, Int)
  def update(transaction: Transaction): Unit
  def lookup(recipient: String,
             zipCode: Int,
             year: Short,
             percent: Double): String
}

final case class InMemoryBank(data: mutable.HashMap[String, Account]) extends Bank {

  def apply(key: String): Account = {
    data.getOrElse(key, emptyAccount)
  }

  def size: (Int, Int) = {
    val donations = data.values.map(a => a.records.count).sum
    (data.size, donations)
  }

  def update(transaction: Transaction): Unit = {
    transaction match {
      case Transaction.Invalid => Unit
      case Transaction.Valid(r, _, z, y, a) =>
        val key = s"$r|$z|$y"
        val acc = apply(key)
        data(key) = Account(acc.total + a, OrderStatisticTree.insert(acc.records, a))
    }
  }

  def lookup(recipient: String,
             zipCode: Int,
             year: Short,
             percent: Double): String = {
    val key = s"$recipient|$zipCode|$year"
    val acc = apply(key)
    val k = (percent * acc.records.count).ceil.toInt - 1
    val quantile = OrderStatisticTree.lookup(acc.records, k)
    f"$recipient%s|$zipCode%05d|$year%04d|$quantile%d|${acc.total}%d|${acc.records.count}%d"
  }

}

object Bank {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  final case class Account(total: Long, records: OrderStatisticTree)

  val emptyAccount: Account = Account(0L, Leaf)

  def apply(): Bank = InMemoryBank(mutable.HashMap[String, Account]())

}