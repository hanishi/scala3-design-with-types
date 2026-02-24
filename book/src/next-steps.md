# Where to Go Next

This tutorial covered the foundations. Here's what builds on them:

**Immediate next steps** (you have the tools now):
- **Type lambdas** (`[X] =>> F[X, Int]`) — partially applying type constructors, needed for working with higher-kinded types
- **Recursive match types** — type-level computation that isn't just a lookup table but an actual algorithm (e.g., computing tuple element types, type-level arithmetic)
- **Mirrors and type class derivation** — automatically generating typeclass instances (JSON codecs, Show, Eq) for case classes and enums, eliminating the boilerplate from [Step 5](./part1/step5.md)

**Deeper explorations:**
- **Phantom types** — type parameters that exist only at compile time and carry zero runtime data (extending the opaque type pattern from [Step 3](./part1/step3.md))
- **Dependent function types** (`(x: Foo) => x.Bar`) — Scala 3's dependent functions extend path-dependent types ([Step 4](./part1/step4.md)) into first-class function signatures
- **Refined types** (via libraries like iron) — attaching compile-time constraints to types, extending the inline validation from [Step 7](./part2/step7.md) into the type system itself (e.g., `Quantity` that is always positive, without the `inline` limitation)

**Recommended reading:**
- *Programming in Scala, 5th Edition* (Odersky et al.) — Chapters 18–23 cover the same terrain with more depth; Chapter 20 on abstract members complements [Step 4](./part1/step4.md)
- The Scala 3 Reference at [docs.scala-lang.org](https://docs.scala-lang.org) — especially the sections on match types and type lambdas
- Scala 3 compiler source code — the ultimate reference for how type-level features actually work
