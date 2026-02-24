# Step 2: Variance in Practice

## 2-9. Covariance in Practice — Most Types Are Producers

In real code, the majority of your custom types are **producers**: they hold data and you read from them.
This is why covariance shows up so often.

```scala
// step2h.scala

// An event hierarchy — very common in event-driven systems
sealed trait OrderEvent
case class OrderPlaced(orderId: String, timestamp: Long) extends OrderEvent
case class PaymentReceived(orderId: String, timestamp: Long, amount: Double) extends OrderEvent
case class ItemShipped(orderId: String, timestamp: Long, trackingId: String) extends OrderEvent

// An event stream. Should EventStream[PaymentReceived] be usable as EventStream[OrderEvent]?
// YES — if you're only reading events out, a stream of payments IS a stream of order events.

// Invariant version — causes friction
class EventStream[A](val events: List[A]):
  def latest: Option[A] = events.headOption

// Covariant version — works naturally
class EventStreamCov[+A](val events: List[A]):
  def latest: Option[A] = events.headOption

@main def step2h(): Unit =
  val payments = EventStreamCov(List(
    PaymentReceived("ord1", 1000L, 99.99),
    PaymentReceived("ord2", 2000L, 49.50),
  ))

  // This just works. A stream of payments IS a stream of order events.
  val events: EventStreamCov[OrderEvent] = payments
  println(events.latest)

  // With the invariant version, you'd have to write:
  val paymentsInv = EventStream(List(PaymentReceived("ord1", 1000L, 99.99)))
  // val eventsInv: EventStream[OrderEvent] = paymentsInv  // REJECTED
  // You'd be forced to create a new EventStream[OrderEvent] by copying.
```

This pattern appears everywhere:
- `Option[+A]` — `Some[Book]` is usable as `Option[Product]`
- `List[+A]` — a list of specific items is a list of general items
- `Future[+A]` — a future producing a `Book` is a future producing a `Product`
- `Either[+A, +B]` — results carry their types upward

If your type only **produces** (returns, holds, emits) values of type `A`,
it should almost certainly be covariant.

## 2-10. Contravariance in Practice — Handlers, Validators, Serializers

Contravariance is less common but appears in a very specific pattern:
types that **consume** or **process** values.

```scala
// step2i.scala

class Product(val name: String, val price: Double)
class Book(name: String, price: Double, val isbn: String) extends Product(name, price)

// JSON serializer — consumes a value and produces a String
trait JsonWriter[-A]:
  def write(value: A): String

class ProductWriter extends JsonWriter[Product]:
  def write(value: Product): String = s"""{"name":"${value.name}","price":${value.price}}"""

class BookWriter extends JsonWriter[Book]:
  def write(value: Book): String = s"""{"name":"${value.name}","price":${value.price},"isbn":"${value.isbn}"}"""

@main def step2i(): Unit =
  val productWriter: JsonWriter[Product] = ProductWriter()

  // A ProductWriter can write Books too — it handles anything that's a Product
  val bookWriter: JsonWriter[Book] = productWriter  // Contravariance!
  println(bookWriter.write(Book("Scala in Depth", 45.0, "978-1617295")))

  // A BookWriter can NOT write arbitrary Products — it expects Book-specific fields
  val specificWriter: JsonWriter[Book] = BookWriter()
  // val generalWriter: JsonWriter[Product] = specificWriter  // REJECTED

// The pattern: "if you can handle the general case, you can handle the specific case"
```

Other real-world contravariant types:
- `Ordering[-A]` — an ordering for `Product` can sort `Book`s
- `Function1[-A, +B]` — a function accepting `Product` can be called with `Book`
- Validators, event handlers, comparators — anything that **receives and processes**

## 2-11. So Is Invariant Actually Useful?

Yes, but its role is narrower than you might think.

Invariant is the right choice when your type **both reads and writes** — mutable containers,
bidirectional channels, read-write references. But in well-designed Scala code,
these are relatively rare because immutability is preferred.

```scala
// step2j.scala

class Product(val name: String):
  override def toString = s"Product($name)"

// Invariant is correct here: a mutable cell reads AND writes
class Cell[A](var value: A):
  def get: A = value            // output position (+)
  def set(a: A): Unit =         // input position (-)
    value = a

// A bidirectional codec: encodes AND decodes
trait Codec[A]:
  def encode(value: A): String  // A in input position (-)
  def decode(raw: String): A    // A in output position (+)

// Both positions → must be invariant. The compiler enforces this.
// Try adding + or - and see what happens:
// trait Codec[+A]:  // error on encode
// trait Codec[-A]:  // error on decode

@main def step2j(): Unit =
  val cell = Cell[Product](Product("Keyboard"))
  cell.set(Product("Mouse"))
  println(cell.get)  // Product(Mouse)

  // Cell[Book] is NOT Cell[Product] — and shouldn't be.
  // If it were, you could put Electronics in a Book cell.
```

**The practical reality:**

Most types you design in Scala fall into one of two camps:

```
Produces A (read-only data, results, events, streams)  → make it covariant [+A]
Consumes A (handlers, writers, validators, orderings)   → make it contravariant [-A]
```

Invariant is what you get when you **don't annotate** — and often that's because
you haven't yet thought about whether your type is a producer or consumer.
When you do think about it, you'll find that one of `+` or `-` usually applies.

The discipline of asking "does this type produce or consume `A`?" is itself
a valuable design exercise. It forces you to clarify your type's role,
and the compiler verifies your answer.
