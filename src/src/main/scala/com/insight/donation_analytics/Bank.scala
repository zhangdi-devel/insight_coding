package com.insight.donation_analytics

import scala.collection.mutable
import com.insight.donation_analytics.Bank.{Account, emptyAccount}
import org.slf4j.{Logger, LoggerFactory}

trait Bank {
  def update(transaction: Transaction): Unit
  def lookup(recipient: String,
             zipCode: Int,
             year: Short,
             percent: Double): String
}

final case class InMemoryBank(data: mutable.OpenHashMap[String, Account]) extends Bank {

  def apply(key: String): Account = {
    data.getOrElse(key, emptyAccount)
  }

  def update(transaction: Transaction): Unit = {
    transaction match {
      case Transaction.Invalid => Unit
      case Transaction.Valid(r, _, z, y, a) =>
        val key = s"$r|$z|$y"
        val acc = apply(key)
        data(key) = Account(acc.total + a, RedBlackTree.insert(acc.records, a))
    }
  }

  def lookup(recipient: String,
             zipCode: Int,
             year: Short,
             percent: Double): String = {
    val key = s"$recipient|$zipCode|$year"
    val acc = apply(key)
    val k = (percent * acc.records.count).ceil.toInt - 1
    val quantile = RedBlackTree.lookup(acc.records, k)
    f"$recipient%s|$zipCode%05d|$year%04d|$quantile%d|${acc.total}%d|${acc.records.count}%d"
  }

}

object Bank {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  final case class Account(total: Long, records: RedBlackTree)

  val emptyAccount: Account = Account(0L, Leaf)

  def apply(): Bank = InMemoryBank(mutable.OpenHashMap[String, Account]())

}