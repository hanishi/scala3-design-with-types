# Step 2: Variance and Bounds

Variance is not a set of rules to memorize.
It's something you experience by asking "why won't this compile?" alongside the compiler.
The starting point is **invariant** (no compatibility). The fact that this is the default matters.

## 2-1. Invariant — The Default Wall

```scala
{{#include ../../../examples/step2/step2a.scala}}
```

**Uncomment and compile.**

The compiler treats `Box[Book]` and `Box[Item]` as **completely different types**.
Just because `Book <: Item` doesn't mean `Box[Book] <: Box[Item]`.

> **Notation:** `<:` means "is a subtype of" and `>:` means "is a supertype of."
> `Book <: Item` reads as "Book is a subtype of Item" — i.e., `Book extends Item`.
> You'll see these symbols used both in prose and in Scala code (type bounds) throughout this chapter.

The compiler's default: *"Show me it's safe, and I'll allow it."*

## 2-2. Why Invariant Is the Default — Break It to Understand

```scala
{{#include ../../../examples/step2/step2b.scala}}
```

This is a thought experiment, but in Java **it actually happens**:

```java
{{#include ../../../examples/step2/Step2bJava.java}}
```

**Try it** (requires `javac` and `java`):
```sh
javac Step2bJava.java && java Step2bJava
```

It compiles without warnings — and crashes at runtime with `ArrayStoreException`.

Scala's invariant default is a design decision to **eliminate this class of bugs at compile time**.

## 2-3. Making It Covariant — What `+A` Means

```scala
{{#include ../../../examples/step2/step2c.scala}}
```

With `+A`, subtyping lifts through the container:
if `Book <: Item`, then `Box[Book] <: Box[Item]`.

This `Box` only holds a value. What if you also want to check what's inside, or change it?

## 2-4. Testing the Compiler's Checks

**Uncomment each block one at a time.**

```scala
{{#include ../../../examples/step2/step2d.scala}}
```

With an invariant `Box[A]`, both `var value: A` and `contains(item: A)` are safe.
This is because `Box[Book]` is not a `Box[Item]`.
The type cannot be widened through subtyping, so no incompatible value can be introduced.

Once we declare `Box[+A]`, Scala permits `Box[Book]` to be viewed as `Box[Item]`.
From that broader perspective:
- `var value: A` would allow assigning a `DVD` into what is actually a `Box[Book]`
- `contains(DVD(...))` would appear valid — since a `DVD` is an `Item`

However, the underlying object remains a `Box[Book]`. To preserve type safety, Scala rejects both cases:
- `var value: A` → *"invariant position"* — a `var` is both read and write
- `contains(item: A)` → *"contravariant position"* — a parameter consumes values

A covariant type parameter (`+A`) may appear only in **output** positions, such as:
- `val value: A`
- a method return type

> **Why reject `contains`, even though it seems harmless?** Scala enforces variance rules structurally —
> it does not analyze method implementations.
> While `contains` would be harmless in this case, other members — like `var value: A` — would be unsound.
> The rule is uniform: a covariant type parameter (`+A`) cannot appear in input positions.

So how can we retain covariance without giving up useful inputs?
That's where **type bounds** come in.

## 2-5. Lower Bounds — Escaping the Variance Constraint

In 2-4, we saw that `+A` rejects `A` in input positions.

`B >: A` means "`B` is a supertype of `A`" — `B` must be `A` or something above it.
That's what makes this possible: rather than accepting `A` directly (which `+A` forbids),
the method asks the compiler to find a type `B` broad enough for both
what's already in the cart and what's being added.
The cart's type either stays the same or widens — never narrows.

```scala
{{#include ../../../examples/step2/step2e.scala}}
```

This is exactly what `List[+A]`'s `prepended` does:

```scala
// Simplified definition of List
sealed abstract class List[+A]:
  def prepended[B >: A](elem: B): List[B]
```

Prepend a `DVD` to a `List[Book]` and you get `List[Item]`. The types don't lie.

### Where do lower bounds show up?

You might wonder: *"Are lower bounds basically used in the same kinds of situations?"*

Yes — they cluster around a small set of roles:

**Widening containers** — inserting into covariant collections:

```scala
val books: List[Book] = List(Book("Scala", 45.0))
val items: List[Item] = books :+ DVD("The Matrix", 19.99)
// List[Book] widened to List[Item]
```

**Fallback values** — `Option.getOrElse[B >: A](default: => B): B`:

