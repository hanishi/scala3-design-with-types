# Design with Types — Practical Scala 3 from Basics to Type-Level Programming

[日本語版はこちら](ja/introduction.html)

> This tutorial is designed to be worked through with `scala-cli`,
> having a conversation with the compiler at every step.
> The most important part of each step: **compile first, read the error, then understand.**

## Who This Is For

You use type parameters — `List[Int]`, `Option[String]`, `Map[K, V]` — every day.
But `[+A]`, `[B >: A]`, `match` types, `=:=`? Those feel like a different language.
You've been told they're "advanced" and moved on. That's completely reasonable.

This tutorial argues they're not advanced — they click once you see the problems they solve.
The concepts build on each other in a straight line, and every one of them addresses
a concrete problem you've already encountered.

## Why This Matters

Here's the fundamental tension: on the JVM, generics are **erased**.
At runtime, `List[Int]` and `List[String]` are both just `List`.
The JVM doesn't know what types your collections hold.

That means **compile time is your only chance** to catch type errors.
Everything in this tutorial — variance, opaque types, typeclasses, match types —
is about giving the compiler more information so it can catch more mistakes
*before* erasure throws that information away.

This matters even more now. In an era where LLMs write more code than humans
type, the type system becomes the critical layer between human intent and
generated code. A rich type system with explicit compiler messages catches
what LLMs silently drop — an implicit convention, a missing annotation, a
subtle variance requirement. The compiler doesn't forget, and it doesn't
hallucinate. It's the one reviewer that checks every line, every time.

## Structure

**Part 1: The Type System** (Steps 0–5) covers how to *use* Scala's type system
effectively — type parameters, variance, bounds, opaque types, type members, and typeclasses.
This is what most production Scala code needs.

**Part 2: Type-Level Programming** (Steps 6–8) covers types that *compute* —
match types, compile-time validation, and type equality proofs.
These techniques show up in library design and domain modeling where
the compiler can catch entire classes of bugs that the type system alone can't.
