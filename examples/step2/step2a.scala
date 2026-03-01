// step2a.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double) extends Item:
  override def toString = s"Book($name, $$${price})"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name, $$${price})"

// Define our own Box. The type parameter is [A] — invariant.
class Box[A](val value: A)

@main def step2a(): Unit =
  val bookBox: Box[Book] = Box(Book("Scala in Depth", 45.0))

  // Book is a subtype of Item. So is Box[Book] a subtype of Box[Item]?
  // val itemBox: Box[Item] = bookBox
  // [error] Found:    (bookBox : Box[Book])
  // [error] Required: Box[Item]
  // [error]   val itemBox: Box[Item] = bookBox
  // [error]                            ^^^^^^^

  println("Box[Book] is NOT Box[Item]")
