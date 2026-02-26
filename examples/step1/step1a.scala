// step1a.scala

// A box that holds anything (no type parameter)
class AnyBox(val value: Any)

// A box with a type parameter
class Box[A](val value: A)

@main def step1(): Unit =
  // AnyBox: easy to put things in
  val anyBox = AnyBox(42)
  // val n: Int = anyBox.value  // Compile error! Any is not Int
  val n: Int = anyBox.value.asInstanceOf[Int]  // Dangerous cast

  // Box[A]: the type is preserved
  val intBox = Box(42)       // Inferred as Box[Int]
  val m: Int = intBox.value  // Comes out as Int — no cast needed

  // Wrong type? Compile error.
  // val s: String = intBox.value  // error: Found Int, Required String

  println(s"AnyBox: $n, Box[Int]: $m")
