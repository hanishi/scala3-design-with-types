# Step 2: Variance in Practice

## 2-9. Covariance in Practice — You Already Use It

You don't need custom types to see covariance — it's in the standard library types
you use every day. Each one also shows the `[B >: A]` lower bound escape hatch
from section 2-5:

```scala
{{#include ../../../examples/step2/step2h.scala}}
```

The code introduces `Nothing` — Scala's **bottom type**. It's a subtype of every type,
and no value of type `Nothing` can ever exist. The compiler uses it as a placeholder
when one side of a type is unspecified: `Right(book)` is `Right[Nothing, Book]`,
`Left("error")` is `Left[String, Nothing]`, `List()` is `List[Nothing]`, and `None`
is `Option[Nothing]`. Covariance makes this work — since `Nothing <: A` for any `A`,
these always fit wherever a type is expected.

You already saw this in section 2-5: `Cart()` is `Cart[Nothing]`. Adding a `Book`
widens it to `Cart[Book]` via the `[B >: A]` lower bound.

The pattern is the same in every case: the type **produces** (returns, holds, emits)
values of `A`, so `+A` is natural. And when you need to add or combine values of
a different subtype, `[B >: A]` widens the type safely.

## 2-10. Contravariance in Practice — Handlers, Validators, Serializers

Contravariance is less common but appears in a very specific pattern:
types that **consume** or **process** values.

```scala
{{#include ../../../examples/step2/step2i.scala}}
```

You already saw this pattern in section 2-7: `Function1[-A, +B]` is contravariant
in its input, which is why `books.filter(cheap)` works when `cheap` is `Item => Boolean`.
The `JsonWriter` here is the same idea — if it can serialize any `Item`, it can
serialize a `Book`.

## 2-11. So Is Invariant Actually Useful?

Yes, but its role is narrower than you might think.

Invariant is the right choice when your type **both reads and writes** — mutable containers,
bidirectional channels, read-write references. But in well-designed Scala code,
these are relatively rare because immutability is preferred.

```scala
{{#include ../../../examples/step2/step2j.scala}}
```

**The practical reality:**

Most types you design in Scala fall into one of two camps:

```
Produces A (read-only data, results, events, streams)  → make it covariant [+A]
Consumes A (handlers, writers, validators, orderings)   → make it contravariant [-A]
```

Invariant is what you get when you **don't annotate** — and often that's because
you haven't yet thought about whether your type is a producer or consumer.
When you do think about it, you'll find that one of `+` or `-` usually applies.

The discipline of asking "does this type produce or consume `A`?" is itself
a valuable design exercise. It forces you to clarify your type's role,
and the compiler verifies your answer.
