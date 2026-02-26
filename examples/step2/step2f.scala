// step2f.scala

trait Item:
  def name: String
  def price: Double

trait Shippable extends Item:
  def weight: Double
  def shippingCost: Double = weight * 0.5

case class Book(name: String, price: Double, weight: Double) extends Shippable
case class DVD(name: String, price: Double, weight: Double) extends Shippable
case class DigitalGiftCertificate(name: String, price: Double) extends Item  // Not shippable

// Only Shippable items can be sorted by shipping cost
def sortByShipping[A <: Shippable](items: List[A]): List[A] =
  items.sortBy(_.shippingCost)

// Combining covariance with an upper bound:
// +A means Shipment[Book] <: Shipment[Shippable]
// <: Shippable means every item is guaranteed to have shippingCost
class Shipment[+A <: Shippable](private val items: List[A]):
  def totalShippingCost: Double = items.map(_.shippingCost).sum
  def sortByShipping: List[A] = items.sortBy(_.shippingCost)
  // +A forbids A in input position, so we use [B >: A] — same escape hatch as List's :+
  def add[B >: A <: Shippable](item: B): Shipment[B] = Shipment(items :+ item)

@main def step2f(): Unit =
  // Mix of Book and DVD → List[Shippable], A = Shippable
  val items = List(
    Book("Scala in Depth", 45.0, 0.8),
    DVD("The Matrix", 19.99, 0.2),
  )
  sortByShipping(items).foreach(p => println(s"${p.name}: shipping=${p.shippingCost}"))

  // Try sorting DigitalGiftCertificate?
  //val cards = List(DigitalGiftCertificate("Amazon $50", 50.0))
  //sortByShipping(cards)
  // [error]   sortByShipping(cards)
  // [error]                  ^^^^^

  // Upper bound + covariance on a class
  val shipment = Shipment(List(Book("Scala in Depth", 45.0, 0.8)))
  println(s"Total shipping: ${shipment.totalShippingCost}")

  // add uses [B >: A <: Shippable] — widens from Shipment[Book] to Shipment[Shippable]
  val wider = shipment.add(DVD("The Matrix", 19.99, 0.2))
  println(s"Total shipping after add: ${wider.totalShippingCost}")

  // Try adding a DigitalGiftCertificate to a Shipment?
  //val shipmentFails = Shipment(List(DigitalGiftCertificate("Netflix", 20.00)))
  // [error]   val shipmentFails = Shipment(List(DigitalGiftCertificate("Netflix", 20.00)))
  // [error]                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^