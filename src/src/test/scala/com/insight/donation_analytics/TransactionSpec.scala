package com.insight.donation_analytics


class TransactionSpec extends UnitSpec {

  val input = """C00629618|N|TER|P|201701230300133512|15C|IND|PEREZ, JOHN A|LOS ANGELES|CA|90017|PRINCIPAL|DOUBLE NICKEL ADVISORS|01032017|40|H6CA34245|SA01251735122|1141239|||2012520171368850783
                |C00177436|N|M2|P|201702039042410894|15|IND|DEEHAN, WILLIAM N|ALPHARETTA|GA|300047357|UNUM|SVP, SALES, CL|01312017|384||PR2283873845050|1147350||P/R DEDUCTION ($192.00 BI-WEEKLY)|4020820171370029337
                |C00384818|N|M2|P|201702039042412112|15|IND|ABBOTT, JOSEPH|WOONSOCKET|RI|028956146|CVS HEALTH|VP, RETAIL PHARMACY OPS|01122017|250||2017020211435-887|1147467|||4020820171370030285
                |C00384516|N|M2|P|201702039042410893|15|IND|SABOURIN, JAMES|LOOKOUT MOUNTAIN|GA|028956146|UNUM|SVP, CORPORATE COMMUNICATIONS|01312017|230||PR1890575345050|1147350||P/R DEDUCTION ($115.00 BI-WEEKLY)|4020820171370029335
                |C00177436|N|M2|P|201702039042410895|15|IND|JEROME, CHRISTOPHER|LOOKOUT MOUNTAIN|GA|307502818|UNUM|EVP, GLOBAL SERVICES|10312017|384||PR2283905245050|1147350||P/R DEDUCTION ($192.00 BI-WEEKLY)|4020820171370029342
                |C00384516|N|M2|P|201702039042412112|15|IND|ABBOTT, JOSEPH|WOONSOCKET|RI|028956146|CVS HEALTH|EVP, HEAD OF RETAIL OPERATIONS|01122018|333||2017020211435-910|1147467|||4020820171370030287
                |C00384516|N|M2|P|201702039042410894|15|IND|SABOURIN, JAMES|LOOKOUT MOUNTAIN|GA|028956146|UNUM|SVP, CORPORATE COMMUNICATIONS|01312018|384||PR2283904845050|1147350||P/R DEDUCTION ($192.00 BI-WEEKLY)|4020820171370029339
                |""".stripMargin.split("\n")


  "A Transaction" should "skip non-empty other_id" in {
    input.foreach{l => Transaction(l) match {
      case Transaction.Invalid =>
        val s = l.split("\\|")
        logger.debug(s"Invalid: ${s(0)}|${s(7)}|${s(10)}|${s.slice(13, 16).mkString("|")}")
      case t =>
        logger.debug(s"Valid: $t")}
    }

    Transaction(input(0)) should equal (Transaction.Invalid)
  }

  it should "skip invalid date" in {
    val data = input(1).split("\\|")
    data(13) = ""
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
    data(13) = "0123198"
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
    data(13) = "d12324567"
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
  }

  it should "skip invalid zip code" in {
    val data = input(1).split("\\|")
    data(10) = ""
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
    data(10) = "0123"
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
    data(10) = "12324567d"
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
  }

  it should "skip invalid name" in {
    val data = input(1).split("\\|")
    data(7) = ""
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
    data(7) = "0123"
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
    data(7) = "zhang di"
    Transaction(data.mkString("|")) should equal (Transaction.Invalid)
  }
}
