// step9c.scala

// Practical example: combining union and intersection types

// --- Union type: API response handling ---

case class Success(body: String, status: Int)
case class Failure(error: String, status: Int)

type ApiResponse = Success | Failure

def handle(response: ApiResponse): String =
  response match
    case Success(body, status) => s"OK ($status): $body"
    case Failure(error, status) => s"ERROR ($status): $error"

// --- Intersection type: resource that is Readable & Closeable ---

trait Readable:
  def read(): String

trait Closeable:
  def close(): Unit

def useAndClose(resource: Readable & Closeable): String =
  try resource.read()
  finally resource.close()

class FileHandle(name: String) extends Readable, Closeable:
  def read(): String = s"contents of $name"
  def close(): Unit = println(s"  closed $name")

// --- Combining both: a function that returns a union, called on an intersection ---

trait Validated:
  def isValid: Boolean

trait Named:
  def name: String

def check(value: Validated & Named): String | Failure =
  if value.isValid then s"${value.name} is valid"
  else Failure(s"${value.name} failed validation", 400)

case class FormField(name: String, isValid: Boolean) extends Validated, Named

@main def step9c(): Unit =
  // Union: pattern match on API responses
  println(handle(Success("user data", 200)))     // OK (200): user data
  println(handle(Failure("not found", 404)))      // ERROR (404): not found

  // Intersection: use a resource that is both Readable and Closeable
  val file = FileHandle("config.json")
  println(useAndClose(file))                      // contents of config.json

  // Combined: function taking intersection, returning union
  println(check(FormField("email", isValid = true)))   // email is valid
  println(check(FormField("phone", isValid = false)))  // Failure(phone failed validation,400)
