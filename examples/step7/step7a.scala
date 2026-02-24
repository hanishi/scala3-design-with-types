// step7a.scala

import scala.compiletime.error

// A percentage must be between 0 and 100.
// With runtime validation, you find out at runtime. With inline, at compile time.

inline def percentage(inline value: Int): Int =
  inline if value < 0 || value > 100 then
    error("Percentage must be between 0 and 100")
  else
    value

@main def step7a(): Unit =
  val valid = percentage(85)     // OK
  println(s"Valid: $valid")

  // val invalid = percentage(150)
  // error: Percentage must be between 0 and 100

  // val negative = percentage(-5)
  // error: Percentage must be between 0 and 100
