// step3a.scala

// Naive approach: type aliases
type USD = Double
type JPY = Double

@main def step3a(): Unit =
  val price: USD = 19.99
  val yen: JPY = 2000.0

  // This compiles!
  val total: USD = price + yen  // USD + JPY = ??? Nonsensical math
  println(s"Total: $total")      // 2019.99 ... what does this even mean?

  // A type alias is just an alternate name, not a distinct type.
  // To the compiler, USD and JPY are both just Double.
