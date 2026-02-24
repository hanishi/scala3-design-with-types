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
