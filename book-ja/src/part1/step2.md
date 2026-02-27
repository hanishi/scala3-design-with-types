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

Java の*ジェネリクス*は不変です ― `List<Book>` は `List<Item>` ではなく、
コンパイラが代入を拒否します。例のジェネリクスセクションのコメントを外して確認してください。
配列はより早くに設計されており共変です ― 実行時クラッシュの原因はそこにあります。

Scala の不変デフォルトは配列を含むすべてに適用され、**このクラスのバグをコンパイル時に排除**します。

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
コンパイラがこう言っていると考えてください：
*「`+A` と宣言したので、`A` を入力として受け取ることは許可できません ― 共変が壊れます。
しかし `A` のスーパータイプである `B` を示してくれれば、既にある中身と追加するものの
両方に合う最も狭い型を見つけます。
既に合っていれば `B` は `A` のままで何も変わりません。そうでなければカートは
広がります ― しかし狭まることはありません。」*

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

なぜ拡大は安全なのか？ コンパイラがこう言っていると考えてください：
*「許可します ― すべての要素は少なくとも `Item` のメソッドを持っているので、
操作は安全です。ただし、中身が具体的に何かはもう約束できません。
`Item` に拡大するよう求めたので、`Item` だけを保証します。」*

型を混ぜる能力を得る代わりに、特定の型を仮定する権利を失います。
`[B >: A]` はそのトレードオフが本当に必要なときに使いましょう。予防策としてではなく。

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

<details>
<summary><strong>Java から来た方へ</strong></summary>

Java では `List<Book>` を `List<Item>` として受け取るために、呼び出し側が毎回 `? extends` を書く必要があります：

```java
{{#include ../../../examples/step2/Step2kCov.java}}
```

**試してみよう：** `javac Step2kCov.java && java Step2kCov`

Scala の `List[+A]` は共変を一度だけ宣言します ― `cheapest(books)` はワイルドカードなしでそのまま動きます。

</details>

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

<details>
<summary><strong>Java から来た方へ</strong></summary>

Java の `Predicate<Item>` が `List<Book>` をフィルタできるのは、`filter()` が
`Predicate<? super T>` を受け取るからです ― 使用側反変：

```java
{{#include ../../../examples/step2/Step2kCon.java}}
```

**試してみよう：** `javac Step2kCon.java && java Step2kCon`

Scala の `Function1[-A, +B]` は反変を一度だけ宣言します ― `books.filter(cheap)` はそのまま動きます。

</details>

### 反変の場合の境界は？

共変では、コンパイラはこう言いました：*「`+A` と宣言したので、`A` を入力として
受け取ることは許可できません。」* 下限境界（`[B >: A]`）が脱出口でした ― 上に向かって拡大。

鏡像も存在します：`-A` は `A` を出力位置で禁止し、上限境界（`[B <: A]`）が
脱出口です ― 下に向かって制限。対応関係は固定されています：

| 変位 | 問題 | 脱出口 | 方向 |
|---|---|---|---|
| `+A` 共変 | `A` が入力に現れない | `[B >: A]` 下限境界 | 上に拡大 |
| `-A` 反変 | `A` が出力に現れない | `[B <: A]` 上限境界 | 下に制限 |

下限境界は共変の問題を解決します。上限境界は反変の問題を解決します。
交差しません ― 共変は拡大するので脱出口も拡大、反変は制限するので脱出口も制限。

`PriceFormatter[-A]` や `Function1[-A, +B]` のような単純なコンシューマーでは、
反変の脱出口は問題になりません ― `String` や `Boolean` を返し、`A` は返さないからです。

しかし、反変型を合成するときには問題になります ― 実際の例で見るのが
脱出口の存在意義を理解する最良の方法です。

### 実例：ZIO

