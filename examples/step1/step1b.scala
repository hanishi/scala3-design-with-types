// step1b.scala

def first[A](xs: List[A]): A = xs.head

@main def step1b(): Unit =
  val names = List("Scala", "Rust", "Go")
  val top: String = first(names)  // Compiler resolves A = String
  println(top)

  val nums = List(1, 2, 3)
  val one: Int = first(nums)      // Compiler resolves A = Int
  println(one)

  // What about this?
  val mixed = List(1, "two", 3.0)
  val what = first(mixed)         // What is A here?
  println(what.getClass)
