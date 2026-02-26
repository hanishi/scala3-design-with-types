# Step 1: Type Parameters Are Contracts

## 1-1. Build Your Own Box

```scala
{{#include ../../../examples/step1/step1a.scala}}
```

**Try it:**
```
> scala-cli run step1a.scala
```

Then uncomment `val s: String = intBox.value`.

**The insight:** The `A` in `Box[A]` doesn't "remember" what you put in.
The compiler **commits** to `A = Int` at construction time. That's a contract.
When you take the value out, it's guaranteed to be `Int`.

## 1-2. Type Parameters in Functions — Contracts Propagate

```scala
{{#include ../../../examples/step1/step1b.scala}}
```

**Inspect the types:**
```sh
scala-cli run step1b.scala

# To see the inferred type (clean first to bypass compilation cache):
scala-cli clean step1b.scala && scala-cli compile step1b.scala -O -Xprint:typer 2>&1 | grep "mixed"
```

`mixed` is inferred as `List[Int | String | Double]` — a union type. This is a Scala 3 thing.

Notice that you never wrote `List[Int | String | Double]` — the compiler **inferred** it.
Throughout this book, we rely on the compiler's ability to figure out type parameters
from context. How type inference works internally is a topic of its own; here we just
trust that it does, and focus on what the types *mean*.
