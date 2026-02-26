# Step 2: 変位と境界

変位は暗記するルールの集まりではありません。
「なぜこれはコンパイルできないのか？」とコンパイラに問いかけることで体験するものです。
出発点は**不変（invariant）**（互換性なし）。これがデフォルトであることに意味があります。

## 2-1. 不変 ― デフォルトの壁

```scala
{{#include ../../../examples/step2/step2a.scala}}
```

**コメントを外してコンパイルしてください。**

コンパイラは `Box[Book]` と `Box[Item]` を**まったく別の型**として扱います。
`Book <: Item` だからといって `Box[Book] <: Box[Item]` とはなりません。

> **記法について：** `<:` は「〜のサブタイプ」、`>:` は「〜のスーパータイプ」を意味します。
> `Book <: Item` は「Book は Item のサブタイプ」、つまり `Book extends Item` です。
> これらの記号はこの章を通じて、文章中でも Scala コード（型境界）でも使われます。

コンパイラのデフォルト：*「安全だと証明してくれたら許可する。」*

## 2-2. なぜ不変がデフォルトなのか ― 壊して理解する

```scala
{{#include ../../../examples/step2/step2b.scala}}
```

これは思考実験ですが、Java では**実際に起きます**：

```java
{{#include ../../../examples/step2/Step2bJava.java}}
```

**試してみよう**（`javac` と `java` が必要）：
```sh
javac Step2bJava.java && java Step2bJava
```

警告なしでコンパイルされますが、実行時に `ArrayStoreException` でクラッシュします。

Scala の不変デフォルトは、**このクラスのバグをコンパイル時に排除する**という設計判断です。

## 2-3. 共変にする ― `+A` の意味

```scala
{{#include ../../../examples/step2/step2c.scala}}
```

`+A` を付けると、サブタイプ関係がコンテナを通じて持ち上がります：
`Book <: Item` ならば `Box[Book] <: Box[Item]`。

この `Box` は値を保持するだけです。中身を検査したり変更したりもしたい場合はどうでしょう？

## 2-4. コンパイラのチェックを試す

**ブロックを一つずつコメント解除してください。**

```scala
{{#include ../../../examples/step2/step2d.scala}}
```

不変の `Box[A]` では、`var value: A` も `contains(item: A)` も安全です。
`Box[Book]` は `Box[Item]` ではないからです。
サブタイプ関係で型が広がることがないので、互換性のない値が入り込む余地がありません。

`Box[+A]` を宣言すると、Scala は `Box[Book]` を `Box[Item]` として見ることを許可します。
その広い視点から見ると：
- `var value: A` は、実際には `Box[Book]` であるところに `DVD` を代入できてしまう
- `contains(DVD(...))` が有効に見える ― `DVD` は `Item` だから

しかし、基盤となるオブジェクトは `Box[Book]` のままです。型安全性を保つために、Scala は両方を拒否します：
- `var value: A` → *「不変位置（invariant position）」* ― `var` は読み書き両方
- `contains(item: A)` → *「反変位置（contravariant position）」* ― パラメータは値を消費する

共変型パラメータ（`+A`）は**出力**位置にのみ現れることができます：
- `val value: A`
- メソッドの戻り値型

> **なぜ `contains` は無害に見えるのに拒否されるのか？** Scala は変位ルールを構造的に強制します ―
> メソッドの実装は分析しません。
> `contains` はこの場合は無害でしょうが、`var value: A` のような他のメンバーは不健全です。
> ルールは統一的です：共変型パラメータ（`+A`）は入力位置に現れることができません。

では、共変を維持しつつ有用な入力を諦めずに済む方法は？
そこで**型境界**の出番です。

## 2-5. 下限境界 ― 変位制約からの脱出

2-4 で、`+A` は `A` を入力位置で拒否することを見ました。

`B >: A` は「`B` は `A` のスーパータイプ」― `B` は `A` またはその上の型でなければなりません。
これによって次のことが可能になります：`A` を直接受け取る（`+A` が禁止する）のではなく、
メソッドはコンパイラに「カートの中身と追加するものの両方に十分広い型 `B`」を見つけさせます。
カートの型は同じか広がるだけで、狭まることはありません。

```scala
{{#include ../../../examples/step2/step2e.scala}}
```

これはまさに `List[+A]` の `prepended` がやっていることです：

```scala
// List の簡略化された定義
sealed abstract class List[+A]:
  def prepended[B >: A](elem: B): List[B]
```

`List[Book]` に `DVD` を prepend すると `List[Item]` になります。型は嘘をつきません。

