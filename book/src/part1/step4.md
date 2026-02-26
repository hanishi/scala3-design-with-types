# Step 4: Type Members — Types Inside Types

Type parameters are great when the *caller* decides the type: `List[Int]`, `Box[String]`.
But sometimes the *implementation* should decide — and the outside world shouldn't even
need to know. That's what type members are for.

Scala offers this second mechanism — **type members**, types declared *inside* a trait or class.
This is unusual among mainstream languages and unlocks patterns that type parameters can't express.

## 4-1. Type Members vs Type Parameters

```scala
{{#include ../../../examples/step4/step4a.scala}}
```

The key difference: with a type parameter, the type appears on the outside (`ContainerParam[Int]`).
With a type member, the type is **inside the object** — you access it through the value itself.

## 4-2. Path-Dependent Types — Types That Belong to Values

```scala
{{#include ../../../examples/step4/step4b.scala}}
```

**Uncomment and read the error.** The compiler says `sqlite.Row` is not `postgres.Row`.
Even though both are type members called `Row`, they belong to different *values*.
`postgres.Row` and `sqlite.Row` are path-dependent types — the path through the value matters.

**This is something type parameters can't do.** With `Database[Row]`, nothing would prevent
you from passing a PostgreSQL row to a SQLite processor. With type members, the compiler
links the row type to the specific database *instance*.

## 4-3. Abstract Type Members — Deferring Type Decisions

```scala
{{#include ../../../examples/step4/step4c.scala}}
```

This pattern is powerful for **information hiding**. The `Pipeline` trait exposes
`Input` and `Output` but the `Intermediate` type is an implementation detail.
With type parameters, you'd need `Pipeline[Input, Output, Intermediate]` —
leaking a type that callers shouldn't have to know about.

## 4-4. When to Use Type Members vs Type Parameters

```scala
{{#include ../../../examples/step4/step4d.scala}}
```

## 4-5. Connection to Opaque Types

You've already used type members without realizing it.
`opaque type USD = Double` inside an `object` is a type member whose underlying
type is hidden from outside. The same information-hiding principle applies:
the outside world sees `Currency.USD` but can't see that it's `Double`.

Opaque types are essentially type members with compiler-enforced opacity.
This is why they must be defined inside an `object` or `class` — they need
an enclosing scope to define the boundary of visibility.
