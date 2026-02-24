# Step 6: Match Types — When the Return Type Depends on the Input Type

Everything so far uses types to describe what things *are*. But what if the return type
of a function should *change* depending on what you pass in? A function that unwraps
`Option[Int]` to `Int` but unwraps `List[String]` to `String`?

Match types are type-level `if-else`. They're useful when a function's **return type**
should change based on the **input type** — something that typeclasses and bounds can't express.

## 6-1. The Problem: Unwrapping Nested Types

```scala
// step6a.scala

// You're building a utility that "unwraps" container types.
// Given Option[Int], you want Int. Given List[String], you want String.
// Given a plain Int, you just want Int.
// The RETURN TYPE changes based on the INPUT TYPE.

// A typeclass can't do this — it can vary behavior, but not the output type.
// A match type can:

type Unwrap[X] = X match
  case Option[t] => t
  case List[t]   => t
  case Either[_, t] => t
  case X         => X         // fallback: return as-is

// Now the compiler computes:
//   Unwrap[Option[Int]]      = Int
//   Unwrap[List[String]]     = String
//   Unwrap[Either[Err, User]] = User
//   Unwrap[Boolean]          = Boolean

@main def step6a(): Unit =
  val a: Unwrap[Option[Int]] = 42             // compiler knows this is Int
  val b: Unwrap[List[String]] = "hello"       // compiler knows this is String
  val c: Unwrap[Boolean] = true               // compiler knows this is Boolean

  println(s"a: $a, b: $b, c: $c")
```

## 6-2. Practical Use: Type-Safe API Response Handling

```scala
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
```

**Why this matters:** Without match types, you'd either return `Any` and cast (unsafe),
or have separate `fetchCampaign`, `fetchMetrics`, `fetchBudget` functions (boilerplate).
Match types give you one function with full type safety.

## 6-3. When Match Types Are the Wrong Tool

Match types compute a **different type** from an input type.
If the output type is always the same (e.g., always `String`), there's nothing to compute —
use a typeclass instead ([Step 5](../part1/step5.md)).

```
Output type changes based on input type  → Match type
Same output type, different behavior      → Typeclass
```
