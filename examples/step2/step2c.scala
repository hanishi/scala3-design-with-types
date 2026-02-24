// step2c.scala

class Product(val name: String):
  override def toString = s"Product($name)"
class Book(name: String) extends Product(name):
  override def toString = s"Book($name)"

// A read-only box. The + declares it covariant.
class ReadBox[+A](val value: A)

@main def step2c(): Unit =
  val bookBox: ReadBox[Book] = ReadBox(Book("Scala in Depth"))

  // With +A, this compiles!
  val productBox: ReadBox[Product] = bookBox
  println(productBox.value)  // Book(Scala in Depth)

  // ReadBox only has a val (read-only).
  // Reading a Book as a Product is always safe.
