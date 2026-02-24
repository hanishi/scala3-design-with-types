# Summary

```
Part 1: The Type System
  Step 0: Life without types       → Runtime errors lurking everywhere
  Step 1: Type parameters          → Contracts with the compiler
  Step 2: Variance & Bounds        → Invariant by default; covariant for producers,
                                     contravariant for consumers; bounds to bridge them
  Step 3: Opaque Types             → Zero-cost type distinctions
  Step 4: Type Members             → Types inside types; path-dependent safety
  Step 5: Givens / Typeclasses     → Evidence the compiler finds automatically

Part 2: Type-Level Programming
  Step 6: Match Types              → Output type computed from input type
  Step 7: Inline / compiletime     → Compile-time validation with custom errors
  Step 8: =:= Evidence             → Proving type equality; conditional capabilities

Interlude                          → Choosing the right tool through compiler conversation
```

The thread running through every step:

**"The more you tell the compiler, the more the compiler can protect you."**

Type parameters are not incantations.
They are a language for communicating your intent to the compiler,
and the basis on which the compiler verifies your code.
