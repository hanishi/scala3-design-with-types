# Design with Types

Practical Scala 3 from Basics to Type-Level Programming.

A hands-on tutorial that takes you from basic type parameters (`List[Int]`) through variance, opaque types, typeclasses, match types, and type equality proofs — all by having a conversation with the compiler.

**[Read the tutorial](https://hanishi.github.io/scala3-design-with-types/)** | **[日本語版](https://hanishi.github.io/scala3-design-with-types/ja/)**

## Quick Start

```bash
# Install scala-cli
curl -sSLf https://scala-cli.virtuslab.org/get | sh

# Run an example
scala-cli run examples/step0/step0.scala
```

## Currently Available

- **Step 0:** Life Without Types
- **Step 1:** Type Parameters Are Contracts
- **Step 2:** Variance and Bounds
- **Step 2:** Variance in Practice

More chapters coming soon.

## Examples

The `examples/` directory contains ready-to-run Scala files for every code example in the book. Run any example individually with `scala-cli`:

```bash
scala-cli run examples/step2/step2c.scala
scala-cli run examples/step2/step2g2.scala
```

> **Important:** Always run a single file at a time — do **not** compile an entire directory (e.g., `scala-cli compile examples/step2/`). Files within a step intentionally redefine the same classes (`Product`, `Book`, `Box`, etc.) with different signatures to show a progression. Compiling them together will produce duplicate-definition errors.

## Building the Book Locally

```bash
# Install mdBook
brew install mdbook

# Build and serve English version
cd book && mdbook serve --open

# Build and serve Japanese version
cd book-ja && mdbook serve --open -p 3001
```