### 下限境界はどこで使われるか？

「下限境界は基本的に同じような場面で使われるのか？」と思うかもしれません。

はい ― いくつかの役割に集中しています：

**コンテナの拡大** ― 共変コレクションへの挿入：

```scala
val books: List[Book] = List(Book("Scala", 45.0))
val items: List[Item] = books :+ DVD("The Matrix", 19.99)
// List[Book] が List[Item] に拡大
```

**フォールバック値** ― `Option.getOrElse[B >: A](default: => B): B`：

```scala
val maybeBook: Option[Book] = None
val item: Item = maybeBook.getOrElse(DVD("The Matrix", 19.99))
// Option[Book] → Book がない → DVD にフォールバック → 結果は Item
```

**結合/マージ** ― `Either` の `orElse` は `Right` 型を拡大する：

```scala
val result: Either[String, Book] = Left("not found")
val recovered: Either[String, Item] = result.orElse(Right(DVD("The Matrix", 19.99)))
// Either[String, Book] → DVD で復旧 → Either[String, Item]
```

いずれの場合もパターンは同じです：`[B >: A]` は `A` にぴったり合わない値を受け入れるために型を拡大します。コンパイラは機能する最も狭い型を選びます ―
`Any` ではなく `Item`。これを**LUB**（Least Upper Bound、最小上界）と呼びます。
名前のついた共通のスーパータイプが*ない*場合、Scala 3 はユニオン型を使います。

日常のアプリケーションコードで `[B >: A]` を書くことはないでしょう ― しかし基盤となる API は
本当にこれに依存しています。下限境界がなければ、共変コンテナは「追加」メソッドを持てません。

一つ覚えておくこと：拡大は呼び出し側により精度の低い型を返すことを意味します。
`[B >: A]` は拡大が本当に必要なときに使いましょう。予防策としてではなく。

## 2-6. 上限境界 ― 最低限の振る舞いを要求する

下限境界が**拡大**するなら、上限境界は**制限**します。

```scala
{{#include ../../../examples/step2/step2f.scala}}
```

### 上限境界はどこで使われるか？

下限境界と同様に、いくつかの役割に集中しています：

**特定の振る舞いを要求** ― 特定のメソッドを持つ型のみ受け入れる：

```scala
def sortByShipping[A <: Shippable](items: List[A]): List[A] =
  items.sortBy(_.shippingCost)
// Shippable なアイテムのみ動作 — DigitalGiftCertificate はコンパイル時に拒否
```
`[A <: Shippable]` はコンパイラに「`Shippable` を拡張する型のみ受け入れよ」と伝えます。
関数内部では `_.shippingCost` を呼べます。コンパイラはすべての `A` が
そのメソッドを持つことを*知っている*からです。`DigitalGiftCertificate` は `Item` を拡張しますが
`Shippable` は拡張しません ― だからコンパイラはコードが実行される前に拒否します。

**クラスの型パラメータを制約** ― コンテナが適切な型のみ保持することを保証：

```scala
class Shipment[+A <: Shippable](items: List[A]):
  def totalShippingCost: Double = items.map(_.shippingCost).sum
  def add[B >: A <: Shippable](item: B): Shipment[B] = Shipment(items :+ item)
// Shipment[DigitalGiftCertificate] は作れない — Shippable ではない
```
`+` は 2-4 で見た共変と同じです ― 境界と一緒に機能します。`<: Shippable` は許可される型を制限し、`+` は
`Shipment[Book] <: Shipment[Shippable]` を可能にします。

`add` メソッドは `[B >: A <: Shippable]` を使います ― 2-5 で見た `[B >: A]` 脱出口に
`<: Shippable` を組み合わせてクラスの境界内に留まります。
`Shipment[Book]` に `DVD` を追加すると `Shipment[Shippable]` に拡大します。

> **なぜ二つの境界が必要なのか？**
>
> `B` がクラスから `<: Shippable` 境界を継承すると期待するかもしれません ―
> 結局 `A` はすでに制約されています。しかし `B` はメソッドで導入された
> 独自の型パラメータです。あなたが伝えたことしか知りません。
>
> そして `B >: A` は階層を*上に*向かいます。Scala ではすべての型の上に `AnyRef` と
> `Any` があるので、上限がなければ `B` はどこまでも拡大できます：
> `Book → Shippable → Item → AnyRef → Any`。`<: Shippable` は
> 「ここで止まれ」と言います。これがなければ `Shipment[B]` はコンパイルできません ―
> `Shipment` は型パラメータが `<: Shippable` であることを要求します。

