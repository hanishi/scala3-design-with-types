// step2f.scala

// E-commerce example
trait Product:
  def name: String
  def price: Double

trait Shippable extends Product:
  def weight: Double
  def shippingCost: Double = weight * 0.5

case class Book(name: String, price: Double, weight: Double) extends Shippable
case class Laptop(name: String, price: Double, weight: Double) extends Shippable
case class GiftCard(name: String, price: Double) extends Product  // Not shippable

// Only Shippable products can be sorted by shipping cost
def sortByShipping[A <: Shippable](products: List[A]): List[A] =
  products.sortBy(_.shippingCost)

@main def step2f(): Unit =
  val items = List(
    Book("Scala in Depth", 45.0, 0.8),
    Book("FP Simplified", 35.0, 1.2),
  )
  sortByShipping(items).foreach(p => println(s"${p.name}: shipping=${p.shippingCost}"))

  // Try sorting GiftCards?
  // val cards = List(GiftCard("Amazon $50", 50.0))
  // sortByShipping(cards)  // Compile error! GiftCard is not Shippable
