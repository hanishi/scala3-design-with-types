// step2g1.scala — Contravariance in Function1

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double, val isbn: String) extends Item:
  override def toString = s"Book($name, $$${price})"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name, $$${price})"

@main def step2g1(): Unit =
  val books = List(Book("Scala", 45.0, "978-1"), Book("FP", 35.0, "978-2"))
  // These functions accept any Item.
  // Item => Boolean is shorthand for Function1[Item, Boolean]  (-A = Item, +B = Boolean)
  val cheap: Item => Boolean = _.price < 40.0
  val priceOf: Item => Double = _.price
  // List[Book].filter signature:  filter(p: Book => Boolean): List[Book]
  // Desugar the arrow:            filter(p: Function1[Book, Boolean]): List[Book]
  //
  // You pass cheap, which is Function1[Item, Boolean] — not Function1[Book, Boolean].
  // Why does this compile? Function1[-A, +B] is contravariant in A:
  //   Function1[Item, Boolean] <: Function1[Book, Boolean]
  // An Item function only reads name and price — a Book has both, so it's safe.
  println(books.filter(cheap))       // List(Book(FP, $35.0))
  println(books.sortBy(priceOf))     // List(Book(FP, $35.0), Book(Scala, $45.0))

  // The payoff: write once against Item, reuse with any subtype collection
  val dvds = List(DVD("The Matrix", 19.99), DVD("Inception", 24.99))
  val items: List[Item] = books ++ dvds

  println(dvds.filter(cheap))        // List(DVD(The Matrix, $19.99), DVD(Inception, $24.99))
  println(items.sortBy(priceOf))     // all four, sorted by price

  // The reverse doesn't work: a Book-only function can't handle DVDs
  val isbnOf: Book => String = _.isbn
  // dvds.map(isbnOf)   // Compile error! DVD has no isbn
  // items.map(isbnOf)  // Compile error! Item has no isbn
