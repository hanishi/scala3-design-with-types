// step2i.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double, val isbn: String) extends Item
class DVD(val name: String, val price: Double) extends Item

// JSON serializer — consumes a value and produces a String
trait JsonWriter[-A]:
  def write(value: A): String

class ItemWriter extends JsonWriter[Item]:
  def write(value: Item): String = s"""{"name":"${value.name}","price":${value.price}}"""

class BookWriter extends JsonWriter[Book]:
  def write(value: Book): String = s"""{"name":"${value.name}","price":${value.price},"isbn":"${value.isbn}"}"""

@main def step2i(): Unit =
  val itemWriter: JsonWriter[Item] = ItemWriter()

  // An ItemWriter can write Books too — it handles anything that's an Item
  val bookWriter: JsonWriter[Book] = itemWriter  // Contravariance!
  println(bookWriter.write(Book("Scala in Depth", 45.0, "978-1617295")))
  // prints: {"name":"Scala in Depth","price":45.0} — no isbn, same as PriceFormatter

  // A BookWriter can NOT write arbitrary Items — it expects Book-specific fields
  val specificWriter: JsonWriter[Book] = BookWriter()
  // val generalWriter: JsonWriter[Item] = specificWriter  // REJECTED
  // What would specificWriter.write(DVD("The Matrix", 19.99)) do with isbn?
