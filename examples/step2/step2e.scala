// step2e.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double) extends Item:
  override def toString = s"Book($name, $$${price})"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name, $$${price})"

// An immutable shopping cart. +A makes it covariant.
class Cart[+A](private val items: List[A] = Nil):
  // def add(item: A) won't compile with +A. Try it!
  // def add[B >: A](item: B) compiles — B must be A or wider,
  // so the cart can only stay the same or widen, never narrow:
  //   add(Book)        to Cart[Book] → still a cart of Books    → Cart[Book]
  //   add(DVD)         to Cart[Book] → no longer just Books     → Cart[Item]
  def add[B >: A](item: B): Cart[B] = Cart(item :: items)

  // A is whatever this cart holds — Book for Cart[Book], Item for Cart[Item].
  def total(f: A => Double): Double = items.map(f).sum

  override def toString = s"Cart(${items.mkString(", ")})"

@main def step2e(): Unit =
  // Cart() is Cart[Nothing] — an empty cart with no type yet.
  // add(Book(...)) widens Nothing to Book → Cart[Book].
  val bookCart: Cart[Book] = Cart().add(Book("Scala in Depth", 45.0))
  println(bookCart)                     // Cart(Book(Scala in Depth, $45.0))

  // Add another Book → stays Cart[Book]
  val bookCart2: Cart[Book] = bookCart.add(Book("FP in Scala", 35.0))
  println(bookCart2)                    // Cart(Book(FP in Scala, $35.0), Book(Scala in Depth, $45.0))

  // Add DVD → widens to Cart[Item] (common parent of Book and DVD)
  val mixedCart: Cart[Item] = bookCart.add(DVD("The Matrix", 19.99))
  println(mixedCart)                    // Cart(DVD(The Matrix, $19.99), Book(Scala in Depth, $45.0))

  // The type widened: mixedCart is Cart[Item], not Cart[Book].
  // A is now Item, so total's signature became:
  //   total(f: Item => Double)
  // Item has price, so _.price just works:
  val subtotal = mixedCart.total(_.price)
  val discounted = mixedCart.total(_.price * 0.95)
  println(f"Subtotal:     $$$subtotal%.2f")       // Subtotal:     $64.99
  println(f"With 5%% off: $$$discounted%.2f")     // With 5% off: $61.74
