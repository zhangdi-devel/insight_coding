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

import java.io.{File, FileWriter}
import java.nio.file._
import java.nio.charset.CodingErrorAction

import org.slf4j.{Logger, LoggerFactory}

import scala.io.{Source, Codec}

object DonationAnalytics {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {

    if (args.length != 0) {
      logger.warn(s"this program ignores any command line arguments")
    }

    val percentFile = Paths.get("input/percentile.txt").toFile
    val transactionFile = Paths.get("input/itcont.txt").toFile
    val outputFile = Paths.get("output/repeat_donors.txt").toFile

    val percent: Double = Source.fromFile(percentFile).getLines().next().toDouble/100

    logger.info(s"process the input with ${f"$percent%.2f"}")

    /**
      * By default jvm will throw error when encountering other charset
      * change it to replace
      * */
    val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    val transactionStream = Source.fromFile(transactionFile)(codec).getLines()

    val donorRepository = DonorRepository()

    val bank = Bank()

    val outputStream = process(percent, transactionStream, donorRepository, bank)

    write(outputFile, outputStream)

    logger.info(s"total number of donors: ${donorRepository.size}")
    logger.info(s"total number of accounts and donations:  ${bank.size}")
  }


  def process(percent: Double,
              input: Iterator[String],
              donorRepository: DonorRepository,
              bank: Bank): Iterator[String] = {

    var i = 0

    input.flatMap{line =>
      i += 1
      if (i%500000 == 0) {
        logger.debug(s"$i records processed")
      }
      Transaction(line) match {
        case Transaction.Invalid => None
        case t: Transaction.Valid =>
          val isRepeat = donorRepository.update(t)
          if (isRepeat) {
            bank.update(t)
            Some(bank.lookup(t.recipient, t.zipCode, t.year, percent))
          } else {
            None
          }
      }
    }
  }

  def write(outFile: File, stream: Iterator[String]): Unit = {

    val writer = new FileWriter(outFile)

    stream.foreach(s => writer.write(s + '\n'))

    writer.close()
  }

}
