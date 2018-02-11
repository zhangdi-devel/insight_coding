package com.insight.donation_analytics

import java.io.{File, FileWriter}
import java.nio.file._

import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source

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

    val transactionStream = Source.fromFile(transactionFile).getLines()

    val donorRepository = DonorRepository()

    val bank = Bank()

    val outputStream = process(percent, transactionStream, donorRepository, bank)

    write(outputFile, outputStream)

  }


  def process(percent: Double,
              input: Iterator[String],
              donorRepository: DonorRepository,
              bank: Bank): Iterator[String] = {

    var i = 0

    input.flatMap{line =>
      i += 1
      if (i%400000 == 0) {
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
