# Learning Guide

Recommended `scala-cli` commands when working through this tutorial:

```bash
# 1. Compile and read errors
scala-cli compile step2d.scala

# 2. See type inference results
scala-cli compile step2e.scala -O -Xprint:typer

# 3. Get detailed error explanations
scala-cli compile step2d.scala -O -explain
```

Deliberately breaking compilation and reading the error messages is the core of this learning process.
Error messages are letters from the compiler.

## Using AI Assistants

As shown in the [Interlude](./interlude.md), AI coding assistants (Claude Code, Cursor, etc.)
are particularly useful for **type design exploration** — the phase where you don't yet know
which type-level tool to reach for. The workflow:

1. Write your initial attempt
2. Compile — read the error (or notice the limitation)
3. Share the code + compiler output with the AI: *"This doesn't express what I want. What are my options?"*
4. Try the suggested alternative — compile again
5. Repeat until the types express your intent

The compiler tells you *what's wrong*. The AI helps you see *what else you could try*.
The understanding comes from you connecting the two.