**型情報の保持** ― 計算を通じて具体的なサブタイプを維持する：

```scala
def cheapest[A <: Item](items: List[A]): A =
  items.minBy(_.price)

val books: List[Book] = List(Book("Scala", 45.0), Book("FP", 35.0))
val b: Book = cheapest(books)  // Book を返す、Item ではなく

val dvds: List[DVD] = List(DVD("The Matrix", 19.99), DVD("Inception", 24.99))
val d: DVD = cheapest(dvds)  // DVD を返す、Item ではなく

val cards: List[DigitalGiftCertificate] = List(DigitalGiftCertificate("$50", 50.0))
val c: DigitalGiftCertificate = cheapest(cards)  // DigitalGiftCertificate を返す、Item ではなく
```

境界がなければ `def cheapest(items: List[Item]): Item` と書くしかなく、リストが `Book` や `DVD` を含んでいても
すべての呼び出しが `Item` を返します。振る舞いは保持できますが、具体的な戻り値型を失います。

`def cheapest[A](items: List[A]): A` とも書けません。`minBy(_.price)` は
各要素が `.price` メンバーを持つことを要求するからです。境界がなければ、
コンパイラには `A` がそれを持つと信じる理由がありません。

上限境界は両方を実現します：`Item` の API へのアクセスと、正確な要素型の返却
（`Book` を入れれば `Book` が出る、`DVD` を入れれば `DVD` が出る）。言い換えれば、`<: Item` は
呼び出し側を制限するためではなく、どの操作が有効かをコンパイラに伝えつつ
結果型を最大限に具体的に保つためのものです。

**標準ライブラリ** ― `WeakReference[+T <: AnyRef]` は参照型のみ受け入れます：

```scala
import scala.ref.WeakReference
val ref = WeakReference(List(1, 2, 3))  // OK: List は AnyRef
// WeakReference(42) — 動きません: Int は AnyRef ではない
```

通常の参照（`val x = ...`）はオブジェクトを生かし続けます ― 誰かが指している限り
ガベージコレクタは回収しません。`WeakReference` は違います：GC による回収を
妨げずに参照を保持します。メモリが逼迫すると GC はオブジェクトを回収でき、
弱参照は静かに空になります。

最も一般的な用途はキャッシュです。計算結果を再利用のために保持したいが、
メモリ不足のコストは払いたくない：

```scala
import scala.ref.WeakReference

class Cache[T <: AnyRef](compute: () => T):
  private var ref: WeakReference[T] = WeakReference(null)

  def get(): T = ref.get match
    case Some(value) => value        // まだメモリにある、再利用
    case None =>                     // GC が回収した、再計算
      val value = compute()
      ref = WeakReference(value)
      value

val users = Cache(() => List("alice", "bob", "charlie"))
users.get()  // 初回は計算、GC が回収していなければ再利用
```

これはヒープ割り当てオブジェクト（`AnyRef`）でのみ意味があります。`Int` や `Double` のような
値型はスタックに直接格納されるか、オブジェクトにインライン化されます ― GC が追跡や
回収するヒープオブジェクトがありません。上限境界 `T <: AnyRef` はこの JVM の現実を
型レベルでエンコードします：ヒープ上に存在しないものへの弱参照は文字通り*作れません*。

これが高度に感じても心配不要です ― この例は安全にスキップして後で戻ってきてください。
上限境界の巧妙な使い方であり、続きの前提条件ではありません。

下限境界は*拡大*します ― より広い型を受け入れます。上限境界は*制限*します ―
必要な振る舞いを持たない型を拒否します。

> **出力側の全ツールキットが揃いました：** 安全な拡大のための共変（`+A`）、
> 共変を壊さずにメソッドを追加するための下限境界（`>:`）、
> 特定の振る舞いを要求するための上限境界（`<:`）。次は入力側です。

## 2-7. 反変 ― 消費者の互換性

最後に `-A`。共変は**出力**側 ― プロデューサーの話でした。
反変は**入力**側 ― コンシューマーの話です。

```scala
{{#include ../../../examples/step2/step2g.scala}}
```

多くのチュートリアルは反変を
`val bookFmt: Formatter[Book] = itemFormatter` のような明示的な代入で説明します ―
上の例のように。サブタイプ関係が見えますが、こんなコードはめったに書きません。
あなたが反変を*使っている* ― 気づかずに ― のは、スーパータイプの関数を渡す
`.filter`、`.map`、`.sortBy` の呼び出しすべてです。

`Function1[-A, +B]` ― `A => B` のデシュガー形式 ― は
標準ライブラリで最も一般的な反変型です。

