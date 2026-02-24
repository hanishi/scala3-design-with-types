// step2d.scala

class Product(val name: String)
class Book(name: String) extends Product(name)
class Electronics(name: String) extends Product(name)

// Try +A with a var → the compiler catches the mismatch
// class MutBox[+A](var value: A)
// error: covariant type A occurs in contravariant position
//        in type A of parameter value_=

// OK, only a val. But add a setter method?
class Box[+A](val value: A):
  // def set(a: A): Box[A] = Box(a)
  // error: covariant type A occurs in contravariant position
  //        in type A of parameter a

  override def toString = s"Box($value)"

@main def step2d(): Unit =
  println("Uncomment each block and read the error messages")
