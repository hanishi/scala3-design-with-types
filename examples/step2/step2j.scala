// step2j.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double, val isbn: String) extends Item:
  override def toString = s"Book($name)"

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
  val cell = Cell[Item](Book("Scala", 45.0, "978-1"))
  cell.set(Book("FP", 35.0, "978-2"))
  println(cell.get)  // Book(FP)

  // Cell[Book] is NOT Cell[Item] — and shouldn't be.
  // If it were, you could put a DVD in a Book cell.
