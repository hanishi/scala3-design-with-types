/*
 step2crossed.scala — Why [-A >: X] is (usually) a code smell

 The pairings are fixed:

   +A pairs with >: (lower bound)
   -A pairs with <: (upper bound)

 What happens if you cross them?
*/

trait Item:
  def name: String
  def price: Double

trait Book extends Item
case class Comic(name: String, price: Double) extends Book
case class Novel(name: String, price: Double) extends Book
case class DVD(name: String, price: Double) extends Item

/*
            Item
          /      \
       Book      DVD
      /    \
   Comic   Novel
*/


// ------------------------------------------------------------
// Plain contravariant consumer
// ------------------------------------------------------------

trait PriceFormatter[-A]:
  def format(a: A): String


def printPrices(items: List[Item], fmt: PriceFormatter[Item]): Unit =
  items.foreach(i => println(fmt.format(i)))


// A narrower formatter

val bookFmt: PriceFormatter[Book] =
  b => s"${b.name}: $$${b.price}"


// This does NOT compile:
//
// printPrices(items, bookFmt)
//
// Why?

/*
Type hierarchy:

  Comic <: Book <: Item <: Any

Contravariance flips it:

  PriceFormatter[Any]
      <: PriceFormatter[Item]
          <: PriceFormatter[Book]
              <: PriceFormatter[Comic]

printPrices requires PriceFormatter[Item].

Only types to the LEFT (subtypes) are acceptable.

PriceFormatter[Book] is to the RIGHT.
So the compiler rejects it — already.
*/


// A wider formatter DOES work:

val anyFmt: PriceFormatter[Any] =
  _.toString

// printPrices(items, anyFmt)  // compiles


// ------------------------------------------------------------
// The crossed lower bound: [-A >: Item]
// ------------------------------------------------------------

// What if we added >: Item?
//
//   trait PriceFormatter[-A >: Item]:
//     def format(a: A): String
//
// It rejects things like:
//
//   val stringFmt: PriceFormatter[String] = _.toUpperCase  // rejected
//
// But even without the bound, you can't pass PriceFormatter[String]
// to printPrices — contravariance already blocks it.
// The bound prevents creating something you couldn't use anyway.


// ------------------------------------------------------------
// Takeaway
// ------------------------------------------------------------

/*
For a pure consumer type (-A), adding A >: Item
does not improve substitutability safety when
your APIs already require PriceFormatter[Item].

Variance enforces the safety property.

The bound merely narrows the universe of
possible instantiations.

If a constraint doesn't reject anything relevant
to how the type is actually used,
it's not protection — it's noise.
*/


val itemFmt: PriceFormatter[Item] =
  a => s"${a.name}: $$${a.price}"

@main def step2crossed(): Unit =
  val items = List(
    Comic("Watchmen", 25.0),
    Novel("Dune", 18.0),
    DVD("The Matrix", 19.99)
  )
  printPrices(items, itemFmt)
