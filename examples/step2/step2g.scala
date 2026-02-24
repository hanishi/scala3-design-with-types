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
