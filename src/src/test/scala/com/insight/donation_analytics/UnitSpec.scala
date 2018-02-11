package com.insight.donation_analytics

import org.scalatest._
import org.slf4j.{Logger, LoggerFactory}

class UnitSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors {

  def logger: Logger = LoggerFactory.getLogger(getClass)

}
