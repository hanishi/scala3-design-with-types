// step4d.scala

// RULE OF THUMB:
//
// Type parameters  → when the caller specifies the type
//                    List[Int], Option[String], Map[K, V]
//                    "I want a list OF something specific"
//
// Type members     → when the implementation specifies the type
//                    Database { type Row }, Pipeline { type Intermediate }
//                    "The type is an internal detail of this thing"

// Another way to think about it:

// Type parameter = the type is an ARGUMENT (provided externally)
trait Repository[Entity]:      // "Give me a repo for THIS entity type"
  def find(id: String): Option[Entity]

// Type member = the type is a PROPERTY (defined internally)
trait Connection:               // "Each connection has ITS OWN cursor type"
  type Cursor
  def open(): Cursor
  def read(c: Cursor): String

// You can also mix both:
trait Store[K]:                 // K is provided by caller
  type V                        // V is determined by implementation
  def get(key: K): Option[V]

@main def step4d(): Unit =
  println("Read the code and comments — this is a design guide, not a runnable demo")
