# Interlude: Exploring Type Design with a Compiler Conversation

The previous steps taught specific type-level features one at a time. But in practice,
the hard part isn't knowing what `+A` or `<:` means — it's knowing **which tool to reach for**
when you're designing something new.

This is where pairing the Scala compiler with an AI coding assistant becomes genuinely useful.
Not as a crutch, but as a way to **iterate faster through design alternatives**.

Here's a real scenario: you're building an order summary system and need a function
that formats different product types for invoices. You know several approaches exist,
but which one fits best?

## The Problem

```scala
{{#include ../../examples/interlude/explore.scala}}
```

## Attempt 1: Upper Bound

Your first instinct might be a shared trait:

```scala
{{#include ../../examples/interlude/explore1.scala}}
```

**Compile and observe:** It works, but `invoiceLine` can only use `price`.
The bound gives you access to the *shared* interface, nothing more.

```
> scala-cli compile explore1.scala
> # It compiles, but the function is too generic to be useful.
```

*If you're working with an AI assistant, this is a good moment to share the code and ask:*
*"This compiles but I can't access type-specific fields. What are my options?"*

## Attempt 2: Typeclass

The AI (or your own experience) might suggest a typeclass:

```scala
{{#include ../../examples/interlude/explore2.scala}}
```

This works well. Each type gets its own formatting logic. New types can be added without
modifying existing code. The compiler ensures every type passed to `invoiceLine` has an
`InvoiceLine` instance.

## Attempt 3: Match Type (Spoiler — Wrong Tool)

What if you tried a match type instead?

```scala
{{#include ../../examples/interlude/explore3.scala}}
```

**Compile and think about it.** A match type computes a *different output type* based on input type.
When the output is always the same type (`String`), there's no type-level computation to do.

*This is exactly the kind of dead end that's worth hitting.* It teaches you when NOT to use a tool.

## The Decision Framework

After exploring these attempts, here's the pattern:

```
"I need access to shared fields"
  → Upper bound (A <: Trait)

"I need different behavior per type, output type is the same"
  → Typeclass (given/using)

"I need the output TYPE to change based on input type"
  → Match type

"I need to reject invalid values at compile time"
  → inline + compiletime.error

"I need a method that only exists when types satisfy a condition"
  → =:= or <:< evidence

"I need a type that's an internal detail, not visible to callers"
  → Type member

"I need to prevent mixing values from different instances"
  → Path-dependent type (type member)
```

This framework doesn't come from reading documentation. It comes from
**trying the wrong tool, reading the compiler's response, and understanding why it didn't fit.**

An AI assistant accelerates this loop: you share compiler output, describe your intent,
and iterate through alternatives in minutes instead of hours. But the understanding
comes from you reading the compiler's feedback at each step — the AI just helps you
navigate faster.
