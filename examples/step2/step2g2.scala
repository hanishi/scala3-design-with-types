// step2g2.scala — Variance in action: a simplified ZIO-like effect

// An effect that requires an environment R and produces a value A.
// This is the core idea behind ZIO[-R, +E, +A] (simplified: no error channel).
class Effect[-R, +A](val run: R => A): 
  def map[B](f: A => B): Effect[R, B] =
    Effect(r => f(run(r)))

  // Intuitively, using the R from the class, the signature would be:
  //   def flatMap[B](f: A => Effect[R, B]): Effect[R, B]
  // But the class's -R ends up in a covariant position, so the compiler rejects it.
  // (See the chapter text for the full position trace.)
  // Fix: introduce R1, a fresh method parameter not subject to variance checking,
  // and define it as R's subtype — so R1 provides at least the same environment as R.
  def flatMap[R1 <: R, B](f: A => Effect[R1, B]): Effect[R1, B] =
    Effect(r => f(run(r)).run(r))

// --- Setup: a small service hierarchy ---

trait Database:
  def lookup(id: Int): String

trait Logger:
  def log(msg: String): Unit

class AppEnv extends Database, Logger:
  def lookup(id: Int): String = s"user-$id"
  def log(msg: String): Unit = println(s"  LOG: $msg")

// --- Effects with different environment requirements ---

// Needs only a Database
val fetchUser: Effect[Database, String] = Effect { db =>
  db.lookup(1)
}

// Needs only a Logger
val logMsg: String => Effect[Logger, Unit] = msg => Effect { logger =>
  logger.log(msg)
}

@main def step2g2(): Unit =
  val env = AppEnv()

  // fetchUser needs Database, logMsg needs Logger
  // the for-comprehension combines them — the result needs both
  val program: Effect[Database & Logger, Unit] =
    for
      user <- fetchUser
      _    <- logMsg(s"fetched $user")
    yield ()

  // AppEnv extends Database, Logger — it satisfies both
  program.run(env)  // LOG: fetched user-1

  // A plain Database isn't enough — program also needs a Logger
  // val db: Database = new Database { def lookup(id: Int) = s"user-$id" }
  // program.run(db)  // Compile error — db is not a Logger
