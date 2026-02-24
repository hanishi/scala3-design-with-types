# Step 2: Bounds and Contravariance

## 2-5. Lower Bounds — Escaping the Variance Constraint

This is where bounds enter the picture. A bound is not a separate concept from variance —
it's **the tool for adding methods to covariant types**.

```scala
// step2e.scala

class Product(val name: String):
  override def toString = s"Product($name)"
class Book(name: String) extends Product(name):
  override def toString = s"Book($name)"
class Electronics(name: String) extends Product(name):
  override def toString = s"Electronics($name)"

class Box[+A](val value: A):
  // Can't use A directly as a parameter. But we can accept "A or wider."
  def set[B >: A](b: B): Box[B] = Box(b)

  override def toString = s"Box($value)"

@main def step2e(): Unit =
  val bookBox: Box[Book] = Box(Book("Scala in Depth"))
  println(bookBox)                    // Box(Book(Scala in Depth))

  // Put a Book in → stays Box[Book]
  val bookBox2: Box[Book] = bookBox.set(Book("FP in Scala"))
  println(bookBox2)                   // Box(Book(FP in Scala))

  // Put Electronics in → widens to Box[Product] (common parent of Book and Electronics)
  val productBox: Box[Product] = bookBox.set(Electronics("Keyboard"))
  println(productBox)                 // Box(Electronics(Keyboard))

  // The type widened automatically in the safe direction.
  // Box[Book].set(Electronics) → Box[Product]
  // Nobody gets deceived. The widening is expressed in the type.
```

`[B >: A]` means "B is a supertype of A (A or wider)."
This is exactly what `List[+A]`'s `prepended` does:

```scala
// Simplified definition of List
sealed abstract class List[+A]:
  def prepended[B >: A](elem: B): List[B]
```

Prepend an `Electronics` to a `List[Book]` and you get `List[Product]`. The types don't lie.

## 2-6. Upper Bounds — Requiring Minimum Capabilities

If lower bounds **widen**, upper bounds **narrow**.

```scala
// step2f.scala

// E-commerce example
trait Product:
  def name: String
  def price: Double

trait Shippable extends Product:
  def weight: Double
  def shippingCost: Double = weight * 0.5

case class Book(name: String, price: Double, weight: Double) extends Shippable
case class Laptop(name: String, price: Double, weight: Double) extends Shippable
case class GiftCard(name: String, price: Double) extends Product  // Not shippable

// Only Shippable products can be sorted by shipping cost
def sortByShipping[A <: Shippable](products: List[A]): List[A] =
  products.sortBy(_.shippingCost)

@main def step2f(): Unit =
  val items = List(
    Book("Scala in Depth", 45.0, 0.8),
    Book("FP Simplified", 35.0, 1.2),
  )
  sortByShipping(items).foreach(p => println(s"${p.name}: shipping=${p.shippingCost}"))

  // Try sorting GiftCards?
  // val cards = List(GiftCard("Amazon $50", 50.0))
  // sortByShipping(cards)  // Compile error! GiftCard is not Shippable
```

`<:` means "this type must have at least this capability."
Not an `instanceof` check at runtime — rejected at compile time.

> **You now have the full toolkit for the output side:** covariance (`+A`) for safe widening,
> lower bounds (`>:`) to add methods without breaking covariance, and upper bounds (`<:`)
> to require capabilities. Next up: the input side.

## 2-7. Contravariance — Consumer Compatibility

Finally, `-A`. This is compatibility for the **input** side.

```scala
// step2g.scala

class Product(val name: String):
  override def toString = s"Product($name)"
class Book(name: String, val isbn: String) extends Product(name):
  override def toString = s"Book($name)"

// PriceFormatter[-A]: the - declares it contravariant
trait PriceFormatter[-A]:
  def format(item: A): String

class ProductFormatter extends PriceFormatter[Product]:
  def format(item: Product): String = s"${item.name}: (price on request)"

class BookFormatter extends PriceFormatter[Book]:
  def format(item: Book): String = s"${item.name} (ISBN: ${item.isbn}): (price on request)"

@main def step2g(): Unit =
  val productFormatter: PriceFormatter[Product] = ProductFormatter()

  // If it can format any Product, it can format a Book
  // → PriceFormatter[Product] is usable as PriceFormatter[Book]
  val formatterForBook: PriceFormatter[Book] = productFormatter  // OK!
  println(formatterForBook.format(Book("Scala in Depth", "978-1617295")))

  // The reverse doesn't work: a Book-only formatter can't handle all Products
  val bookFormatter: PriceFormatter[Book] = BookFormatter()
  // val formatterForProduct: PriceFormatter[Product] = bookFormatter  // Compile error!
```

## 2-8. Putting It All Together

Variance and bounds are not separate topics. They're one story:

```
1. Invariant is the default     → The compiler starts cautious — no compatibility assumed
2. +A (covariant)               → You declare "output only, safe to widen"
3. Compiler verifies             → Checks that A only appears in output positions
4. >: (lower bound)             → The tool to safely add methods to covariant types
5. <: (upper bound)             → The tool to require minimum capabilities
6. -A (contravariant)           → "Input only, safe in the opposite direction"
```

Everything starts from "invariant = no compatibility assumed."
You open it up only where you can show it's safe. It's a conversation with the compiler.

But knowing the mechanics is only half the story. The next question is:
*in practice, how often do you actually want invariant, and when should you reach for `+` or `-`?*

> **Take a breath.** The hard conceptual work is done. Sections 2-9 through 2-11
> show how variance plays out in real code — these are shorter and more concrete.
