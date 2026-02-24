# Step 8: =:= Evidence — Proving Type Equality

In generic code, the compiler sometimes can't tell whether two type parameters
are the same type. You can give it proof — and unlock methods that only make sense
when certain type relationships hold.

## 8-1. Evidence That Two Types Are the Same

`summon` asks the compiler for evidence that a given instance exists.
`=:=` takes this further: it's evidence that **two types are equal**.

```scala
// step8a.scala

@main def step8a(): Unit =
  // =:= is evidence that two types are equal
  val evidence: Int =:= Int = summon[Int =:= Int]  // OK

  // No evidence that String =:= Int
  // val bad = summon[String =:= Int]
  // error: Cannot prove that String =:= Int.

  // The evidence object can convert values between the two types
  def convert[A, B](a: A)(using ev: A =:= B): B = ev(a)

  val x: Int = 42
  val y: Int = convert[Int, Int](x)  // OK: Int =:= Int can be proven
  println(y)

  // val z: String = convert[Int, String](x)  // Compile error!
```

If your reaction is "why would I ever need to *prove* types are equal?" — that's fair.
In most application code, you wouldn't. But in generic code (libraries, frameworks,
reusable abstractions), the compiler sometimes genuinely doesn't know whether two
type parameters refer to the same type. That's where `=:=` earns its keep.

## 8-2. Practical Use: Conditional Methods on Generic Types

The real power of `=:=` appears when you want a method that **only exists
under certain type conditions**:

```scala
// step8b.scala

// A generic pipeline stage that transforms From → To
class Stage[From, To](val transform: From => To):
  // Chain two stages: Stage[A, B] andThen Stage[B, C] = Stage[A, C]
  def andThen[Next](next: Stage[To, Next]): Stage[From, Next] =
    Stage(from => next.transform(transform(from)))

  // "run" only makes sense when input and output are the same type
  // (a stage that can be applied repeatedly)
  def runLoop(value: From, times: Int)(using ev: From =:= To): From =
    var current = value
    for _ <- 1 to times do
      current = ev.flip(transform(current)) // To → From via evidence
    current

@main def step8b(): Unit =
  // Stage[Int, Int] — doubles a number
  val doubler = Stage[Int, Int](_ * 2)
  println(doubler.runLoop(1, 5))  // 1 → 2 → 4 → 8 → 16 → 32

  // Stage[String, String] — adds exclamation
  val exclaim = Stage[String, String](_ + "!")
  println(exclaim.runLoop("hello", 3))  // hello! → hello!! → hello!!!

  // Stage[String, Int] — can't loop, types don't match
  val counter = Stage[String, Int](_.length)
  // counter.runLoop("hi", 3)
  // error: Cannot prove that String =:= Int.
  // Makes sense: you can't feed an Int back into something expecting String
```

Without `=:=`, you'd have two options: either make `runLoop` available on all stages
(and crash at runtime when types don't match), or create a separate `LoopableStage` class
(duplicating code). `=:=` lets you keep one class with a method that the compiler
only allows when it's safe.

## 8-3. Connecting It Back: Types as Proof

Notice the progression through this tutorial:

```
Part 1 — The Type System:
Step 1:  [A]                  "There exists a type A"
Step 2:  [A <: Shippable]     "A has at least these capabilities"
Step 4:  type Row             "This type belongs to this specific instance"
Step 5:  using Show[A]        "Evidence exists that A can be shown"

Part 2 — Type-Level Programming:
Step 6:  ResponseOf[E]        "The output type is computed from the input type"
Step 7:  inline + error       "Invalid values are rejected at compile time"
Step 8:  using A =:= B        "A and B are proven to be the same type"
```

Each step gives the compiler a **stronger statement** about types.
Part 1 describes what types *are*. Part 2 makes types *compute and validate*.
The compiler checks your claims at every level.

> **That's the complete progression** — from basic type parameters to type equality proofs.
> The [Interlude](../interlude.md) below puts it all together in a practical design exercise.
