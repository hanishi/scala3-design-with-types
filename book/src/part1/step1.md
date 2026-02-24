# Step 1: Type Parameters Are Contracts

## 1-1. Build Your Own Box

```scala
// step1.scala

// A box that holds anything (no type parameter)
class AnyBox(val value: Any)

// A box with a type parameter
class Box[A](val value: A)

@main def step1(): Unit =
  // AnyBox: easy to put things in
  val anyBox = AnyBox(42)
  // val n: Int = anyBox.value  // Compile error! Any is not Int
  val n: Int = anyBox.value.asInstanceOf[Int]  // Dangerous cast

  // Box[A]: the type is preserved
  val intBox = Box(42)       // Inferred as Box[Int]
  val m: Int = intBox.value  // Comes out as Int — no cast needed

  // Wrong type? Compile error.
  // val s: String = intBox.value  // error: Found Int, Required String

  println(s"AnyBox: $n, Box[Int]: $m")
```

**Try it:**
```
> scala-cli run step1.scala
```

Then uncomment `val s: String = intBox.value`.

**The insight:** The `A` in `Box[A]` doesn't "remember" what you put in.
The compiler **commits** to `A = Int` at construction time. That's a contract.
When you take the value out, it's guaranteed to be `Int`.

## 1-2. Type Parameters in Functions — Contracts Propagate

```scala
// step1b.scala

def first[A](xs: List[A]): A = xs.head

@main def step1b(): Unit =
  val names = List("Scala", "Rust", "Go")
  val top: String = first(names)  // Compiler resolves A = String
  println(top)

  val nums = List(1, 2, 3)
  val one: Int = first(nums)      // Compiler resolves A = Int
  println(one)

  // What about this?
  val mixed = List(1, "two", 3.0)
  val what = first(mixed)         // What is A here?
  println(what.getClass)
```

**Inspect the types:**
```
> scala-cli run step1b.scala
> # To see the inferred type:
> scala-cli compile step1b.scala -- -Xprint:typer 2>&1 | grep "mixed"
```

`mixed` is inferred as `List[Int | String | Double]` — a union type. This is a Scala 3 thing.
