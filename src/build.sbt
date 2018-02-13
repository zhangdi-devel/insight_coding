name := "donation-analytics"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.12" % "3.0.4" % "test",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25")


parallelExecution in Test := false

test in assembly := {}

mainClass in assembly := Some("com.insight.donation_analytics.DonationAnalytics")

assemblyJarName in assembly := "donation_analytics.jar"