```scala
val maybeBook: Option[Book] = None
val item: Item = maybeBook.getOrElse(DVD("The Matrix", 19.99))
// Option[Book] → no Book present → falls back to DVD → result is Item
```

**Combining/merging** — `Either`'s `orElse` widens the `Right` type:

```scala
val result: Either[String, Book] = Left("not found")
val recovered: Either[String, Item] = result.orElse(Right(DVD("The Matrix", 19.99)))
// Either[String, Book] → recovery with DVD → Either[String, Item]
```

In each case, the pattern is the same: `[B >: A]` lets the type widen to accommodate
a value that doesn't fit `A` exactly. The compiler picks the narrowest type that works —
`Item`, not `Any`. This is called the **LUB** (Least Upper Bound).
In [Step 9](../part2/step9.md), we'll see what happens when there *is* no named common
supertype — Scala 3 uses union types instead.

You won't write `[B >: A]` in everyday application code — but those foundational APIs
genuinely depend on it. Without lower bounds, covariant containers simply couldn't
have "add" methods.

One thing to keep in mind: widening means callers get back a less precise type.
Use `[B >: A]` when widening is a real requirement, not as a precaution.

## 2-6. Upper Bounds — Requiring Minimum Behavior

If lower bounds **widen**, upper bounds **narrow**.

```scala
{{#include ../../../examples/step2/step2f.scala}}
```

### Where do upper bounds show up?

Like lower bounds, they cluster around a few roles:

**Requiring specific behavior** — only accept types with certain methods:

```scala
def sortByShipping[A <: Shippable](items: List[A]): List[A] =
  items.sortBy(_.shippingCost)
// Only works for Shippable items — DigitalGiftCertificate rejected at compile time
```
`[A <: Shippable]` tells the compiler: "only accept types that extend `Shippable`."
Inside the function, you can call `_.shippingCost` because the compiler *knows* every `A`
has that method. `DigitalGiftCertificate` extends `Item` but not `Shippable` — so the
compiler rejects it before the code even runs.

**Constraining type parameters on classes** — ensuring a container only holds suitable types:

```scala
class Shipment[+A <: Shippable](items: List[A]):
  def totalShippingCost: Double = items.map(_.shippingCost).sum
  def add[B >: A <: Shippable](item: B): Shipment[B] = Shipment(items :+ item)
// Can't create Shipment[DigitalGiftCertificate] — not Shippable
```
The `+` is the same covariance you saw in section 2-4 — it just works alongside
the bound. `<: Shippable` restricts which types are allowed, `+` makes
`Shipment[Book] <: Shipment[Shippable]`.

The `add` method uses `[B >: A <: Shippable]` — the same `[B >: A]` escape hatch
from section 2-5, combined with `<: Shippable` to stay within the class's bound.
Adding a `DVD` to a `Shipment[Book]` widens it to `Shipment[Shippable]`.

> **Why two bounds?** 
>
> You might expect `B` to inherit the `<: Shippable`
> bound from the class — after all, `A` is already constrained. But `B` is
> its own type parameter, introduced on the method. It only knows what you
> tell it.
>
> And `B >: A` points *up* the hierarchy. In Scala, every type has `AnyRef`
> and `Any` above it, so without a cap `B` could widen all the way:
> `Book → Shippable → Item → AnyRef → Any`. The `<: Shippable` says
> "stop here." Without it, `Shipment[B]` wouldn't compile — `Shipment`
> requires its type parameter to be `<: Shippable`.

**Preserving type information** — keeping the specific subtype through a computation:

```scala
def cheapest[A <: Item](items: List[A]): A =
  items.minBy(_.price)

val books: List[Book] = List(Book("Scala", 45.0), Book("FP", 35.0))
val b: Book = cheapest(books)  // returns Book, not Item

val dvds: List[DVD] = List(DVD("The Matrix", 19.99), DVD("Inception", 24.99))
val d: DVD = cheapest(dvds)  // returns DVD, not Item

val cards: List[DigitalGiftCertificate] = List(DigitalGiftCertificate("$50", 50.0))
val c: DigitalGiftCertificate = cheapest(cards)  // returns DigitalGiftCertificate, not Item
```

Without the bound, you'd have to write `def cheapest(items: List[Item]): Item` — and every
call would return `Item`, even when the list contains `Book` or `DVD`. You'd preserve
the behavior, but lose the specific return type.

You also can't write `def cheapest[A](items: List[A]): A`, because `minBy(_.price)`
requires that each element has a `.price` member. With no bound, the compiler has no
reason to believe that about `A`.

