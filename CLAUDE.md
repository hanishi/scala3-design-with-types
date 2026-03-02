# Project: Design with Types — Scala 3 Book

A free, open-source book teaching Scala 3's type system through compiler dialogue.
Published at https://hanishi.github.io/scala3-design-with-types/

## Project structure

```
book/           — EN mdbook source (outputs to docs/)
book-ja/        — JA mdbook source (outputs to docs/ja/)
examples/       — Scala source files included by {{#include}} in chapters
docs/           — Built HTML served by GitHub Pages (DO NOT edit directly)
```

## Branches

- **`book`** — primary working branch for all edits
- **`main`** — serves GitHub Pages; merge `book` into `main` to deploy

## Build process

**Order matters.** EN build wipes `docs/` clean, which deletes `docs/ja/` and OG images.

```sh
# Always build in this order:
cd book && mdbook build
cd ../book-ja && mdbook build

# Then restore OG images (they live outside mdbook's output):
# docs/og-image.png       — EN Open Graph image
# docs/ja/og-image.png    — JA Open Graph image
#
# The SVG sources are in /tmp/og-image.svg and /tmp/og-image-ja.svg
# (regenerate with rsvg-convert if needed)
```

## Deploy workflow

```sh
git push origin book
git checkout main && git merge book && git push origin main
git checkout book
```

## Writing conventions

### English
- Conversational, direct tone
- Compiler-first: every concept is introduced through a compiler error
- Pattern: show code → reader uncomments → compiler rejects → explain why → fix
- Comments in example files tell the story — keep them dense but clear

### Japanese
- **だ/である tone** throughout (NOT です/ます)
- Same conversational feel as English — direct, not textbook
- Example: `コンパイラは拒否する。` not `コンパイラは拒否します。`
- Code comments inside bash blocks (e.g., `# Scala 3.x が必要です`) are OK to leave polite

### Both languages
- Error messages in example files must match actual `scala-cli` compiler output
- Caret alignment (`^^^^^^^`) must match real compiler errors
- Each example file is self-contained — one file per concept
- Sections use `{{#include ../../../examples/stepN/file.scala}}` to embed code

## Current state (as of March 2026)

### Published chapters
- Step 0: Life Without Types
- Step 1: Type Parameters Are Contracts
- Step 2: Variance and Bounds (the largest chapter — covers invariance, covariance, contravariance, upper/lower bounds, ZIO example)
- Step 3: Variance in Practice (covariant/contravariant/invariant in real code)

### Planned chapters (Part 1 continued)
- Opaque types
- Type members
- Type classes

### Planned chapters (Part 2: Type-Level Programming)
- Match types
- Compile-time validation
- Type equality proofs

## Key design decisions

- **step2crossed.scala** demonstrates why `[-A >: Item]` is a code smell — uses a 4-level hierarchy (Item → Book → Comic/Novel) to make the contravariance flip visual
- **Variance pairings are fixed**: `+A` pairs with `>:` (lower bound), `-A` pairs with `<:` (upper bound). Crossing them is redundant.
- **The ZIO section** in Step 2 is a real-world example showing why the contravariant escape hatch (`[R1 <: R]`) exists — without it, `flatMap` can't be written for `Effect[-R, +A]`

## Example files

Run any example with:
```sh
scala-cli run examples/step2/step2a.scala
```

**Never compile a whole directory** — files intentionally redefine the same classes to show progression.
