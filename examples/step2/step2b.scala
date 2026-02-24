// step2b.scala

class Product(val name: String)
class Book(name: String) extends Product(name)
class Electronics(name: String) extends Product(name)

// What if Box were covariant? Let's think about a mutable box.
class MutBox[A](var value: A)

@main def step2b(): Unit =
  val bookBox: MutBox[Book] = MutBox(Book("Scala in Depth"))

  // If this were allowed... (it's actually a compile error)
  // val productBox: MutBox[Product] = bookBox
  // productBox.value = Electronics("Keyboard")  // Product allows Electronics...
  // val book: Book = bookBox.value  // ...but now a Keyboard comes out as a Book!

  println("That's why a mutable box must be invariant")