The upper bound gives you both: access to `Item`'s API and the precise element type
back (`Book` in, `Book` out; `DVD` in, `DVD` out). In other words, `<: Item` is not
about restricting callers — it's about telling the compiler what operations are valid
while still keeping the result type maximally specific.

**Standard library** — `WeakReference[+T <: AnyRef]` only accepts reference types:

```scala
import scala.ref.WeakReference
val ref = WeakReference(List(1, 2, 3))  // OK: List is AnyRef
// WeakReference(42) — won't work: Int is not AnyRef
```

A normal reference (`val x = ...`) keeps an object alive — the garbage collector won't
reclaim it as long as someone points to it. A `WeakReference` is different: it holds
a reference without preventing the GC from reclaiming the object. When memory is tight,
the GC can reclaim it, and the weak reference silently becomes empty.

The most common use case is caches. You want to keep a computed result around
for reuse, but not at the cost of running out of memory:

```scala
import scala.ref.WeakReference

class Cache[T <: AnyRef](compute: () => T):
  private var ref: WeakReference[T] = WeakReference(null)

  def get(): T = ref.get match
    case Some(value) => value        // still in memory, reuse it
    case None =>                     // GC reclaimed it, recompute
      val value = compute()
      ref = WeakReference(value)
      value

val users = Cache(() => List("alice", "bob", "charlie"))
users.get()  // computes the first time, reuses if GC hasn't reclaimed
```

This only makes sense for heap-allocated objects (`AnyRef`). Value types like `Int`
and `Double` are stored directly on the stack or inlined into objects — there's no
heap object for the GC to track or reclaim. The upper bound `T <: AnyRef` encodes
this JVM reality at the type level: you literally *can't* create a weak reference
to something that doesn't live on the heap.

Don't worry if this feels advanced — you can safely skip this example and come back to it later. It's a clever use of upper bounds, not a prerequisite for what follows.

Lower bounds *widen* — they accept broader types. Upper bounds *narrow* — they
reject types that lack the required behavior.

> **You now have the full toolkit for the output side:** covariance (`+A`) for safe widening,
> lower bounds (`>:`) to add methods without breaking covariance, and upper bounds (`<:`)
> to require specific behavior. Next up: the input side.

## 2-7. Contravariance — Consumer Compatibility

Finally, `-A`. Covariance is about the **output** side — producers.
Contravariance is about the **input** side — consumers.

```scala
{{#include ../../../examples/step2/step2g.scala}}
```

Most tutorials explain contravariance with explicit assignment like
`val bookFmt: Formatter[Book] = itemFormatter` — like the one in the example
above. That makes the subtyping visible, but you rarely write code like that.
Where you *do* use contravariance — without realizing it — is every `.filter`,
`.map`, `.sortBy` call where you pass a function on a supertype.

`Function1[-A, +B]` — the desugared form of `A => B` — is the most common
contravariant type in the standard library. 

`A` is in the **input position** —
the function *receives* it — so it's contravariant (`-A`). 

`B` is in the **output
position** — the function *returns* it — so it's covariant (`+B`). 

That's why `Item => Boolean` is usable wherever `Book => Boolean` is expected:

```scala
{{#include ../../../examples/step2/step2g1.scala}}
```

The pattern: if a type **consumes** `A` (takes it as input), it's a candidate for `-A`.
If it **produces** `A` (returns it as output), it's a candidate for `+A`.

Why does the direction flip? Look at `val cheap: Item => Boolean = _.price < 40.0`.
Every subtype of `Item` — `Book`, `DVD`, `DigitalGiftCertificate` — has `.price`.
So a function that only uses `Item`'s properties naturally works for any of them.
You write it once and reuse it with `List[Book]`, `List[DVD]`, or `List[Item]`.

That's what contravariance means in practice: **a consumer of a broader type is a
safe substitute for a consumer of a narrower type.**

The `PriceFormatter` from the first example is the same idea made explicit.
An `ItemFormatter` only uses `name` and `price` — things every `Item` has. So it
can handle a `Book`, a `DVD`, anything. A `BookFormatter` uses `isbn` — something
only `Book` has. Hand it a `DVD` and it breaks. Hence the flip:
`Book <: Item`, but `PriceFormatter[Item] <: PriceFormatter[Book]`.

### What about bounds for contravariance?

With covariance, `+A` forbids `A` in input positions (method parameters — where the
method *receives* values). Lower bounds (`[B >: A]`) are the escape hatch, as you saw
in section 2-5. 

