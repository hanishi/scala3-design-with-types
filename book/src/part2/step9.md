# Step 9: Union and Intersection Types — Combining Types

In [Step 2](../part1/step2.md), you saw the compiler pick `Item` as the LUB when mixing
`Book` and `DVD`. That works because `Item` exists. But what happens when the types
share no common supertype? In Scala 2, you'd get `Any`. In Scala 3, you can say
exactly what you mean.

Union types (`A | B`) express "either this or that." Intersection types (`A & B`)
express "both this and that." Together they complete the type-level toolkit —
types don't just describe, compute, and prove; they **combine**.

## 9-1. Union Types — "Either This or That"

```scala
{{#include ../../../examples/step9/step9a.scala}}
```

The key insight: **use union types when the types are unrelated.** `String | Int` has no
named supertype — a union is the only way to express this without `Any`.

But when you own the types and they share a trait (like `Book` and `DVD` sharing `Item`),
use the trait. `List[Item]` is clearer than `List[Book | DVD]` and gives you access
to shared members like `name` and `price` without pattern matching.

## 9-2. Intersection Types — "Both This and That"

```scala
{{#include ../../../examples/step9/step9b.scala}}
```

`Printable & Serializable` means "a value that has **all** members of both traits."
The compiler enforces this: only types that extend both traits can be passed
to `printAndSave`. No casts, no runtime checks.

This is different from creating a trait that extends both (`trait PrintableAndSerializable
extends Printable, Serializable`). Intersection types are **structural** — you don't
need to predefine the combination. Any type that happens to implement both traits qualifies.

## 9-3. Practical Use: Union and Intersection Together

```scala
{{#include ../../../examples/step9/step9c.scala}}
```

Union types work well for return types — "this function returns success or failure."
Intersection types work well for parameter types — "this function requires something
readable and closeable." Together they let you express precise contracts without
building deep class hierarchies.

## 9-4. When to Use What

**Union types vs sealed traits:**

```
You own the types and they share behavior → Sealed trait (Item, Book, DVD)
Types are unrelated or from different libraries → Union type (String | Int)
You need exhaustiveness checking            → Sealed trait
You want ad-hoc "one of these" without boilerplate → Union type
```

**Intersection types vs trait composition:**

```
You need a named, reusable combination     → Trait that extends both
You want a one-off "must have both" constraint → Intersection type (A & B)
The combination is structural, not designed → Intersection type
```

**Connection to variance:** In [Step 2](../part1/step2.md), the LUB picks the narrowest
named supertype — `Item`, not `Any`. Union types give you an even more precise
alternative when no named supertype exists: `Book | DVD` instead of `Any`.
The compiler keeps track of exactly which types are possible.
