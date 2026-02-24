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
