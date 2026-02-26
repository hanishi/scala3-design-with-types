# Step 8: =:= Evidence — Proving Type Equality

In generic code, the compiler sometimes can't tell whether two type parameters
are the same type. You can give it proof — and unlock methods that only make sense
when certain type relationships hold.

## 8-1. Evidence That Two Types Are the Same

`summon` asks the compiler for evidence that a given instance exists.
`=:=` takes this further: it's evidence that **two types are equal**.

```scala
{{#include ../../../examples/step8/step8a.scala}}
```

If your reaction is "why would I ever need to *prove* types are equal?" — that's fair.
In most application code, you wouldn't. But in generic code (libraries, frameworks,
reusable abstractions), the compiler sometimes genuinely doesn't know whether two
type parameters refer to the same type. That's where `=:=` earns its keep.

## 8-2. Practical Use: Conditional Methods on Generic Types

The real power of `=:=` appears when you want a method that **only exists
under certain type conditions**:

```scala
{{#include ../../../examples/step8/step8b.scala}}
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

> Next, [Step 9](./step9.md) adds one more dimension: **combining types** with union and
> intersection types — expressing "either/or" and "both/and" at the type level.
