# Step 2: Variance — Earning the Compiler's Trust

Variance is not a set of rules to memorize.
It's something you experience by asking "why won't this compile?" alongside the compiler.
The starting point is **invariant** (no compatibility). The fact that this is the default matters.

## 2-1. Invariant — The Default Wall

```scala
// step2a.scala

class Product(val name: String)
class Book(name: String, val isbn: String) extends Product(name)

// Define our own Box. The type parameter is [A] — invariant.
class Box[A](val value: A)

@main def step2a(): Unit =
  val bookBox: Box[Book] = Box(Book("Scala in Depth", "978-1617295"))

  // Book is a subtype of Product. So is Box[Book] a subtype of Box[Product]?
  // val productBox: Box[Product] = bookBox
  // error: Found Box[Book], Required Box[Product]

  println("Box[Book] is NOT Box[Product]")
```

**Uncomment and compile.**

The compiler treats `Box[Book]` and `Box[Product]` as **completely different types**.
Just because `Book <: Product` doesn't mean `Box[Book] <: Box[Product]`.
The compiler's default: *"Show me it's safe, and I'll allow it."*

## 2-2. Why Invariant Is the Default — Break It to Understand

```scala
// step2b.scala

class Product(val name: String)
class Book(name: String) extends Product(name)
class Electronics(name: String) extends Product(name)

// What if Box were covariant? Let's think about a mutable box.
class MutBox[A](var value: A)

@main def step2b(): Unit =
  val bookBox: MutBox[Book] = MutBox(Book("Scala in Depth"))

  // If this were allowed... (it's actually a compile error)
  // val productBox: MutBox[Product] = bookBox
  // productBox.value = Electronics("Keyboard")  // Product allows Electronics...
  // val book: Book = bookBox.value  // ...but now a Keyboard comes out as a Book!

  println("That's why a mutable box must be invariant")
```

This is a thought experiment, but in Java **it actually happens**:

```java
// Java — this compiles!
Book[] books = { new Book("Scala") };
Product[] products = books;        // Java arrays are covariant
products[0] = new Electronics("Keyboard");  // ArrayStoreException at runtime!
```

Scala's invariant default is a design decision to **eliminate this class of bugs at compile time**.

## 2-3. Making It Covariant — What `+A` Means

```scala
// step2c.scala

class Product(val name: String):
  override def toString = s"Product($name)"
class Book(name: String) extends Product(name):
  override def toString = s"Book($name)"

// A read-only box. The + declares it covariant.
class ReadBox[+A](val value: A)

@main def step2c(): Unit =
  val bookBox: ReadBox[Book] = ReadBox(Book("Scala in Depth"))

  // With +A, this compiles!
  val productBox: ReadBox[Product] = bookBox
  println(productBox.value)  // Book(Scala in Depth)

  // ReadBox only has a val (read-only).
  // Reading a Book as a Product is always safe.
```

`+A` declares: *"This type only **outputs** A, so it's safe to widen in the subtype direction."*
The compiler **verifies** this claim — it checks that you're only using `A` in output positions.

> **Checkpoint:** You now know the core idea — `+A` means "this type only produces `A`."
> The next sections show what happens when you push the boundaries.

## 2-4. Testing the Compiler's Checks

```scala
// step2d.scala

class Product(val name: String)
class Book(name: String) extends Product(name)
class Electronics(name: String) extends Product(name)

// Try +A with a var → the compiler catches the mismatch
// class MutBox[+A](var value: A)
// error: covariant type A occurs in contravariant position
//        in type A of parameter value_=

// OK, only a val. But add a setter method?
class Box[+A](val value: A):
  // def set(a: A): Box[A] = Box(a)
  // error: covariant type A occurs in contravariant position
  //        in type A of parameter a

  override def toString = s"Box($value)"

@main def step2d(): Unit =
  println("Uncomment each block and read the error messages")
```

**This is the heart of the matter.** Uncomment each block one at a time.

The compiler says "`A` occurs in contravariant position."
A method parameter is an **input** — the consuming side — a contravariant position.
You declared `+A` (safe as output), but you're putting it in an input position. Contradiction.

**Dig deeper:**
```
> scala-cli compile step2d.scala -- -explain
```
