// step5b.scala

// Define a "can be displayed" capability as a typeclass
trait Show[A]:
  def show(a: A): String

object Show:
  // How to display an Int
  given Show[Int] with
    def show(a: Int): String = a.toString

  // How to display a String
  given Show[String] with
    def show(a: String): String = s"\"$a\""

  // How to display a List[A] (only if A itself has Show)
  given [A](using inner: Show[A]): Show[List[A]] with
    def show(a: List[A]): String =
      a.map(inner.show).mkString("[", ", ", "]")

// Context bound: A: Show means "a given Show[A] must exist"
def display[A: Show](a: A): String =
  summon[Show[A]].show(a)

@main def step5b(): Unit =
  println(display(42))
  println(display("hello"))
  println(display(List(1, 2, 3)))
  println(display(List(List(1, 2), List(3, 4))))

  // No given Show[Boolean] exists:
  // println(display(true))
  // error: no given instance of type Show[Boolean] was found
