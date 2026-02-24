// explore3.scala

case class PhysicalItem(name: String, price: Double, weight: Double)
case class DigitalItem(name: String, price: Double, downloadUrl: String)

type InvoiceString[A] = A match
  case PhysicalItem => String
  case DigitalItem  => String

// The return type is always String regardless of input...
// Match type adds nothing here. It's the wrong tool for this job.
