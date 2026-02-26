// step9a.scala

// Union types: A | B means "either A or B"

// No common supertype between String and Int — in Scala 2, the LUB would be Any.
// In Scala 3, you can say exactly what you mean:
def describe(value: String | Int): String =
  value match
    case s: String => s"text: $s"
    case n: Int    => s"number: $n"

// Contrast with a hierarchy where a named supertype exists:
trait Item:
  def name: String
  def price: Double
class Book(val name: String, val price: Double) extends Item:
  override def toString = s"Book($name)"
class DVD(val name: String, val price: Double) extends Item:
  override def toString = s"DVD($name)"

// When you own the types and they share a common trait, use the trait:
def printItem(item: Item): String =
  s"${item.name}: $$${item.price}"

// When there's no shared supertype, union types express "one of these":
def parseId(input: String | Int): String =
  input match
    case s: String => s"id-$s"
    case n: Int    => s"id-$n"

@main def step9a(): Unit =
  // Union type in action — no common supertype needed
  println(describe("hello"))  // text: hello
  println(describe(42))       // number: 42

  // With a hierarchy, the LUB gives you Item — use Item, not Book | DVD
  val items: List[Item] = List(Book("Scala", 45.0), DVD("The Matrix", 19.99))
  items.foreach(i => println(printItem(i)))

  // Union types shine when the types are unrelated
  println(parseId("abc"))  // id-abc
  println(parseId(123))    // id-123
