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

Java *generics* are invariant — `List<Book>` is not `List<Item>`, and the compiler
rejects the assignment. Uncomment the generics section in the example to see this.
Arrays, designed earlier, are covariant — and that's where the runtime crash comes from.

Scala's invariant default applies to everything — arrays included — and **eliminates
this class of bugs at compile time**.

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
Think of the compiler saying:
*"You declared `+A`, so I can't let you take an `A` as input — that would break
covariance. But give me a `B` that's a supertype of `A`, and I'll find the narrowest
type that fits both what's already here and what you're adding.
If it already fits, `B` is just `A` and nothing changes. Otherwise the cart
widens — but it never narrows."*

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

Why is widening safe? Think of it as the compiler telling you:
*"I'll allow this — every element has at least `Item`'s methods, so your operations
are safe. But I can no longer promise what's specifically inside. You asked me to
widen to `Item`, so `Item` is all I'll guarantee."*

You gain the ability to mix types; you lose the right to assume a specific one.
Use `[B >: A]` when that trade-off is real, not as a precaution.

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

<details>
<summary><strong>Coming from Java?</strong></summary>

Java generics are invariant by default. To accept `List<Book>` where
`List<Item>` is expected, you must write `? extends` **at every call site**:

```java
{{#include ../../../examples/step2/Step2kCov.java}}
```

**Try it:** `javac Step2kCov.java && java Step2kCov`

This is *use-site* variance — if you forget `? extends`, the code silently
stops accepting subtypes. Every method that should be flexible needs the
annotation, and nothing warns you when you leave it out.

Scala's `List[+A]` declares covariance once at the class definition —
`cheapest(books)` just works everywhere, and you can't forget.

</details>

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

<details>
<summary><strong>Coming from Java?</strong></summary>

Java's `Predicate<Item>` can filter a `List<Book>` only because `filter()`
explicitly accepts `Predicate<? super T>` — use-site contravariance:

```java
{{#include ../../../examples/step2/Step2kCon.java}}
```

**Try it:** `javac Step2kCon.java && java Step2kCon`

If `filter()` had been written as `filter(Predicate<T>)` without `? super`,
passing a `Predicate<Item>` where `Predicate<Book>` is expected would fail —
and nothing warns you that the annotation is missing.

Scala's `Function1[-A, +B]` declares contravariance once —
`books.filter(cheap)` just works, and the caller never thinks about it.

</details>

### What about bounds for contravariance?

With covariance, the compiler said: *"You declared `+A`, so I can't let you take
an `A` as input."* Lower bounds (`[B >: A]`) were the escape hatch — widen upward.

The mirror exists: `-A` forbids `A` in output positions, and upper bounds
(`[B <: A]`) are the escape hatch — narrow downward. The pairings are fixed:

| Variance | Problem | Escape hatch | Direction |
|---|---|---|---|
| `+A` covariant | `A` can't appear in input | `[B >: A]` lower bound | widen up |
| `-A` contravariant | `A` can't appear in output | `[B <: A]` upper bound | narrow down |

Lower bounds solve a covariance problem. Upper bounds solve a contravariance
problem. They don't cross — covariance widens, so its escape hatch widens;
contravariance narrows, so its escape hatch narrows.

<details>
<summary><strong>What if you cross them? <code>[-A >: Item]</code></strong></summary>

You already know `PriceFormatter[-A]`. What if someone adds a lower bound?

```scala
{{#include ../../../examples/step2/step2crossed.scala}}
```

The bound `>: Item` prevents `PriceFormatter[Book]` from existing. But even
without the bound, could you use a `PriceFormatter[Book]` where
`PriceFormatter[Item]` is expected? No — contravariance flips the direction,
so `PriceFormatter[Book]` is a *supertype* of `PriceFormatter[Item]`.
The bound restricts at the definition site what contravariance already
handles at the use site. If you see `[-A >: X]` in code, it's a smell.

</details>

For simple consumers like `PriceFormatter[-A]` or `Function1[-A, +B]`, the
contravariant escape hatch never comes up — they return `String` or `Boolean`,
not `A`.

But it *does* come up when you compose contravariant types — and seeing it
in a real example is the best way to understand why the escape hatch exists.

### A real example: ZIO

