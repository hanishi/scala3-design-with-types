// step2d.scala

trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double) extends Item:
  override def toString = s"Book($name, $$${price})"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name, $$${price})"

// Uncomment and compile each block one at a time:

// 1. Try +A with a var — var lets you write a new value in, violating +A.
// class Box[+A](var value: A)
// [error] covariant type A occurs in invariant position in type A of variable value
// [error] class Box[+A](var value: A)
// [error]               ^^^^^^^^^^^^

// 2. OK, only a val. But what about checking what's inside?
class Box[+A](val value: A):
  def contains(item: A): Boolean = value == item
  // [error] covariant type A occurs in contravariant position in type A of parameter item
  // [error]   def contains(item: A): Boolean = value == item
  // [error]                ^^^^^^^
  //
  // With just [A] (invariant), contains works fine.
  // It's the + that prevents passing A in.

@main def step2d(): Unit =
  println("Uncomment each block and read the error messages")
