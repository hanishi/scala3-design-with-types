// step7c.scala

import scala.compiletime.constValue

// constValue lets you read a singleton type as a runtime value.
// This bridges the gap between type-level and value-level.

// constValue[A] extracts the singleton value from A (e.g., 42 from type 42).
// The match cases check the type of that extracted value.
inline def describe[A]: String =
  inline constValue[A] match
    case _: Int    => "integer"
    case _: String => "string"
    case _: Boolean => "boolean"

@main def step7c(): Unit =
  // Singleton types: the type IS the value
  val x: 42 = 42              // x has type 42, not Int
  val y: "hello" = "hello"    // y has type "hello", not String

  // constValue extracts the value from the type
  val n: Int = constValue[42]        // 42
  val s: String = constValue["hello"] // "hello"
  println(s"n: $n, s: $s")
