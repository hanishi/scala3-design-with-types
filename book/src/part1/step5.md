# Step 5: Givens and Typeclasses — Evidence the Compiler Finds for You

## 5-1. The Problem: How Do You Represent Currency Conversion?

```scala
// step5a.scala

object Currency:
  opaque type USD = Double
  opaque type JPY = Double

  def usd(amount: Double): USD = amount
  def jpy(amount: Double): JPY = amount

  extension (x: USD) def toDouble: Double = x
  extension (x: JPY) def toDouble: Double = x

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
```

**The insight:** `using` declares "calling this function requires evidence of this type."
The compiler searches for evidence. If it can't find any, compile error.
**A "missing exchange rate" runtime crash is structurally impossible.**

## 5-2. `summon` — Asking the Compiler to Show Its Work

In Step 5-1, the compiler finds a `given` instance and passes it automatically.
But sometimes you want to grab that evidence yourself. That's what `summon` does.

```scala
// step5_summon.scala

trait Describable[A]:
  def describe: String

object Describable:
  given Describable[Int] with
    def describe: String = "a 32-bit integer"

  given Describable[String] with
    def describe: String = "a sequence of characters"

@main def step5_summon(): Unit =
  // summon[X] says: "Compiler, find me a given instance of type X"
  val intDesc = summon[Describable[Int]]
  println(intDesc.describe)     // "a 32-bit integer"

  val strDesc = summon[Describable[String]]
  println(strDesc.describe)     // "a sequence of characters"

  // No given Describable[Boolean] exists:
  // val boolDesc = summon[Describable[Boolean]]
  // error: no given instance of type Describable[Boolean] was found
```

`summon[X]` is simple: it asks the compiler *"do you have evidence of type X?"*
If yes, it returns that evidence. If no, compile error.

Think of it this way:
- `using` in a parameter list: the compiler passes evidence **to you** automatically
- `summon[X]` in a function body: **you** ask the compiler for evidence explicitly

Both are doing the same thing — retrieving a `given` instance — just from different directions.
You'll see `summon` used frequently from here on. It's the basic verb of type-level inquiry.

## 5-3. Context Bounds — A More Concise Syntax

```scala
// step5b.scala

// Define a "can be displayed" capability as a typeclass
trait Show[A]:
  def show(a: A): String

object Show:
  // How to display an Int
  given Show[Int] with
    def show(a: Int): String = a.toString

  // How to display a String
  given Show[String] with
    def show(a: String): String = s"\"$a\""

  // How to display a List[A] (only if A itself has Show)
  given [A](using inner: Show[A]): Show[List[A]] with
    def show(a: List[A]): String =
      a.map(inner.show).mkString("[", ", ", "]")

// Context bound: A: Show means "a given Show[A] must exist"
def display[A: Show](a: A): String =
  summon[Show[A]].show(a)

@main def step5b(): Unit =
  println(display(42))
  println(display("hello"))
  println(display(List(1, 2, 3)))
  println(display(List(List(1, 2), List(3, 4))))

  // No given Show[Boolean] exists:
  // println(display(true))
  // error: no given instance of type Show[Boolean] was found
```

**The structure:**
1. `trait Show[A]` — typeclass definition ("this capability exists")
2. `given Show[Int]` — instance for a specific type ("Int has this capability")
3. `[A: Show]` — context bound ("A must have this capability")

The crucial property: you can add capabilities **after the fact**.
Define `Show[Int]` without touching `Int`'s source code. That's ad hoc polymorphism.

> **You made it through Part 1.** You now have the full everyday type system toolkit:
> type parameters, variance, bounds, opaque types, type members, and typeclasses.
> Most production Scala code lives entirely within these concepts.
> Everything from here builds on what you already know.
