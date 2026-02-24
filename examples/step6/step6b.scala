// step6b.scala

// In an e-commerce system, different API endpoints return different types.
// A match type lets you write one `fetch` function where the compiler
// knows the return type based on the endpoint.

sealed trait Endpoint
case class ProductEndpoint(sku: String) extends Endpoint
case class OrderEndpoint(orderId: String) extends Endpoint
case class InventoryEndpoint(sku: String, warehouse: String) extends Endpoint

case class ProductInfo(sku: String, name: String, price: Double)
case class OrderStatus(orderId: String, status: String, total: Double)
case class StockLevel(sku: String, warehouse: String, quantity: Int)

// The return type is computed from the endpoint type
type ResponseOf[E <: Endpoint] = E match
  case ProductEndpoint   => ProductInfo
  case OrderEndpoint     => OrderStatus
  case InventoryEndpoint => StockLevel

// Simulated fetch — in real code this would call an HTTP client
inline def fetch[E <: Endpoint](endpoint: E): ResponseOf[E] = endpoint match
  case _: ProductEndpoint   => ProductInfo("SKU-001", "Wireless Mouse", 29.99)
  case _: OrderEndpoint     => OrderStatus("ORD-123", "shipped", 84.97)
  case _: InventoryEndpoint => StockLevel("SKU-001", "US-WEST", 150)

@main def step6b(): Unit =
  // The compiler knows each return type — no casting, no pattern matching on the caller side
  val product: ProductInfo = fetch(ProductEndpoint("SKU-001"))
  val order: OrderStatus = fetch(OrderEndpoint("ORD-123"))
  val stock: StockLevel = fetch(InventoryEndpoint("SKU-001", "US-WEST"))

  println(s"Product: ${product.name}, $$${product.price}")
  println(s"Order ${order.orderId}: ${order.status}")
  println(s"Stock: ${stock.quantity} units in ${stock.warehouse}")

  // If you try to assign the wrong type:
  // val wrong: StockLevel = fetch(ProductEndpoint("SKU-001"))
  // error: Found ProductInfo, Required StockLevel
