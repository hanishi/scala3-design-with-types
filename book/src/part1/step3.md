# Step 3: Opaque Types — Zero-Cost Type Safety

You've seen how type parameters prevent mixing types. But what about mixing *values*
of the same underlying type — dollars and yen that are both `Double`? This is where
Scala 3's opaque types come in.

## 3-1. The Problem: Type Aliases Lie

```scala
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
```

## 3-2. Opaque Types Fix This

```scala
// step3b.scala

object Currency:
  // opaque type: outside this object, the Double representation is invisible
  opaque type USD = Double
  opaque type JPY = Double

  // Constructors (only here is Double → USD conversion allowed)
  def usd(amount: Double): USD = amount
  def jpy(amount: Double): JPY = amount

  // Only allow adding same currencies
  extension (x: USD)
    def +(y: USD): USD = x + y     // internally Double + Double
    def show: String = f"$$$x%.2f"

  extension (x: JPY)
    def +(y: JPY): JPY = x + y
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
```

**Try it:**
```
> scala-cli run step3b.scala
> # Uncomment price + yen to see the error
```

**The key point:**
`opaque type USD = Double` means:
- At compile time: `USD` and `Double` are **different types**. Can't mix them.
- At runtime: `USD` **is just a `Double`**. No boxing. Zero overhead.

A `case class USD(value: Double)` would allocate an object. Opaque types are truly zero-cost.
This is the purest expression of "types are contracts with the compiler, not runtime entities."
