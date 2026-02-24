# Setup

```bash
# Install scala-cli (if you haven't already)
curl -sSLf https://scala-cli.virtuslab.org/get | sh

# Verify
scala-cli version
# Scala 3.x is required
```

## Using the Example Files

Every code example in this book is available as a ready-to-run file in the `examples/` directory. Clone the repository and run any example:

```bash
git clone https://github.com/hanishi/scala3-design-with-types.git
cd scala3-design-with-types

# Run a specific example
scala-cli run examples/step0/step0.scala

# Or cd into a step directory
cd examples/step2
scala-cli run step2c.scala
```

Each file is self-contained — just pick the step you're reading and run the corresponding file.

> **Important:** Always run a single file at a time — do **not** compile an entire directory (e.g., `scala-cli compile examples/step2/`). Files within a step intentionally redefine the same classes (`Product`, `Book`, `Box`, etc.) with different signatures to show a progression. Compiling them together will produce duplicate-definition errors.
