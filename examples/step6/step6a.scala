// step6a.scala

// You're building a utility that "unwraps" container types.
// Given Option[Int], you want Int. Given List[String], you want String.
// Given a plain Int, you just want Int.
// The RETURN TYPE changes based on the INPUT TYPE.

// A typeclass can't do this — it can vary behavior, but not the output type.
// A match type can:

type Unwrap[X] = X match
  case Option[t] => t
  case List[t]   => t
  case Either[_, t] => t
  case X         => X         // fallback: return as-is

// Now the compiler computes:
//   Unwrap[Option[Int]]      = Int
//   Unwrap[List[String]]     = String
//   Unwrap[Either[Err, User]] = User
//   Unwrap[Boolean]          = Boolean

@main def step6a(): Unit =
  val a: Unwrap[Option[Int]] = 42             // compiler knows this is Int
  val b: Unwrap[List[String]] = "hello"       // compiler knows this is String
  val c: Unwrap[Boolean] = true               // compiler knows this is Boolean

  println(s"a: $a, b: $b, c: $c")
