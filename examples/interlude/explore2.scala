// explore2.scala

case class PhysicalItem(name: String, price: Double, weight: Double, shippingCost: Double)
case class DigitalItem(name: String, price: Double, downloadUrl: String)
case class Subscription(name: String, price: Double, billingCycle: String, nextBillingDate: String)

// Define the capability: "can produce an invoice line"
trait InvoiceLine[A]:
  def format(item: A): String

object InvoiceLine:
  given InvoiceLine[PhysicalItem] with
    def format(item: PhysicalItem): String =
      s"${item.name} | $$${item.price} + $$${item.shippingCost} shipping (${item.weight}kg)"

  given InvoiceLine[DigitalItem] with
    def format(item: DigitalItem): String =
      s"${item.name} | $$${item.price} (download: ${item.downloadUrl})"

  given InvoiceLine[Subscription] with
    def format(item: Subscription): String =
      s"${item.name} | $$${item.price}/${item.billingCycle} (next: ${item.nextBillingDate})"

def invoiceLine[A: InvoiceLine](item: A): String =
  summon[InvoiceLine[A]].format(item)

@main def explore2(): Unit =
  println(invoiceLine(PhysicalItem("Keyboard", 79.99, 0.5, 5.99)))
  println(invoiceLine(DigitalItem("E-book", 14.99, "https://example.com/dl/123")))
  println(invoiceLine(Subscription("Cloud Storage", 9.99, "month", "2024-03-01")))

  // New type without InvoiceLine? Compile error.
  // case class GiftCard(name: String, value: Double)
  // invoiceLine(GiftCard("Amazon", 50.0))
