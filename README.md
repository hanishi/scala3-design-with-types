# Design with Types

Practical Scala 3 from Basics to Type-Level Programming.

A hands-on tutorial that takes you from basic type parameters (`List[Int]`) through variance, opaque types, typeclasses, match types, and type equality proofs — all by having a conversation with the compiler.

**[Read the tutorial](https://hanishi.github.io/scala3-design-with-types/)**

## Quick Start

```bash
# Install scala-cli
curl -sSLf https://scala-cli.virtuslab.org/get | sh

# Run an example
scala-cli run examples/step0/step0.scala
```

## Examples

The `examples/` directory contains ready-to-run Scala files for every code example in the book. Each step has its own subdirectory:

```
examples/
  step0/    — Step 0: The Problem (Any)
  step1/    — Step 1: Type Parameters
  step2/    — Step 2: Variance & Bounds
  step3/    — Step 3: Opaque Types
  step4/    — Step 4: Type Members
  step5/    — Step 5: Typeclasses & Givens
  step6/    — Step 6: Match Types
  step7/    — Step 7: Inline & Compile-Time
  step8/    — Step 8: Type Equality Proofs
  interlude/ — Interlude: Choosing Your Tool
```

Run any example individually with `scala-cli`:

```bash
scala-cli run examples/step2/step2c.scala
scala-cli run examples/step5/step5b.scala
```

> **Important:** Always run a single file at a time — do **not** compile an entire directory (e.g., `scala-cli compile examples/step2/`). Files within a step intentionally redefine the same classes (`Product`, `Book`, `Box`, etc.) with different signatures to show a progression. Compiling them together will produce duplicate-definition errors.

## Structure

- **Part 1: The Type System** (Steps 0–5) — type parameters, variance, bounds, opaque types, type members, typeclasses
- **Part 2: Type-Level Programming** (Steps 6–8) — match types, compile-time validation, type equality proofs

## Building the Book Locally

```bash
# Install mdBook
brew install mdbook

# Build
cd book && mdbook build

# Serve locally with live reload
cd book && mdbook serve --open
```
