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

import com.insight.donation_analytics.Transaction._

sealed trait  Transaction {
  override def toString: String = this match {
    case Invalid => "Invalid"
    case Valid(r, d, zc, dt, amt) => s"$r|$d|${f"$zc%05d"}|$dt|$amt"
  }
}

object Transaction {

  final case class Valid(recipient: String,
                         donor: String,
                         zipCode: Int,
                         year: Short,
                         amount: Long) extends Transaction

  final case object Invalid extends Transaction

  def apply(line: String): Transaction = {
    val s = line.split("\\|")
    if (s.length != 21) {
      Invalid
    } else {
      val idR = """(\w+)""".r
      val nameR = """([A-Za-z]+,[\sa-zA-Z\.]+)""".r
      val zipCodeR = """(\d{5})\d*""".r
      val dateR = """\d{4}(\d{4})""".r
      val amtR = """(\d{1,12}\.?\d{0,2})""".r

      /** CMTE_ID, NAME, ZIP_CODE, TRANSACTION_DT, TRANSACTION_AMT, OTHER_ID */
      (s(0), s(7), s(10), s(13), s(14), s(15)) match {
        case (idR(id), nameR(name), zipCodeR(zipCode), dateR(date), amtR(amt), "") =>
          Valid(id, name, zipCode.toInt, date.toShort, math.round(amt.toDouble))
        case _ =>
          Invalid
      }
    }
  }

}
