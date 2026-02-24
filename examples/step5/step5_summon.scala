// step5_summon.scala

trait Describable[A]:
  def describe: String

object Describable:
  given Describable[Int] with
    def describe: String = "a 32-bit integer"

  given Describable[String] with
    def describe: String = "a sequence of characters"

@main def step5_summon(): Unit =
  // summon[X] says: "Compiler, find me a given instance of type X"
  val intDesc = summon[Describable[Int]]
  println(intDesc.describe)     // "a 32-bit integer"

  val strDesc = summon[Describable[String]]
  println(strDesc.describe)     // "a sequence of characters"

  // No given Describable[Boolean] exists:
  // val boolDesc = summon[Describable[Boolean]]
  // error: no given instance of type Describable[Boolean] was found
