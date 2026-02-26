# Step 6: Match Types — When the Return Type Depends on the Input Type

Everything so far uses types to describe what things *are*. But what if the return type
of a function should *change* depending on what you pass in? A function that unwraps
`Option[Int]` to `Int` but unwraps `List[String]` to `String`?

Match types are type-level `if-else`. They're useful when a function's **return type**
should change based on the **input type** — something that typeclasses and bounds can't express.

## 6-1. The Problem: Unwrapping Nested Types

```scala
{{#include ../../../examples/step6/step6a.scala}}
```

## 6-2. Practical Use: Type-Safe API Response Handling

```scala
{{#include ../../../examples/step6/step6b.scala}}
```

**Why this matters:** Without match types, you'd either return `Any` and cast (unsafe),
or have separate `fetchCampaign`, `fetchMetrics`, `fetchBudget` functions (boilerplate).
Match types give you one function with full type safety.

## 6-3. When Match Types Are the Wrong Tool

Match types compute a **different type** from an input type.
If the output type is always the same (e.g., always `String`), there's nothing to compute —
use a typeclass instead ([Step 5](../part1/step5.md)).

```
Output type changes based on input type  → Match type
Same output type, different behavior      → Typeclass
```
