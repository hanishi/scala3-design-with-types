// step3b.scala

import scala.annotation.targetName

object Currency:
  // opaque type: outside this object, the Double representation is invisible
  opaque type USD = Double
  opaque type JPY = Double

  // Constructors (only here is Double → USD conversion allowed)
  def usd(amount: Double): USD = amount
  def jpy(amount: Double): JPY = amount

  // Only allow adding same currencies
  extension (x: USD)
    @targetName("addUSD")
    def +(y: USD): USD = x + y     // internally Double + Double
    def show: String = f"$$$x%.2f"

  extension (x: JPY)
    @targetName("addJPY")
    def +(y: JPY): JPY = x + y
    @targetName("showJPY")
    def show: String = f"¥$x%.0f"

import Currency.*

@main def step3b(): Unit =
  val price = usd(19.99)
  val shipping = usd(5.00)
  val total = price + shipping   // OK: USD + USD
  println(total.show)            // $24.99

  val yen = jpy(2000)
  val yenTotal = yen + jpy(500)  // OK: JPY + JPY
  println(yenTotal.show)         // ¥2500

  // USD + JPY → Compile error!
  // val mistake = price + yen
  // error: Found Currency.JPY, Required Currency.USD
