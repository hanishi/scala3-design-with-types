// step2c.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double) extends Item:
  override def toString = s"Book($name, $$${price})"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name, $$${price})"

// Same Box, but now with +A — covariant.
class Box[+A](val value: A)

@main def step2c(): Unit =
  val bookBox: Box[Book] = Box(Book("Scala in Depth", 45.0))

  // In 2-1, Box[A] rejected this. The + is what makes the difference.
  // Book is an Item, so Box[Book] can now be used as Box[Item].
  val itemBox: Box[Item] = bookBox
  println(itemBox.value)  // Book(Scala in Depth, $45.0)
