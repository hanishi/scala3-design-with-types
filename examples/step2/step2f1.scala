// step2f1.scala — Self-referential bounds: T <: Ordered[T]

// We've seen A <: Shippable — "A must extend Shippable."
// But what happens when the bound refers back to the type itself?

// Start with concrete classes. Notice: each class appears in its own extends clause.

case class Temperature(celsius: Double) extends Ordered[Temperature]:
  def compare(that: Temperature): Int =
    this.celsius.compare(that.celsius)

case class Priority(level: Int) extends Ordered[Priority]:
  def compare(that: Priority): Int =
    this.level.compare(that.level)

// See the pattern?
//   Temperature extends Ordered[Temperature]
//   Priority    extends Ordered[Priority]
//                              ^^^^^^^^^^
//                              itself
//
// Each class promises: "I know how to compare myself to others of my own kind."

// Now the generic version:
def maximum[T <: Ordered[T]](items: List[T]): T =
  items.reduce((a, b) => if a > b then a else b)

// How to read T <: Ordered[T]:
//   1. Start simple:  T <: Ordered     — "T must be Ordered."
//   2. Then ask:      Ordered of what? — of itself.
//
// In plain English: "T must know how to compare itself to other T values."
//
// Plug in a concrete type and it's obvious:
//   Temperature <: Ordered[Temperature]  ✓ — that's exactly what it extends
//   Priority    <: Ordered[Priority]     ✓

// A type without ordering:
case class Label(text: String)

@main def step2f1(): Unit =
  val temps = List(Temperature(36.6), Temperature(38.1), Temperature(37.0))
  println(s"Hottest: ${maximum(temps)}")
  // Hottest: Temperature(38.1)

  val priorities = List(Priority(3), Priority(1), Priority(5))
  println(s"Highest: ${maximum(priorities)}")
  // Highest: Priority(5)

  // Uncomment to see the compiler reject Label:
  //val labels = List(Label("urgent"), Label("low"))
  //maximum(labels)
  // [error] Found:    (labels : List[Label])
  // [error] Required: List[T]
  // [error]
  // [error] where:    T is a type variable with constraint <: Ordered[T]
  // [error]   maximum(labels)
  // [error]           ^^^^^^