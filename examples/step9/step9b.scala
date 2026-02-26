// step9b.scala

// Intersection types: A & B means "both A and B"

// Two independent traits — no shared hierarchy
trait Printable:
  def printOut(): String

trait Serializable:
  def serialize(): Array[Byte]

// A method that requires BOTH capabilities
def printAndSave(value: Printable & Serializable): Unit =
  println(value.printOut())
  val bytes = value.serialize()
  println(s"  serialized: ${bytes.length} bytes")

// A class that implements both traits satisfies Printable & Serializable
case class Report(title: String, content: String) extends Printable, Serializable:
  def printOut(): String = s"[$title] $content"
  def serialize(): Array[Byte] = s"$title|$content".getBytes

// A class that only implements one trait does NOT satisfy the intersection
case class Label(text: String) extends Printable:
  def printOut(): String = text

@main def step9b(): Unit =
  val report = Report("Q4 Sales", "Revenue up 15%")
  printAndSave(report)  // OK: Report is Printable & Serializable

  val label = Label("Hello")
  println(label.printOut())  // OK: Label is Printable
  // printAndSave(label)     // Compile error! Label is not Serializable
