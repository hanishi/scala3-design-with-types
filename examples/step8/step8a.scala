// step8a.scala

@main def step8a(): Unit =
  // =:= is evidence that two types are equal
  val evidence: Int =:= Int = summon[Int =:= Int]  // OK

  // No evidence that String =:= Int
  // val bad = summon[String =:= Int]
  // error: Cannot prove that String =:= Int.

  // The evidence object can convert values between the two types
  def convert[A, B](a: A)(using ev: A =:= B): B = ev(a)

  val x: Int = 42
  val y: Int = convert[Int, Int](x)  // OK: Int =:= Int can be proven
  println(y)

  // val z: String = convert[Int, String](x)  // Compile error!
