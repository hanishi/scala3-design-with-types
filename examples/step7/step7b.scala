// step7b.scala

import scala.compiletime.error

object Shop:
  // Quantities must be positive
  opaque type Quantity = Int

  inline def quantity(inline n: Int): Quantity =
    inline if n <= 0 then
      error("Quantity must be positive")
    else n

  extension (q: Quantity)
    def value: Int = q
    def *(price: Double): Double = q * price

  // Discount percentages must be 0–100
  opaque type DiscountPct = Int

  inline def discount(inline pct: Int): DiscountPct =
    inline if pct < 0 || pct > 100 then
      error("Discount must be between 0 and 100")
    else pct

  extension (d: DiscountPct)
    def applyTo(price: Double): Double = price * (1.0 - d / 100.0)

import Shop.*

@main def step7b(): Unit =
  val qty = quantity(3)            // OK
  val disc = discount(20)          // OK

  val subtotal = qty * 29.99
  val total = disc.applyTo(subtotal)
  println(f"$qty%d × $$29.99 = $$$subtotal%.2f, after $disc%%  off: $$$total%.2f")

  // val bad = quantity(0)          // error: Quantity must be positive
  // val badDisc = discount(150)    // error: Discount must be between 0 and 100
