// step4a.scala

// With a type parameter: the type is visible from outside
trait ContainerParam[A]:
  def get: A

// With a type member: the type is hidden inside
trait ContainerMember:
  type Element
  def get: Element

@main def step4a(): Unit =
  // Type parameter: the caller decides the type
  val intContainer: ContainerParam[Int] = new ContainerParam[Int]:
    def get: Int = 42

  // Type member: the implementation decides the type
  val memberContainer: ContainerMember = new ContainerMember:
    type Element = Int
    def get: Int = 42

  val a: Int = intContainer.get               // Caller knows it's Int
  val b: memberContainer.Element = memberContainer.get  // Type depends on the *value*

  println(s"a: $a, b: $b")
