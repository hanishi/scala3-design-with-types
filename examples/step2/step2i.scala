// step2i.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double, val isbn: String) extends Item:
  override def toString = s"Book($name, $price, $isbn)"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name, $price)"

// JSON serializer — consumes a value and produces a String
trait JsonWriter[-A]:
  def write(value: A): String

class ItemWriter extends JsonWriter[Item]:
  def write(value: Item): String =
    s"""{"name":"${value.name}","price":${value.price}}"""

class BookWriter extends JsonWriter[Book]:
  def write(value: Book): String =
    s"""{"name":"${value.name}","price":${value.price},""" +
    s""""isbn":"${value.isbn}"}"""

// Validator — consumes a value and returns pass/fail
trait Validator[-A]:
  def validate(value: A): Boolean

class PriceValidator extends Validator[Item]:
  def validate(value: Item): Boolean = value.price > 0

def serialize[A](value: A, writer: JsonWriter[A]): String =
  writer.write(value)

@main def step2i(): Unit =
  val itemWriter = ItemWriter()
  val book = Book("Scala in Depth", 45.0, "978-1617295")

  // serialize expects JsonWriter[Book] — ItemWriter handles any Item, so it qualifies
  println(serialize(book, itemWriter))
  // {"name":"Scala in Depth","price":45.0} — no isbn, but it works

  // A BookWriter knows about Book-specific fields
  val bookWriter = BookWriter()
  println(serialize(book, bookWriter))
  // {"name":"Scala in Depth","price":45.0,"isbn":"978-1617295"}

  // The reverse doesn't work: a BookWriter can't handle arbitrary Items
  // serialize(DVD("The Matrix", 19.99), bookWriter)  // Compile error
  // What would bookWriter do with a DVD's isbn?

  // Same pattern with Validator: PriceValidator validates any Item,
  // so it works on Books too
  val books = List(Book("Scala", 45.0, "978-1"), Book("Free", 0.0, "978-2"))
  val valid = books.filter(PriceValidator().validate)
  println(valid)  // List(Book(Scala, 45.0, 978-1))
