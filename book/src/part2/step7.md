# Step 7: Inline and Compile-Time Validation

Runtime validation catches bad values when your program runs — an `IllegalArgumentException`
at 3 AM in production. What if the compiler could catch them before it even compiles?

`inline` makes the compiler expand code at the call site. Combined with
`scala.compiletime`, it lets you **reject invalid code at compile time**
with custom error messages.

## 7-1. compiletime.error — Custom Compile Errors

```scala
{{#include ../../../examples/step7/step7a.scala}}
```

**Uncomment and compile.** The error message is yours, not the compiler's.
This is compile-time validation: bugs that would be `IllegalArgumentException` at
runtime become compile errors.

The key constraint: the value must be known at compile time (a literal or another inline value).
For runtime values, you still need runtime validation.

## 7-2. Practical Use: Compile-Time Validated Domain Types

Remember `Currency.USD` from [Step 3](../part1/step3.md)? Opaque types prevented mixing dollars and yen.
Now imagine going further: ensuring the *amount* itself is valid before the code compiles.

```scala
{{#include ../../../examples/step7/step7b.scala}}
```

This combines opaque types ([Step 3](../part1/step3.md)) with inline validation:
the type system ensures `Quantity` and `DiscountPct` can't be confused with plain `Int`,
and `inline` ensures only valid values can be created.

## 7-3. constValue — Reading Types as Values

```scala
{{#include ../../../examples/step7/step7c.scala}}
```

Singleton types and `constValue` are how Scala 3 connects compile-time computation
to runtime values. Libraries like circe and tapir use these techniques to generate
codecs and API documentation from type information alone.
