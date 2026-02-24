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