[ZIO](https://zio.dev/)（最も人気のある Scala ライブラリの一つ）は依存関係管理に
興味深いアプローチを取ります：依存関係（データベース接続、ロガー）を関数に直接渡す
代わりに、*必要なもの*を型で記述します。プログラムは一つの値になります：
*「Database があれば実行でき、String を生産する。」*
コンパイラはプログラムの実行前にすべての依存関係が満たされていることを検証します
― 型レベルの依存性注入です。

ZIO はこれを `ZIO[-R, +E, +A]` としてエンコードします：

- **`-R`**（環境） ― プログラムが実行に*必要とする*もの。ここまで議論してきた
  とおり反変。
- **`+E`**（エラー） ― プログラムがどう*失敗しうる*か。共変 ―
  `DatabaseError` を処理する関数は、`NetworkError` で失敗するコードと合成しても
  動作し、`DatabaseError | NetworkError` に拡大します。
- **`+A`**（値） ― 成功時にプログラムが*生産する*もの。共変。

型付きエラーチャネルは ZIO がプロダクションで有用な大きな理由です：
`Exception` を catch して最善を祈る代わりに、コンパイラが各操作がどのエラーを
生産しうるかを正確に教え、処理を強制します。変位がこれを合成可能にします：
異なる失敗をする二つの操作を組み合わせると、エラー型のユニオンが得られます。

簡略版 `Effect[-R, +A]` を作ります ― 変位と上限境界に集中するためエラーチャネルを
省きます。私たちの `Effect` は反変の脱出口がなぜ必要かを見るための教材にすぎません：
`R1 <: R` なしでは `flatMap` が書けず、`flatMap` なしではエフェクトを合成できません。

`Effect[-R, +A]` は環境 `R` を必要として実行し、値 `A` を生産する計算です。
内部的には関数 `R => A` をラップしています：

```scala
class Effect[-R, +A](val run: R => A)
```

`R => A` は `Function1[R, A]` であり、`R` は `-A` スロットに、`A` は `+B`
スロットにあります。つまり `Effect[-R, +A]` は `Function1[-A, +B]` と同じ変位の
形です ― ただのラッパーなので当然です。

`Effect[-R, +A]` が実際にどう動くか見るために、異なる機能を表す2つのトレイトと、
両方を提供する `AppEnv` を定義しましょう：

```scala
trait Database:
  def lookup(id: Int): String

trait Logger:
  def log(msg: String): Unit

class AppEnv extends Database, Logger:
  def lookup(id: Int) = s"user-$id"
  def log(msg: String) = println(s"  LOG: $msg")
```

```
Database          Logger
    ↑                ↑
    └── AppEnv ──────┘
```

`Database` を受け取り `String` を返すエフェクトが欲しい ― そこで
`db => db.lookup(1)` を渡すと、型は `Effect[Database, String]` になります。
`logMsg` は `String` を受け取り、`Logger` を必要とするエフェクトを返します ―
メッセージをクロージャで閉じ込めます：

```scala
val fetchUser: Effect[Database, String] = Effect(db => db.lookup(1))
val logMsg: String => Effect[Logger, Unit] = msg => Effect(logger => logger.log(msg))
```

for 内包表記で合成すると、`Database & Logger` を必要とするエフェクトになります
― 環境を渡して実行します：

```scala
val program: Effect[Database & Logger, Unit] =
  for
    user <- fetchUser
    _    <- logMsg(s"fetched $user")
  yield ()

program.run(AppEnv())  // LOG: fetched user-1
```

for 内包表記は `flatMap` と `map` にデシュガーされます ― しかし `flatMap` の
定義こそ、変位が立ちはだかるところです。

### 問題：`flatMap` と `-R`

直感的には、`flatMap` はクラスの `R` をそのまま使うはずです ―
現在のエフェクトも次のエフェクトも同じ環境で：

```scala
def flatMap[B](f: A => Effect[R, B]): Effect[R, B]
```

しかしコンパイラはこれを拒否します。理由を見るには、コンパイラが**位置**をどう
読むかを知る必要があります：

- メソッドパラメータは**入力** ― 位置は `(-)`
- 戻り値型は**出力** ― 位置は `(+)`
- 型がネストされると、位置は符号のように掛け合わされます：

```
(+) × (+) = (+)     出力の中の出力 → そのまま出力
(+) × (-) = (-)     入力の中の出力 → 入力に反転
(-) × (-) = (+)     入力の中の入力 → 出力に戻る
```

シグネチャをデシュガー：`flatMap[B](f: Function1[A, Effect[R, B]]): Effect[R, B]`

| ステップ | R はどこ？ | 位置 |
|------|-------------|----------|
| `f` はメソッドパラメータ | 開始 | `(-)` |
| `Effect[R, B]` は `Function1` の `+B` スロット | `(-) × (+)` | `(-)` |
| `R` は `Effect` の `-R` スロット | `(-) × (-)` | `(+)` |

結果：`R` は `(+)` に着地。`-R` と宣言した。**拒否。**

修正：メソッドに新しい型パラメータ `R1 <: R` を導入します。
変位ルール（`+` と `-`）は*クラス自身の*型パラメータにのみ適用されます。
`R1` は*メソッド*の型パラメータなので、変位チェッカーはまったく追跡しません。
どの位置にも現れることができます。コンパイラがチェックするのは、呼び出し側で
`R1 <: R` が成立するかだけです。

```scala
def flatMap[R1 <: R, B](f: A => Effect[R1, B]): Effect[R1, B] =
    Effect(r => f(run(r)).run(r))
```

<details>
<summary><strong>比較：なぜ List の flatMap は境界なしで動くのか？</strong></summary>

デシュガー：`flatMap[B](f: Function1[A, IterableOnce[B]]): List[B]`

| ステップ | A はどこ？ | 位置 |
|------|-------------|----------|
| `f` はメソッドパラメータ | 開始 | `(-)` |
| `A` は `Function1` の `-A` スロット | `(-) × (-)` | `(+)` |

結果：`A` は `(+)` に着地。`+A` と宣言した。**OK。** ネストの追加層がないので
位置は反転せず、境界は不要です。

</details>

### 実装の仕組み

`R1 <: R` は変位チェッカーのためだけではありません ― 実装も動作させます。
`Effect(r => f(run(r)).run(r))` を名前付きステップに分解し、
`Function1[-A, +B]` を明示的に書いて各型パラメータの着地位置を見てみましょう：

```scala
//                               つまり R => A
class Effect[-R, +A](val run: Function1[R, A]):
//                              つまり A => Effect[R1, B]
  def flatMap[R1 <: R, B](f: Function1[A, Effect[R1, B]]): Effect[R1, B] =
    Effect { r =>              // r: R1 — 戻り値型が Effect[R1, B] だから
      val a = run(r)           // このエフェクトを実行 — R1 <: R なので OK
      val effect2 = f(a)       // 次のエフェクトを構築
      effect2.run(r)           // 同じ環境で実行
    }
```

なぜ `r` は `R` ではなく `R1` なのか？ `flatMap` は `Effect[R1, B]` を返すからです。
これは関数 `R1 => B` をラップしています。だからその関数に渡される `r` は型 `R1` です。
そして `R1 <: R` なので、同じ `r` が `run(r)`（`R` を期待）と
`effect2.run(r)`（`R1` を期待）の両方に使えます。
これは反変のパターンです：`run` は `R` を期待し、`R1` は `R` が持つものをすべて
持っています ― より広い型のコンシューマーはより狭い型で動作します。

`scala-cli run step2g2.scala` で完全な例を実行できます：

```scala
{{#include ../../../examples/step2/step2g2.scala}}
```

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
