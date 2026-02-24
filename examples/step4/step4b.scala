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
