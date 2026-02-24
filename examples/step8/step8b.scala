// step8b.scala

// A generic pipeline stage that transforms From → To
class Stage[From, To](val transform: From => To):
  // Chain two stages: Stage[A, B] andThen Stage[B, C] = Stage[A, C]
  def andThen[Next](next: Stage[To, Next]): Stage[From, Next] =
    Stage(from => next.transform(transform(from)))

  // "run" only makes sense when input and output are the same type
  // (a stage that can be applied repeatedly)
  def runLoop(value: From, times: Int)(using ev: From =:= To): From =
    var current = value
    for _ <- 1 to times do
      current = ev.flip(transform(current)) // To → From via evidence
    current

@main def step8b(): Unit =
  // Stage[Int, Int] — doubles a number
  val doubler = Stage[Int, Int](_ * 2)
  println(doubler.runLoop(1, 5))  // 1 → 2 → 4 → 8 → 16 → 32

  // Stage[String, String] — adds exclamation
  val exclaim = Stage[String, String](_ + "!")
  println(exclaim.runLoop("hello", 3))  // hello! → hello!! → hello!!!

  // Stage[String, Int] — can't loop, types don't match
  val counter = Stage[String, Int](_.length)
  // counter.runLoop("hi", 3)
  // error: Cannot prove that String =:= Int.
  // Makes sense: you can't feed an Int back into something expecting String
