// step2crossed.scala — Why [-A >: X] is a code smell
//
// The pairings are fixed: +A pairs with >: (lower bound),
// -A pairs with <: (upper bound). What if you cross them?

trait Item:
  def name: String
  def price: Double

case class Book(name: String, price: Double) extends Item
case class DVD(name: String, price: Double) extends Item

// You already know PriceFormatter[-A] from the contravariance section.
// What if someone adds a lower bound?

trait PriceFormatter[-A >: Item]:
  def format(a: A): String

// A = Item — works
val itemFmt: PriceFormatter[Item] = a => s"${a.name}: $$${a.price}"

// A = Any — works (Any >: Item)
val anyFmt: PriceFormatter[Any] = a => a.toString

// Uncomment — won't compile. The bound blocks PriceFormatter[Book]:
// val bookFmt: PriceFormatter[Book] = b => s"${b.name}: $$${b.price}"

// But even without >: Item, could you USE a PriceFormatter[Book]
// where PriceFormatter[Item] is expected?
//
// No. Contravariance flips the direction:
//   Book <: Item  →  PriceFormatter[Item] <: PriceFormatter[Book]
//
// A Book-only formatter is a SUPERTYPE of an Item formatter.
// You can't pass it where PriceFormatter[Item] is expected.
// The bound prevents something that was already unusable.

def printPrices(items: List[Item], fmt: PriceFormatter[Item]): Unit =
  items.foreach(a => println(fmt.format(a)))

@main def step2crossed(): Unit =
  printPrices(List(Book("Scala", 45.0), DVD("The Matrix", 19.99)), itemFmt)
