# Step 5: Givens and Typeclasses — Evidence the Compiler Finds for You

## 5-1. The Problem: How Do You Represent Currency Conversion?

```scala
{{#include ../../../examples/step5/step5a.scala}}
```

**The insight:** `using` declares "calling this function requires evidence of this type."
The compiler searches for evidence. If it can't find any, compile error.
**A "missing exchange rate" runtime crash is structurally impossible.**

## 5-2. `summon` — Asking the Compiler to Show Its Work

In Step 5-1, the compiler finds a `given` instance and passes it automatically.
But sometimes you want to grab that evidence yourself. That's what `summon` does.

```scala
{{#include ../../../examples/step5/step5_summon.scala}}
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
{{#include ../../../examples/step5/step5b.scala}}
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
