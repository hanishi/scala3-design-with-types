# Step 4: Type Members — Types Inside Types

Type parameters are great when the *caller* decides the type: `List[Int]`, `Box[String]`.
But sometimes the *implementation* should decide — and the outside world shouldn't even
need to know. That's what type members are for.

Scala offers this second mechanism — **type members**, types declared *inside* a trait or class.
This is unusual among mainstream languages and unlocks patterns that type parameters can't express.

## 4-1. Type Members vs Type Parameters

```scala
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
```

The key difference: with a type parameter, the type appears on the outside (`ContainerParam[Int]`).
With a type member, the type is **inside the object** — you access it through the value itself.

## 4-2. Path-Dependent Types — Types That Belong to Values

```scala
// step4b.scala

// A database connection where each database has its own Row type
trait Database:
  type Row
  def query(sql: String): List[Row]
  def process(row: Row): String

// PostgreSQL returns tuples
val postgres: Database = new Database:
  type Row = (String, Int, Boolean)
  def query(sql: String) = List(("Alice", 30, true), ("Bob", 25, false))
  def process(row: Row) = s"${row._1} (age ${row._2})"

// SQLite returns maps
val sqlite: Database = new Database:
  type Row = Map[String, String]
  def query(sql: String) = List(Map("name" -> "Alice", "age" -> "30"))
  def process(row: Row) = row.getOrElse("name", "unknown")

@main def step4b(): Unit =
  // This works: query and process use the same database's Row type
  val rows = postgres.query("SELECT * FROM users")
  rows.foreach(r => println(postgres.process(r)))

  // This does NOT compile: mixing Row types from different databases
  // val sqliteRows = sqlite.query("SELECT * FROM users")
  // postgres.process(sqliteRows.head)
  // error: Found sqlite.Row, Required postgres.Row
```

**Uncomment and read the error.** The compiler says `sqlite.Row` is not `postgres.Row`.
Even though both are type members called `Row`, they belong to different *values*.
`postgres.Row` and `sqlite.Row` are path-dependent types — the path through the value matters.

**This is something type parameters can't do.** With `Database[Row]`, nothing would prevent
you from passing a PostgreSQL row to a SQLite processor. With type members, the compiler
links the row type to the specific database *instance*.

## 4-3. Abstract Type Members — Deferring Type Decisions

```scala
// step4c.scala

// A processing pipeline where each stage defines its own intermediate type
trait Pipeline:
  type Input
  type Output
  type Intermediate  // The implementation decides what this is

  def parse(raw: Input): Intermediate
  def transform(data: Intermediate): Output

// A CSV-to-JSON pipeline
class CsvToJson extends Pipeline:
  type Input = String
  type Output = String
  type Intermediate = List[Map[String, String]]  // parsed CSV rows

  def parse(raw: String): Intermediate =
    raw.split("\n").toList.map: line =>
      val cols = line.split(",")
      Map("name" -> cols(0), "value" -> cols(1))

  def transform(data: Intermediate): Output =
    data.map(row => s"""{"name":"${row("name")}","value":"${row("value")}"}""")
      .mkString("[", ",", "]")

@main def step4c(): Unit =
  val pipeline = CsvToJson()
  val csv = "Alice,100\nBob,200"
  val intermediate = pipeline.parse(csv)
  val json = pipeline.transform(intermediate)
  println(json)

  // The Intermediate type is specific to CsvToJson.
  // Another Pipeline implementation could use a completely different Intermediate.
  // Callers don't need to know or care what Intermediate is.
```

This pattern is powerful for **information hiding**. The `Pipeline` trait exposes
`Input` and `Output` but the `Intermediate` type is an implementation detail.
With type parameters, you'd need `Pipeline[Input, Output, Intermediate]` —
leaking a type that callers shouldn't have to know about.

## 4-4. When to Use Type Members vs Type Parameters

```scala
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
```

## 4-5. Connection to Opaque Types

You've already used type members without realizing it.
`opaque type USD = Double` inside an `object` is a type member whose underlying
type is hidden from outside. The same information-hiding principle applies:
the outside world sees `Currency.USD` but can't see that it's `Double`.

Opaque types are essentially type members with compiler-enforced opacity.
This is why they must be defined inside an `object` or `class` — they need
an enclosing scope to define the boundary of visibility.