[ZIO](https://zio.dev/), one of the most popular Scala libraries, takes an
interesting approach to dependency management: instead of passing dependencies
(a database connection, a logger) directly to functions, you describe what you
*need* in the type. A program becomes a value: *"I need a Database to run,
and I'll produce a String."* The compiler then verifies that all dependencies
are satisfied before the program runs — dependency injection at the type level.

ZIO encodes this as `ZIO[-R, +E, +A]`:

- **`-R`** (environment) — what the program *needs* to run. Contravariant,
  as we've been discussing.
- **`+E`** (error) — how the program can *fail*. Covariant — a function that
  handles `DatabaseError` also handles it when composed with code that fails
  with `NetworkError`, widening to `DatabaseError | NetworkError`.
- **`+A`** (value) — what the program *produces* on success. Covariant.

The typed error channel is a big part of what makes ZIO useful in production:
instead of catching `Exception` and hoping for the best, the compiler tells
you exactly which errors each operation can produce — and forces you to handle
them. Variance makes this composable: combining two operations that fail
differently gives you a union of their error types.

We'll build a simplified version — `Effect[-R, +A]` — dropping the error channel
to focus on variance and upper bounds. Our `Effect` is just a teaching tool to
see why the contravariant escape hatch is necessary: without `R1 <: R`, you
can't write `flatMap` — and without `flatMap`, you can't combine effects.

`Effect[-R, +A]` is a computation that needs an environment `R` to run and
produces a value `A`. Internally it wraps a function `R => A`:

```scala
class Effect[-R, +A](val run: R => A)
```

`R => A` is `Function1[R, A]`: `R` sits in the `-A` slot, `A` sits in the
`+B` slot. So `Effect[-R, +A]` has the same variance shape as
`Function1[-A, +B]` — which makes sense, since it's just a wrapper.

To see how `Effect[-R, +A]` works in practice, let's define two traits that
represent different capabilities, and an `AppEnv` that provides both:

```scala
trait Database:
  def lookup(id: Int): String

trait Logger:
  def log(msg: String): Unit

class AppEnv extends Database, Logger:
  def lookup(id: Int) = s"user-$id"
  def log(msg: String) = println(s"  LOG: $msg")
```

```
Database          Logger
    ↑                ↑
    └── AppEnv ──────┘
```

We want an effect that takes a `Database` and returns a `String` — so we
pass `db => db.lookup(1)`, and the type becomes `Effect[Database, String]`.
`logMsg` takes a `String` and returns an effect that needs a `Logger` —
it closes over the message:

```scala
val fetchUser: Effect[Database, String] = Effect(db => db.lookup(1))
val logMsg: String => Effect[Logger, Unit] = msg => Effect(logger => logger.log(msg))
```

Compose them with a for-comprehension, and the result is an effect that
requires `Database & Logger` — which we run by providing the environment:

```scala
val program: Effect[Database & Logger, Unit] =
  for
    user <- fetchUser
    _    <- logMsg(s"fetched $user")
  yield ()

program.run(AppEnv())  // LOG: fetched user-1
```

The for-comprehension desugars to `flatMap` and `map` — but defining
`flatMap` is where variance gets in the way.

### The problem: `flatMap` and `-R`

Intuitively, `flatMap` should use the
class's `R` — the same environment for both the current effect and the next one:

```scala
def flatMap[B](f: A => Effect[R, B]): Effect[R, B]
```

But the compiler rejects it. To see why, you need to know how the compiler
reads **positions**:

- Method parameters are **input** — position is `(-)`
- Return types are **output** — position is `(+)`
- When types are nested, positions multiply like signs:

```
(+) × (+) = (+)     output inside output → still output
(+) × (-) = (-)     output inside input → flips to input
(-) × (-) = (+)     input inside input → flips back to output
```

Desugar the signature: `flatMap[B](f: Function1[A, Effect[R, B]]): Effect[R, B]`

| Step | Where is R? | Position |
|------|-------------|----------|
| `f` is a method parameter | start | `(-)` |
| `Effect[R, B]` is in the `+B` slot of `Function1` | `(-) × (+)` | `(-)` |
| `R` is in the `-R` slot of `Effect` | `(-) × (-)` | `(+)` |

Result: `R` lands in `(+)`. We declared `-R`. **Rejected.**

The fix: introduce a fresh type parameter `R1 <: R` on the method.
Variance rules (`+` and `-`) only apply to the *class's* own type parameters.
`R1` is a *method* type parameter — the variance checker doesn't track it at
all. It can appear in any position. The only thing the compiler checks is that
`R1 <: R` holds at the call site.

```scala
def flatMap[R1 <: R, B](f: A => Effect[R1, B]): Effect[R1, B] =
    Effect(r => f(run(r)).run(r))
```

<details>
<summary><strong>Comparison: why does List's flatMap work without a bound?</strong></summary>

Desugar: `flatMap[B](f: Function1[A, IterableOnce[B]]): List[B]`

| Step | Where is A? | Position |
|------|-------------|----------|
| `f` is a method parameter | start | `(-)` |
| `A` is in the `-A` slot of `Function1` | `(-) × (-)` | `(+)` |

Result: `A` lands in `(+)`. We declared `+A`. **OK.** No extra nesting layer,
so no position flip — and no bound needed.

</details>

### How the implementation works

`R1 <: R` isn't just for the variance checker — it makes the implementation
work too. Let's break `Effect(r => f(run(r)).run(r))` into named steps,
writing `Function1[-A, +B]` explicitly so we can see where each type parameter lands:

```scala
//                               i.e. R => A
class Effect[-R, +A](val run: Function1[R, A]):
//                              i.e. A => Effect[R1, B]
  def flatMap[R1 <: R, B](f: Function1[A, Effect[R1, B]]): Effect[R1, B] =
    Effect { r =>              // r: R1 — the return type is Effect[R1, B]
      val a = run(r)           // run this effect — OK because R1 <: R
      val effect2 = f(a)       // build the next effect
      effect2.run(r)           // run it with the same environment
    }
```

Why is `r` an `R1`, not an `R`? Because `flatMap` returns `Effect[R1, B]`,
which wraps a function `R1 => B`. So `r` — the environment passed to
that function — has type `R1`. And since `R1 <: R`, the same `r` can feed
`run(r)` (which expects `R`) and `effect2.run(r)` (which expects `R1`).
This is the contravariant pattern: `run` expects an `R`, and `R1` has at
least everything `R` has — a consumer of a broader type works with a narrower one.

You can run the complete example with `scala-cli run step2g2.scala`:

```scala
{{#include ../../../examples/step2/step2g2.scala}}
```

## 2-8. Quick Reference

That was a lot of moving parts. Here's everything from this chapter in one place:

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
