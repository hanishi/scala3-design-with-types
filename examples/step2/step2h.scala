// step2h.scala — Covariance in the standard library

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double, val isbn: String) extends Item:
  override def toString = s"Book($name)"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name)"

@main def step2h(): Unit =
  val book = Book("Scala in Depth", 45.0, "978-1617295")
  val dvd = DVD("The Matrix", 19.99)

  // --- Option[+A] ---
  // Some[Book] is usable as Option[Item] — a Book result IS an Item result
  val maybeBook: Option[Book] = Some(book)
  val maybeItem: Option[Item] = maybeBook  // covariance
  println(maybeItem)  // Some(Book(Scala in Depth))

  // getOrElse uses [B >: A] — the lower bound escape hatch for covariance
  val item: Item = maybeBook.getOrElse(dvd)  // Option[Book] → fallback DVD → Item
  println(item)

  // --- List[+A] ---
  // List[Book] is usable as List[Item]
  val books: List[Book] = List(book)
  val items: List[Item] = books  // covariance
  println(items)

  // :+ uses [B >: A] — adding a DVD widens to List[Item]
  val mixed: List[Item] = books :+ dvd
  println(mixed)  // List(Book(Scala in Depth), DVD(The Matrix))

  // --- Nothing ---
  // Nothing is Scala's bottom type — a subtype of every other type.
  // No value of type Nothing can ever exist, but the compiler uses it as a
  // placeholder: "this side has no type yet." Combined with covariance,
  // Nothing <: anything, so it fits wherever a type is expected.

  // --- Either[+A, +B] ---
  // Either is covariant in BOTH type parameters
  // Right(book) is Right[Nothing, Book] — Nothing is the Left type.
  // Covariance on +A: Nothing <: String, so Either[Nothing, Book] <: Either[String, Book]
  val result: Either[String, Book] = Right(book)
  val wider: Either[String, Item] = result  // covariance on +B: Book <: Item
  println(wider)

  // Same idea with Left: Left("not found") is Left[String, Nothing]
  // Covariance on +B: Nothing <: Book, so Either[String, Nothing] <: Either[String, Book]
  val failed: Either[String, Book] = Left("not found")
  println(failed)

  // orElse uses [B1 >: B] — recovery widens the Right type
  val recovered: Either[String, Item] = failed.orElse(Right(dvd))
  println(recovered)  // Right(DVD(The Matrix))
