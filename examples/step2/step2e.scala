// step2e.scala

class Product(val name: String):
  override def toString = s"Product($name)"
class Book(name: String) extends Product(name):
  override def toString = s"Book($name)"
class Electronics(name: String) extends Product(name):
  override def toString = s"Electronics($name)"

class Box[+A](val value: A):
  // Can't use A directly as a parameter. But we can accept "A or wider."
  def set[B >: A](b: B): Box[B] = Box(b)

  override def toString = s"Box($value)"

@main def step2e(): Unit =
  val bookBox: Box[Book] = Box(Book("Scala in Depth"))
  println(bookBox)                    // Box(Book(Scala in Depth))

  // Put a Book in → stays Box[Book]
  val bookBox2: Box[Book] = bookBox.set(Book("FP in Scala"))
  println(bookBox2)                   // Box(Book(FP in Scala))

  // Put Electronics in → widens to Box[Product] (common parent of Book and Electronics)
  val productBox: Box[Product] = bookBox.set(Electronics("Keyboard"))
  println(productBox)                 // Box(Electronics(Keyboard))

  // The type widened automatically in the safe direction.
  // Box[Book].set(Electronics) → Box[Product]
  // Nobody gets deceived. The widening is expressed in the type.
