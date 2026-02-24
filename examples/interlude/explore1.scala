// explore1.scala

trait HasPrice:
  def price: Double

case class PhysicalItem(name: String, price: Double, weight: Double) extends HasPrice
case class DigitalItem(name: String, price: Double, downloadUrl: String) extends HasPrice

def invoiceLine[A <: HasPrice](item: A): String =
  s"Item: $$${item.price}"
  // ...but how do I access weight or downloadUrl? I only know about price.

@main def explore1(): Unit =
  println(invoiceLine(PhysicalItem("Keyboard", 79.99, 0.5)))
