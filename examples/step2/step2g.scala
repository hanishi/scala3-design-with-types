// step2g.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double, val isbn: String) extends Item:
  override def toString = s"Book($name)"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name)"

// PriceFormatter[-A]: the - declares it contravariant.
// format CONSUMES an A — it takes it in and reads from it.
trait PriceFormatter[-A]:
  def format(item: A): String

// Can format ANY Item — only reads name and price
class ItemFormatter extends PriceFormatter[Item]:
  def format(item: Item): String = s"${item.name}: $$${item.price}"

// Can only format Books — reads isbn, which only Book has
class BookFormatter extends PriceFormatter[Book]:
  def format(item: Book): String = s"${item.name} (ISBN: ${item.isbn}): $$${item.price}"

@main def step2g(): Unit =
  val book = Book("Scala in Depth", 45.0, "978-1617295")

  // ItemFormatter consumes a Book by reading name and price.
  // It doesn't know about isbn — but that's fine, it never asks for it.
  // Safe: Book has everything Item has.
  val formatterForBook: PriceFormatter[Book] = ItemFormatter()
  println(formatterForBook.format(book))
  // prints: Scala in Depth: $45.0 — isbn is there, but the formatter never reads it

  // BookFormatter consumes a Book by reading name, price, AND isbn.
  val bookFormatter: PriceFormatter[Book] = BookFormatter()
  println(bookFormatter.format(book))
  // prints: Scala in Depth (ISBN: 978-1617295): $45.0

  // Now imagine the reverse: using BookFormatter as PriceFormatter[Item].
  // val formatterForItem: PriceFormatter[Item] = bookFormatter  // Compile error!
  //
  // If this compiled, you could write:
  //   formatterForItem.format(DVD("The Matrix", 19.99))
  //
  // BookFormatter.format reads item.isbn — but DVD has no isbn.
  // The compiler catches this: a Book consumer can't safely consume all Items.