Symmetrically, `-A` forbids `A` in output positions (return types —
where the method *produces* values). Upper bounds (`[B <: A]`) would be the escape hatch.

For simple consumers like `PriceFormatter[-A]` or `Function1[-A, +B]`, this never
comes up — they return `String` or `Boolean`, not `A`.

But it *does* come up when you compose contravariant types. Here's a simplified
inspired by [ZIO](https://zio.dev/), a popular Scala library for building concurrent
and asynchronous programs. 

ZIO's actual type is `ZIO[-R, +E, +A]` (environment,
error, value) — we drop the error channel here to focus on variance. Think of
`Effect[-R, +A]` as a computation that needs an environment `R` to run and
produces a value `A`. The environment is *consumed* (`-R`) and the value is
*produced* (`+A`) — the same variance shape as `Function1[-A, +B]`, which makes
sense since `Effect` is essentially a wrapper around `R => A`:

```scala
{{#include ../../../examples/step2/step2g2.scala}}
```

Compare `flatMap` on both types. In `List[+A]`, the `A` comes from the class's `+A`
— and it lands in the right position without any bound. In `Effect[-R, +A]`, the `R`
comes from the class's `-R` — but the extra nesting of `Effect[R, B]` inside
`Function1` changes where `R` lands, and a bound becomes necessary.

To trace this, we need to understand how the compiler decides. It checks whether
each type parameter lands in an **input** or **output** position:

- Method parameters are **input** — the method *receives* them → position is `(-)`
- Return types are **output** — the method *produces* them → position is `(+)`
- So for `Effect[-R, +A]`: `+A` must end up in `(+)` positions, `-R` must end up in `(-)` positions.

When types are nested, positions multiply like signs in arithmetic:

```
(+) × (+) = (+)     output inside output → still output
(+) × (-) = (-)     output inside input → flips to input
(-) × (-) = (+)     input inside input → flips back to output
```

**`List[+A]`'s flatMap — no bound needed:**

Desugar: `flatMap[B](f: Function1[A, IterableOnce[B]]): List[B]`

| Step | Where is A? | Position |
|------|-------------|----------|
| `f` is a method parameter | start | `(-)` |
| `A` is in the `-A` slot of `Function1` | `(-) × (-)` | `(+)` |

Result: `A` lands in `(+)`. We declared `+A`. **OK.**

**`Effect[-R, +A]`'s flatMap — what if we tried without a bound?**

Desugar (without bound): `flatMap[B](f: Function1[A, Effect[R, B]]): Effect[R, B]`

| Step | Where is R? | Position |
|------|-------------|----------|
| `f` is a method parameter | start | `(-)` |
| `Effect[R, B]` is in the `+B` slot of `Function1` | `(-) × (+)` | `(-)` |
| `R` is in the `-R` slot of `Effect` | `(-) × (-)` | `(+)` |

Result: `R` lands in `(+)`. We declared `-R`. **Rejected.**

The fix:

```scala
// Desugared => for readability
def flatMap[R1 <: R, B](f: Function1[A, Effect[R1, B]]): Effect[R1, B]
```

`R1 <: R` means `R1` is `R` or a subtype of `R` — it has at least everything `R` has.
Variance rules (`+` and `-`) only apply to the class's own type parameters.
`R1` is a type parameter on the *method*, so it's not subject to variance checking —
the compiler just checks that `R1 <: R` holds at the call site.

And `R1 <: R` isn't just a trick to satisfy the variance checker — it's what makes
the implementation work. Look at the body:

```scala
Effect(r => f(run(r)).run(r))
```

`r` has type `R1`. The outer effect's `run` expects `R`. Since `R1 <: R`, passing
`r` where `R` is expected works — `R1` has everything `R` needs. The same `r`
feeds both effects.

This is the same pattern as `[B >: A]` for covariance, but in the opposite direction:
lower bounds widen covariant types, upper bounds narrow contravariant types.

## 2-8. Quick Reference

```
Invariant (default)   → No compatibility assumed
+A (covariant)        → Output side — safe to widen
-A (contravariant)    → Input side — safe in the opposite direction
>: (lower bound)      → Escape hatch for covariant types (widen)
<: (upper bound)      → Escape hatch for contravariant types (narrow)
```

But knowing the mechanics is only half the story. The next question is:
*in practice, how often do you actually want invariant, and when should you reach for `+` or `-`?*

> **Take a breath.** The hard conceptual work is done. Sections 2-9 through 2-11
> show how variance plays out in real code — these are shorter and more concrete.
