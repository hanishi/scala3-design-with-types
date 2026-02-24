// explore.scala

// Different product types have different invoice details
case class PhysicalItem(name: String, price: Double, weight: Double, shippingCost: Double)
case class DigitalItem(name: String, price: Double, downloadUrl: String)
case class Subscription(name: String, price: Double, billingCycle: String, nextBillingDate: String)

// You want a single `invoiceLine` function that handles all of them.
// But how should you parameterize it?
