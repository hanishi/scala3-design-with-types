# Step 1: 型パラメータは契約である

## 1-1. 自分だけの Box を作る

```scala
{{#include ../../../examples/step1/step1a.scala}}
```

**試してみよう：**
```
> scala-cli run step1a.scala
```

次に `val s: String = intBox.value` のコメントを外してみてほしい。

**ポイント：** `Box[A]` の `A` は中に入れたものを「覚えている」のではない。
コンパイラは構築時に `A = Int` と**確定**する。これが契約だ。
値を取り出すとき、それは `Int` であることが保証される。

## 1-2. 関数の型パラメータ ― 契約は伝播する

```scala
{{#include ../../../examples/step1/step1b.scala}}
```

**型を確認してみよう：**
```sh
scala-cli run step1b.scala

# 推論された型を見る（コンパイルキャッシュを回避するため先にクリーン）：
scala-cli clean step1b.scala && scala-cli compile step1b.scala -O -Xprint:typer 2>&1 | grep "mixed"
```

`mixed` は `List[Int | String | Double]` と推論される ― これはユニオン型で、Scala 3 の機能だ。

`List[Int | String | Double]` とは一度も書いていないことに注目してほしい ― コンパイラが**推論**したのだ。
本書を通じて、コンパイラが文脈から型パラメータを導き出す能力に頼る。
型推論が内部的にどう動くかはそれ自体が一つのテーマだが、ここでは単にそれが機能すると信頼し、
型が*何を意味するか*に集中する。