`A` は**入力位置**にあります ―
関数はそれを*受け取り*ます ― だから反変（`-A`）です。

`B` は**出力位置**にあります ―
関数はそれを*返し*ます ― だから共変（`+B`）です。

だから `Item => Boolean` は `Book => Boolean` が期待される場所で使えます：

```scala
{{#include ../../../examples/step2/step2g1.scala}}
```

パターン：型が `A` を**消費する**（入力として受け取る）なら、`-A` の候補です。
`A` を**生産する**（出力として返す）なら、`+A` の候補です。

なぜ方向が逆転するのか？ `val cheap: Item => Boolean = _.price < 40.0` を見てください。
`Item` のすべてのサブタイプ ― `Book`、`DVD`、`DigitalGiftCertificate` ― は `.price` を持ちます。
だから `Item` のプロパティだけを使う関数は、どれに対しても自然に動作します。
一度書けば `List[Book]`、`List[DVD]`、`List[Item]` で再利用できます。

反変が実践で意味すること：**より広い型のコンシューマーは、
より狭い型のコンシューマーの安全な代替品です。**

最初の例の `PriceFormatter` は同じアイデアを明示的にしたものです。
`ItemFormatter` は `name` と `price` だけを使います ― すべての `Item` が持つものです。だから
`Book` でも `DVD` でも何でも扱えます。`BookFormatter` は `isbn` を使います ―
`Book` だけが持つものです。`DVD` を渡すと壊れます。だから逆転：
`Book <: Item`、しかし `PriceFormatter[Item] <: PriceFormatter[Book]`。

### 反変の場合の境界は？

共変では、`+A` は入力位置（メソッドパラメータ ― メソッドが値を*受け取る*場所）で `A` を禁止します。
下限境界（`[B >: A]`）が脱出口でした（2-5 参照）。

対称的に、`-A` は出力位置（戻り値型 ― メソッドが値を*生産する*場所）で `A` を禁止します。
上限境界（`[B <: A]`）が脱出口になります。

`PriceFormatter[-A]` や `Function1[-A, +B]` のような単純なコンシューマーでは、
これは問題になりません ― `String` や `Boolean` を返し、`A` は返さないからです。

