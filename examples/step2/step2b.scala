// step2b.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double) extends Item:
  override def toString = s"Book($name, $$${price})"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name, $$${price})"

// What if Box[Book] could be used as Box[Item]?
// Note: var means the value can be reassigned — this box is mutable (read AND write).
class Box[A](var value: A)

@main def step2b(): Unit =
  val bookBox: Box[Book] = Box(Book("Scala in Depth", 45.0))

  // Try uncommenting the next line and compile:
  // val itemBox: Box[Item] = bookBox
  // [error] Found:    (bookBox : Box[Book])
  // [error] Required: Box[Item]
  // [error]   val itemBox: Box[Item] = bookBox
  // [error]                            ^^^^^^^
  //
  // The compiler rejects this. But imagine if it didn't — what could go wrong?
  // itemBox.value = DVD("The Matrix", 19.99)  // Item allows DVD...
  // val book: Book = bookBox.value  // ...but now a DVD comes out as a Book!

  println("The compiler prevents this — a mutable box must be invariant")
