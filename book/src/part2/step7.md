# Step 7: Inline and Compile-Time Validation

Runtime validation catches bad values when your program runs — an `IllegalArgumentException`
at 3 AM in production. What if the compiler could catch them before it even compiles?

`inline` makes the compiler expand code at the call site. Combined with
`scala.compiletime`, it lets you **reject invalid code at compile time**
with custom error messages.

## 7-1. compiletime.error — Custom Compile Errors

```scala
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
```

**Uncomment and compile.** The error message is yours, not the compiler's.
This is compile-time validation: bugs that would be `IllegalArgumentException` at
runtime become compile errors.

The key constraint: the value must be known at compile time (a literal or another inline value).
For runtime values, you still need runtime validation.

## 7-2. Practical Use: Compile-Time Validated Domain Types

Remember `Currency.USD` from [Step 3](../part1/step3.md)? Opaque types prevented mixing dollars and yen.
Now imagine going further: ensuring the *amount* itself is valid before the code compiles.

```scala
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
```

This combines opaque types ([Step 3](../part1/step3.md)) with inline validation:
the type system ensures `Quantity` and `DiscountPct` can't be confused with plain `Int`,
and `inline` ensures only valid values can be created.

## 7-3. constValue — Reading Types as Values

```scala
// step7c.scala

import scala.compiletime.constValue

// constValue lets you read a singleton type as a runtime value.
// This bridges the gap between type-level and value-level.

// constValue[A] extracts the singleton value from A (e.g., 42 from type 42).
// The match cases check the type of that extracted value.
inline def describe[A]: String =
  inline constValue[A] match
    case _: Int    => "integer"
    case _: String => "string"
    case _: Boolean => "boolean"

@main def step7c(): Unit =
  // Singleton types: the type IS the value
  val x: 42 = 42              // x has type 42, not Int
  val y: "hello" = "hello"    // y has type "hello", not String

  // constValue extracts the value from the type
  val n: Int = constValue[42]        // 42
  val s: String = constValue["hello"] // "hello"
  println(s"n: $n, s: $s")
```

Singleton types and `constValue` are how Scala 3 connects compile-time computation
to runtime values. Libraries like circe and tapir use these techniques to generate
codecs and API documentation from type information alone.
