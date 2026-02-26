# Step 0: Life Without Types

Let's feel what happens when type information is lost.

## 0-1. The Anything-Goes List

```scala
{{#include ../../../examples/step0/step0.scala}}
```

**Try it:**
```
> scala-cli run step0.scala
```

Uncomment `first * 2` and compile. Read the error.
The compiler says `Any` has no `*` method.

**The insight:** The value *is* 100 at runtime, but the compiler doesn't know that.
The moment type information is lost, the compiler can no longer protect you.
