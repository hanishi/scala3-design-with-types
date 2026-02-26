# Step 3: Opaque Types — Zero-Cost Type Safety

You've seen how type parameters prevent mixing types. But what about mixing *values*
of the same underlying type — dollars and yen that are both `Double`? This is where
Scala 3's opaque types come in.

## 3-1. The Problem: Type Aliases Lie

```scala
{{#include ../../../examples/step3/step3a.scala}}
```

## 3-2. Opaque Types Fix This

```scala
{{#include ../../../examples/step3/step3b.scala}}
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