しかし、反変型を合成するときには問題になります。ここに [ZIO](https://zio.dev/)
（並行・非同期プログラム構築のための人気 Scala ライブラリ）に着想を得た簡略版を示します。

ZIO の実際の型は `ZIO[-R, +E, +A]`（環境、エラー、値）です ― ここでは変位に集中するためエラーチャネルを省きます。

`Effect[-R, +A]` は環境 `R` を必要として実行し、値 `A` を生産する計算です。
内部的には関数 `R => A` をラップしています：

```scala
class Effect[-R, +A](val run: R => A)
```

変位アノテーションを見てください。`+A` は出力にあります ― `run` はそれを*返し*ます。
`-R` は入力にあります ― `run` はそれを*受け取り*ます。実際、`R => A` は `Function1[R, A]` であり、
`R` は `Function1` の `-A` スロットにあります。これは反変位置なので、
クラスをコンパイルするには `R` を `-R` と宣言する必要があります。

これは `Function1[-A, +B]` と同じ変位の形です ― `Effect` は本質的に `R => A` のラッパーなので当然です。

以下の例では二つのトレイト ― `Database` と `Logger` ― と
両方を拡張するクラス `AppEnv` を定義します：

```
Database          Logger
    ↑                ↑
    └── AppEnv ──────┘
```

`AppEnv <: Database` かつ `AppEnv <: Logger`。`Database` だけを必要とするエフェクトは
`AppEnv` が利用可能な場所で実行できます ― `AppEnv` はエフェクトが必要とするものをすべて持っています。
これが反変の実際の動作です：

```scala
{{#include ../../../examples/step2/step2g2.scala}}
```

`List[+A]` と `Effect[-R, +A]` の `flatMap` を比較しましょう。`List` では `A` は
クラスの `+A` から来ます ― そして境界なしで正しい位置に収まります。
`Effect` では `R` はクラスの `-R` から来ます ― しかし `Function1` 内部の
`Effect[R, B]` の追加のネストが `R` の着地位置を変え、境界が必要になります。

これをたどるには、コンパイラの判定方法を理解する必要があります。各型パラメータが
**入力**位置か**出力**位置に着地するかをチェックします：

- メソッドパラメータは**入力** ― メソッドはそれを*受け取る* → 位置は `(-)`
- 戻り値型は**出力** ― メソッドはそれを*生産する* → 位置は `(+)`
- `Effect[-R, +A]` では：`+A` は `(+)` 位置に、`-R` は `(-)` 位置に収まる必要がある。

型がネストされると、位置は算術の符号のように掛け合わされます：

```
(+) × (+) = (+)     出力の中の出力 → そのまま出力
(+) × (-) = (-)     入力の中の出力 → 入力に反転
(-) × (-) = (+)     入力の中の入力 → 出力に戻る
```

**`List[+A]` の flatMap ― 境界不要：**

デシュガー：`flatMap[B](f: Function1[A, IterableOnce[B]]): List[B]`

| ステップ | A はどこ？ | 位置 |
|------|-------------|----------|
| `f` はメソッドパラメータ | 開始 | `(-)` |
| `A` は `Function1` の `-A` スロット | `(-) × (-)` | `(+)` |

結果：`A` は `(+)` に着地。`+A` と宣言した。**OK。**

**`Effect[-R, +A]` の flatMap ― 境界なしで試したら？**

デシュガー（境界なし）：`flatMap[B](f: Function1[A, Effect[R, B]]): Effect[R, B]`

| ステップ | R はどこ？ | 位置 |
|------|-------------|----------|
| `f` はメソッドパラメータ | 開始 | `(-)` |
| `Effect[R, B]` は `Function1` の `+B` スロット | `(-) × (+)` | `(-)` |
| `R` は `Effect` の `-R` スロット | `(-) × (-)` | `(+)` |

結果：`R` は `(+)` に着地。`-R` と宣言した。**拒否。**

修正：

```scala
// 読みやすさのため => をデシュガー
def flatMap[R1 <: R, B](f: Function1[A, Effect[R1, B]]): Effect[R1, B]
```

`R1 <: R` は `R1` が `R` またはそのサブタイプであることを意味します ― `R` が持つものをすべて持っています。

変位ルール（`+` と `-`）はクラス自身の型パラメータにのみ適用されます。
`R1` は*メソッド*の型パラメータなので、変位チェックの対象外です ―
コンパイラは呼び出し側で `R1 <: R` が成立するかだけをチェックします。

`R1 <: R` は型チェッカーだけでなく、実装も動作させます。

`Effect(r => f(run(r)).run(r))` を名前付きステップに分解し ―
`=>` の代わりに `Function1` を明示的に書いて ― 実装の動作と各型パラメータの着地位置の両方を見てみましょう：

```scala
//                                       つまり R => A
class Effect[-R, +A](val run: Function1[R, A]):
//                                     つまり A => Effect[R1, B]
  def flatMap[R1 <: R, B](f: Function1[A, Effect[R1, B]]): Effect[R1, B] =
    Effect { r =>              // r: R1
      val a = run(r)           // このエフェクトを実行 — R1 <: R なので OK
      val effect2 = f(a)       // 次のエフェクトを構築
      effect2.run(r)           // 同じ環境で実行
    }
```

`A` ― `run` が生産した値 ― は `f` に渡されます。
`f: Function1[A, Effect[R1, B]]` を見てください：`A` は `Function1` の `-A` スロットにあります。
クラスの `+A` は反変位置の中の反変位置に収まります
― `(-) × (-) = (+)` ― コンパイラは満足します。

同じ `r` が両方のエフェクトに供給されます ― だから `R1 <: R` は
変位チェッカーだけでなく実装レベルでも重要なのです。

`R` は反変なので、`Database`（`R`）だけを必要とするエフェクトは
`AppEnv`（`R1`）が利用可能な場所で実行できます ― `AppEnv <: Database` なので、
エフェクトが必要とするものをすべて持っています。だから `fetchUser.run(AppEnv())` が動作します：
`fetchUser` は `Database` だけを必要とし、`AppEnv` はそれを満たします。

## 2-8. クイックリファレンス

たくさんの要素が出てきました。この章のすべてをまとめます：

```
不変（デフォルト）     → 互換性を仮定しない
+A（共変）            → 出力側 — 安全に拡大
-A（反変）            → 入力側 — 逆方向で安全
>:（下限境界）         → 共変型の脱出口（拡大）
<:（上限境界）         → 反変型の脱出口（制限）
```

しかし仕組みを知るのは話の半分です。次の問いは：
*実際にどのくらい不変が欲しくて、いつ `+` や `-` を使うべきなのか？*

> **一息つきましょう。** 難しい概念的な作業は終わりました。セクション 2-9 から 2-11 は
> 変位が実際のコードでどう働くかを示します ― より短く、より具体的です。
