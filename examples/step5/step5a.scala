// step5a.scala

import scala.annotation.targetName

object Currency:
  opaque type USD = Double
  opaque type JPY = Double

  def usd(amount: Double): USD = amount
  def jpy(amount: Double): JPY = amount

  extension (x: USD) def toDouble: Double = x
  extension (x: JPY) @targetName("jpyToDouble") def toDouble: Double = x

// Express exchange rates as a relationship between types
trait ExchangeRate[From, To]:
  def convert(amount: Double): Double

object ExchangeRate:
  // "Evidence" that a USD → JPY conversion rate exists
  given usdToJpy: ExchangeRate[Currency.USD, Currency.JPY] with
    def convert(amount: Double): Double = amount * 150.0

  // JPY → USD too
  given jpyToUsd: ExchangeRate[Currency.JPY, Currency.USD] with
    def convert(amount: Double): Double = amount / 150.0

// This function *requires* evidence that a From → To rate exists
def convert[From, To](amount: Double)(using rate: ExchangeRate[From, To]): Double =
  rate.convert(amount)

import Currency.*
import ExchangeRate.given

@main def step5a(): Unit =
  val dollars = usd(100.0)
  val yen = convert[Currency.USD, Currency.JPY](dollars.toDouble)
  println(s"$$100 = ¥$yen")

  // If no given instance exists for EUR → USD:
  // opaque type EUR = Double
  // convert[EUR, Currency.USD](50.0)
  // error: no given instance of type ExchangeRate[EUR, USD] was found
