// step2crossed.scala — Why [-A >: X] is a code smell
//
// The pairings are fixed: +A pairs with >: (lower bound),
// -A pairs with <: (upper bound). What if you cross them?

// --- Domain (same as earlier) ---
trait Item:
  def name: String
  def price: Double

case class Book(name: String, price: Double) extends Item
case class DVD(name: String, price: Double) extends Item

// --- Contravariance + lower bound: [-A >: Item] ---
trait Sink[-A >: Item]:
  def accept(a: A): Unit

// A = Item — works (Item >: Item)
val itemSink: Sink[Item] = a => println(s"Got item: ${a.name}")

// A = Any — works (Any >: Item)
val anySink: Sink[Any] = a => println(s"Got anything: $a")

// A = Book — compile error! Book is narrower than Item
// val bookSink: Sink[Book] = b => println(b.name)  // blocked by >: Item

// --- But remove the bound and compare ---
trait SinkNoBound[-A]:
  def accept(a: A): Unit

// Now Book compiles...
val bookSink: SinkNoBound[Book] = b => println(b.name)

// ...but can you use it where SinkNoBound[Item] is expected?
def process(items: List[Item], sink: SinkNoBound[Item]): Unit =
  items.foreach(sink.accept)

// process(List(Book("Scala", 45.0)), bookSink)  // won't compile!
// SinkNoBound[Book] is a SUPERTYPE of SinkNoBound[Item], not a subtype.
// Contravariance flips the direction: Item <: Any, so Sink[Any] <: Sink[Item].
// A Book-only sink can't safely handle arbitrary Items.

// --- Conclusion ---
// The bound [-A >: Item] prevents Sink[Book] from existing.
// Without the bound, Sink[Book] exists but can't be used anywhere useful.
// The bound is redundant safety — contravariance already handles it.
// Don't cross the pairings. The complexity isn't earning its keep.

@main def step2crossed(): Unit =
  val sink: SinkNoBound[Item] = a => println(s"Got item: ${a.name}")
  process(List(Book("Scala", 45.0), DVD("The Matrix", 19.99)), sink)
