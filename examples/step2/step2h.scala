// step2h.scala

// An event hierarchy — very common in event-driven systems
sealed trait OrderEvent
case class OrderPlaced(orderId: String, timestamp: Long) extends OrderEvent
case class PaymentReceived(orderId: String, timestamp: Long, amount: Double) extends OrderEvent
case class ItemShipped(orderId: String, timestamp: Long, trackingId: String) extends OrderEvent

// An event stream. Should EventStream[PaymentReceived] be usable as EventStream[OrderEvent]?
// YES — if you're only reading events out, a stream of payments IS a stream of order events.

// Invariant version — causes friction
class EventStream[A](val events: List[A]):
  def latest: Option[A] = events.headOption

// Covariant version — works naturally
class EventStreamCov[+A](val events: List[A]):
  def latest: Option[A] = events.headOption

@main def step2h(): Unit =
  val payments = EventStreamCov(List(
    PaymentReceived("ord1", 1000L, 99.99),
    PaymentReceived("ord2", 2000L, 49.50),
  ))

  // This just works. A stream of payments IS a stream of order events.
  val events: EventStreamCov[OrderEvent] = payments
  println(events.latest)

  // With the invariant version, you'd have to write:
  val paymentsInv = EventStream(List(PaymentReceived("ord1", 1000L, 99.99)))
  // val eventsInv: EventStream[OrderEvent] = paymentsInv  // REJECTED
  // You'd be forced to create a new EventStream[OrderEvent] by copying.
