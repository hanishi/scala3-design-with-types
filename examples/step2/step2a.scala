// step2a.scala

class Product(val name: String)
class Book(name: String, val isbn: String) extends Product(name)

// Define our own Box. The type parameter is [A] — invariant.
class Box[A](val value: A)

@main def step2a(): Unit =
  val bookBox: Box[Book] = Box(Book("Scala in Depth", "978-1617295"))

  // Book is a subtype of Product. So is Box[Book] a subtype of Box[Product]?
  // val productBox: Box[Product] = bookBox
  // error: Found Box[Book], Required Box[Product]

  println("Box[Book] is NOT Box[Product]")
