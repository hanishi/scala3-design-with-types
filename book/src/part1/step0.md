# Step 0: Life Without Types

Let's feel what happens when type information is lost.

## 0-1. The Anything-Goes List

```scala
// step0.scala
@main def step0(): Unit =
  // In Scala, you can't omit type parameters.
  // But you can use Any to make a list that holds everything.
  val xs: List[Any] = List(100, "hello", true, 3.14)

  // What do you get when you pull something out?
  val first: Any = xs.head  // It's 100... but the type is Any

  // Try to do arithmetic — compile error
  // val doubled = first * 2
  // error: value * is not a member of Any

  println(first)            // prints 100
  println(first.getClass)   // class java.lang.Integer
```

**Try it:**
```
> scala-cli run step0.scala
```

Uncomment `first * 2` and compile. Read the error.
The compiler says `Any` has no `*` method.

**The insight:** The value *is* 100 at runtime, but the compiler doesn't know that.
The moment type information is lost, the compiler can no longer protect you.
