# Step 2: 変位と境界

変位はルールを暗記するものじゃない。
「なぜコンパイルが通らない？」をコンパイラと一緒に繰り返すうちに身につく。
出発点は**不変（invariant）**― 互換性なし。デフォルトが不変であること自体に意味がある。

## 2-1. 不変 ― デフォルトの壁

```scala
{{#include ../../../examples/step2/step2a.scala}}
```

**コメントを外してコンパイルしてみよう。**

コンパイラは `Box[Book]` と `Box[Item]` を**まったく別の型**として扱う。
`Book <: Item` だからといって `Box[Book] <: Box[Item]` にはならない。

> **記法：** `<:` は「〜のサブタイプ」、`>:` は「〜のスーパータイプ」。
> `Book <: Item` は「Book は Item のサブタイプ」― つまり `Book extends Item`。
> この記号はこの章を通じて、文章でも Scala コード（型境界）でも使う。

コンパイラのデフォルト：*「安全だと示してくれ。そうすれば通す。」*

## 2-2. なぜ不変がデフォルトなのか ― 壊して理解する

```scala
{{#include ../../../examples/step2/step2b.scala}}
```

これは思考実験だが、Java では**実際に起きる**：

```java
{{#include ../../../examples/step2/Step2bJava.java}}
```

**試してみよう**（`javac` と `java` が必要）：
```sh
javac Step2bJava.java && java Step2bJava
```

警告なしでコンパイルされるが、実行時に `ArrayStoreException` でクラッシュする。

Java の*ジェネリクス*は不変だ ― `List<Book>` は `List<Item>` ではなく、
コンパイラが代入を拒否する。例のジェネリクスセクションのコメントを外して確認してほしい。
配列はより早くに設計されており共変 ― 実行時クラッシュの原因はここにある。

Scala の不変デフォルトは配列を含むすべてに適用され、**このクラスのバグをコンパイル時に潰す**。

## 2-3. 共変にする ― `+A` の意味

```scala
{{#include ../../../examples/step2/step2c.scala}}
```

`+A` を付けると、サブタイプ関係がコンテナを貫通する：
`Book <: Item` なら `Box[Book] <: Box[Item]`。

この `Box` は値を保持するだけだ。中身を検査したり変更もしたくなったら？

## 2-4. コンパイラのチェックを試す

**ブロックを一つずつコメント解除してみよう。**

```scala
{{#include ../../../examples/step2/step2d.scala}}
```

不変の `Box[A]` では `var value: A` も `contains(item: A)` も安全だ。
`Box[Book]` は `Box[Item]` ではないから、
サブタイプ関係で型が広がることがなく、互換性のない値が入り込む余地がない。

`Box[+A]` と宣言すると、Scala は `Box[Book]` を `Box[Item]` として見ることを許す。
その広い視点から見ると：
- `var value: A` ― 実際には `Box[Book]` なのに `DVD` を代入できてしまう
- `contains(DVD(...))` が有効に見える ― `DVD` は `Item` だから

だが実体は `Box[Book]` のままだ。型安全性を守るため、Scala は両方を拒否する：
- `var value: A` → *「不変位置（invariant position）」* ― `var` は読み書き両方
- `contains(item: A)` → *「反変位置（contravariant position）」* ― パラメータは値を消費する

共変型パラメータ（`+A`）は**出力**位置にしか現れられない：
- `val value: A`
- メソッドの戻り値型

> **なぜ `contains` は無害に見えるのに拒否される？** Scala は変位ルールを構造的に強制する ―
> メソッドの実装は分析しない。
> `contains` はこの場合は無害だろうが、`var value: A` のようなメンバーは不健全だ。
> ルールは統一的：共変型パラメータ（`+A`）は入力位置に現れられない。

では、共変を維持しつつ有用な入力を諦めずに済む方法は？
**型境界**の出番だ。

## 2-5. 下限境界 ― 変位制約からの脱出

2-4 で、`+A` は `A` を入力位置で拒否することを見た。

`B >: A` は「`B` は `A` のスーパータイプ」― `B` は `A` かその上の型でなければならない。
コンパイラはこう言っている：
*「`+A` と宣言したから、`A` を入力として受け取ることは許可できない ― 共変が壊れる。
だが `A` のスーパータイプ `B` を示してくれれば、既にある中身と追加するものの
両方に合う最も狭い型を見つける。
既に合っていれば `B` は `A` のままで何も変わらない。そうでなければカートは
広がる ― 狭まることはない。」*

```scala
{{#include ../../../examples/step2/step2e.scala}}
```

これはまさに `List[+A]` の `prepended` がやっていることだ：

```scala
// List の簡略化された定義
sealed abstract class List[+A]:
  def prepended[B >: A](elem: B): List[B]
```

`List[Book]` に `DVD` を prepend すると `List[Item]` になる。型は嘘をつかない。

### 下限境界はどこで使われるか？

「下限境界は基本的に同じような場面で使われるのか？」と思うかもしれない。

そうだ ― いくつかの役割に集中している：

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

いずれの場合もパターンは同じだ：`[B >: A]` は `A` にぴったり合わない値を受け入れるために型を拡大する。コンパイラは機能する最も狭い型を選ぶ ―
`Any` ではなく `Item`。これを**LUB**（Least Upper Bound、最小上界）と呼ぶ。
名前のついた共通のスーパータイプが*ない*場合、Scala 3 はユニオン型を使う。

日常のアプリケーションコードで `[B >: A]` を書くことはないだろう ― だが基盤の API は
本当にこれに依存している。下限境界がなければ、共変コンテナは「追加」メソッドを持てない。

なぜ拡大は安全か？ コンパイラはこう言っている：
*「許可する ― すべての要素は少なくとも `Item` のメソッドを持っているから、
操作は安全だ。ただし、中身が具体的に何かはもう約束できない。
`Item` に拡大するよう求めたから、`Item` だけを保証する。」*

型を混ぜる能力を得る代わりに、特定の型を仮定する権利を失う。
`[B >: A]` はそのトレードオフが本当に必要なときに使おう。予防策としてではなく。

## 2-6. 上限境界 ― 最低限の振る舞いを要求する

下限境界が**拡大**するなら、上限境界は**制限**する。

```scala
{{#include ../../../examples/step2/step2f.scala}}
```

### 上限境界はどこで使われるか？

下限境界と同様に、いくつかの役割に集中している：

**特定の振る舞いを要求** ― 特定のメソッドを持つ型のみ受け入れる：

```scala
def sortByShipping[A <: Shippable](items: List[A]): List[A] =
  items.sortBy(_.shippingCost)
// Shippable なアイテムのみ動作 — DigitalGiftCertificate はコンパイル時に拒否
```
`[A <: Shippable]` はコンパイラに「`Shippable` を拡張する型だけ受け入れろ」と伝える。
関数内部で `_.shippingCost` を呼べるのは、コンパイラがすべての `A` に
そのメソッドがあると*知っている*からだ。`DigitalGiftCertificate` は `Item` を拡張するが
`Shippable` は拡張しない ― だからコンパイラはコード実行前に拒否する。

**クラスの型パラメータを制約** ― コンテナが適切な型のみ保持することを保証：

```scala
class Shipment[+A <: Shippable](items: List[A]):
  def totalShippingCost: Double = items.map(_.shippingCost).sum
  def add[B >: A <: Shippable](item: B): Shipment[B] = Shipment(items :+ item)
// Shipment[DigitalGiftCertificate] は作れない — Shippable ではない
```
`+` は 2-4 で見た共変と同じだ ― 境界と一緒に機能する。`<: Shippable` が許可される型を制限し、`+` が
`Shipment[Book] <: Shipment[Shippable]` を可能にする。

`add` メソッドは `[B >: A <: Shippable]` を使う ― 2-5 で見た `[B >: A]` 脱出口に
`<: Shippable` を組み合わせてクラスの境界内に留まる。
`Shipment[Book]` に `DVD` を追加すると `Shipment[Shippable]` に拡大する。

> **なぜ二つの境界が必要なのか？**
>
> `B` がクラスから `<: Shippable` 境界を継承すると期待するかもしれない ―
> 結局 `A` はすでに制約されている。だが `B` はメソッドで導入された
> 独自の型パラメータだ。伝えたことしか知らない。
>
> そして `B >: A` は階層を*上に*向かう。Scala ではすべての型の上に `AnyRef` と
> `Any` があるので、上限がなければ `B` はどこまでも拡大できる：
> `Book → Shippable → Item → AnyRef → Any`。`<: Shippable` が
> 「ここで止まれ」と言う。これがなければ `Shipment[B]` はコンパイルできない ―
> `Shipment` は型パラメータが `<: Shippable` であることを要求する。

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
すべての呼び出しが `Item` を返す。振る舞いは保持できるが、具体的な戻り値型を失う。

`def cheapest[A](items: List[A]): A` とも書けない。`minBy(_.price)` は
各要素が `.price` を持つことを要求するからだ。境界がなければ、
コンパイラには `A` がそれを持つと信じる理由がない。

上限境界は両方を実現する：`Item` の API へのアクセスと、正確な要素型の返却
（`Book` を入れれば `Book` が出る、`DVD` を入れれば `DVD` が出る）。つまり `<: Item` は
呼び出し側を制限するためではなく、どの操作が有効かをコンパイラに伝えつつ
結果型を最大限に具体的に保つためにある。

<details>
<summary><strong>Java から来た方へ</strong></summary>

Java のジェネリクスはデフォルトで不変だ。`List<Book>` を `List<Item>` として
受け取るには、**呼び出し側で毎回** `? extends` を書く必要がある：

```java
{{#include ../../../examples/step2/Step2kCov.java}}
```

**試してみよう：** `javac Step2kCov.java && java Step2kCov`

これは*使用側*変位だ ― `? extends` を書き忘れると、サブタイプを黙って
受け付けなくなる。柔軟にしたいメソッドすべてにアノテーションが必要で、
書き忘れても何も警告されない。

Scala の `List[+A]` はクラス定義で共変を一度だけ宣言する ―
`cheapest(books)` はどこでもそのまま動く。忘れようがない。

</details>

**標準ライブラリ** ― `WeakReference[+T <: AnyRef]` は参照型のみ受け入れる：

```scala
import scala.ref.WeakReference
val ref = WeakReference(List(1, 2, 3))  // OK: List は AnyRef
// WeakReference(42) — 動かない: Int は AnyRef ではない
```

通常の参照（`val x = ...`）はオブジェクトを生かし続ける ― 誰かが指している限り
ガベージコレクタは回収しない。`WeakReference` は違う：GC による回収を
妨げずに参照を保持する。メモリが逼迫すると GC はオブジェクトを回収でき、
弱参照は静かに空になる。

最も一般的な用途はキャッシュだ。計算結果を再利用のために保持したいが、
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

これはヒープ割り当てオブジェクト（`AnyRef`）でのみ意味がある。`Int` や `Double` のような
値型はスタックに直接格納されるか、オブジェクトにインライン化される ― GC が追跡・
回収するヒープオブジェクトがない。上限境界 `T <: AnyRef` はこの JVM の現実を
型レベルでエンコードしている：ヒープ上に存在しないものへの弱参照は文字通り*作れない*。

高度に感じても心配ない ― この例はスキップして後で戻ってきても大丈夫だ。
上限境界の巧妙な使い方であり、続きの前提条件ではない。

**自己参照境界** ― 境界の中に、境界される型自身が現れるとき：

ここまでの境界は素直だった ― `A <: Shippable`、`A <: Item` ― 境界が固定の型だ。
だが `T <: Ordered[T]` のように、`T` が両側に現れる境界に出くわすことがある。
初めて見ると循環しているように見える。実際にはそうではない ―
ただ読み解くのに少しコツがいる。

```scala
{{#include ../../../examples/step2/step2f1.scala}}
```

読み方のコツは二段階だ：

1. **まず単純に** ― `T <: Ordered` と読む：「T は Ordered でなければならない。」
2. **次に問う** ― 何に対する Ordered か？ 自分自身に対してだ。

平たく言えば：**「T は他の T の値と自分を比較する方法を知っていなければならない。」**

具体的な型を代入すれば腑に落ちる。`Temperature extends Ordered[Temperature]` ―
Temperature は他の Temperature と自分を比較する方法を知っている。汎用の境界
`T <: Ordered[T]` は、`T` がどんな型であれ、自分自身の種類に対する
`compare` を実装した型でなければならないと言っているだけだ。

`Label` に対するコンパイラのエラーがこれを具体化する：`T` には
`constraint <: Ordered[T]` があると教えてくれるが、`Label` はそれを満たさない。
`Label` は何かと自分を比較できるとは一度も約束していない。

`T >: Ordered[T]` はどうか？ 存在しない ― 下限境界はインターフェースから
*離れる*方向に拡大するので、`compare` を呼ぶ能力を失う。自己参照境界は
常に上限境界だ：`T <: Something[T]`。

> **出力側の全ツールキットが揃った：** 安全な拡大のための共変（`+A`）、
> 共変を壊さずにメソッドを追加するための下限境界（`>:`）、
> 特定の振る舞いを要求するための上限境界（`<:`）。次は入力側だ。

## 2-7. 反変 ― 消費者の互換性

最後に `-A`。共変は**出力**側 ― プロデューサーの話だった。
反変は**入力**側 ― コンシューマーの話だ。

```scala
{{#include ../../../examples/step2/step2g.scala}}
```

多くのチュートリアルは反変を
`val bookFmt: Formatter[Book] = itemFormatter` のような明示的な代入で説明する ―
上の例のように。サブタイプ関係は見えるが、こんなコードはめったに書かない。
反変を*使っている* ― 気づかずに ― のは、スーパータイプの関数を渡す
`.filter`、`.map`、`.sortBy` の呼び出しすべてだ。

`Function1[-A, +B]` ― `A => B` のデシュガー形式 ― は
標準ライブラリで最も一般的な反変型だ。

`A` は**入力位置** ―
関数はそれを*受け取る* ― だから反変（`-A`）。

`B` は**出力位置** ―
関数はそれを*返す* ― だから共変（`+B`）。

だから `Item => Boolean` は `Book => Boolean` が期待される場所で使える：

```scala
{{#include ../../../examples/step2/step2g1.scala}}
```

パターン：型が `A` を**消費する**（入力として受け取る）なら `-A` の候補。
`A` を**生産する**（出力として返す）なら `+A` の候補。

なぜ方向が逆転するのか？ `val cheap: Item => Boolean = _.price < 40.0` を見てほしい。
`Item` のすべてのサブタイプ ― `Book`、`DVD`、`DigitalGiftCertificate` ― は `.price` を持つ。
だから `Item` のプロパティだけを使う関数は、どれに対しても自然に動作する。
一度書けば `List[Book]`、`List[DVD]`、`List[Item]` で再利用できる。

反変が実践で意味すること：**より広い型のコンシューマーは、
より狭い型のコンシューマーの安全な代替品だ。**

最初の例の `PriceFormatter` は同じアイデアを明示的にしたものだ。
`ItemFormatter` は `name` と `price` だけを使う ― すべての `Item` が持つもの。だから
`Book` でも `DVD` でも何でも扱える。`BookFormatter` は `isbn` を使う ―
`Book` だけが持つもの。`DVD` を渡すと壊れる。だから逆転：
`Book <: Item`、しかし `PriceFormatter[Item] <: PriceFormatter[Book]`。

<details>
<summary><strong>Java から来た方へ</strong></summary>

Java の `Predicate<Item>` が `List<Book>` をフィルタできるのは、`filter()` が
明示的に `Predicate<? super T>` を受け取るからだ ― 使用側反変：

```java
{{#include ../../../examples/step2/Step2kCon.java}}
```

**試してみよう：** `javac Step2kCon.java && java Step2kCon`

もし `filter()` が `? super` なしの `filter(Predicate<T>)` と書かれていたら、
`Predicate<Item>` を `Predicate<Book>` が期待される場所に渡せない ―
しかもアノテーションが抜けていても何も警告されない。

Scala の `Function1[-A, +B]` は反変を一度だけ宣言する ―
`books.filter(cheap)` はそのまま動く。呼び出し側は何も意識しない。

</details>

### 反変の場合の境界は？

共変ではコンパイラがこう言った：*「`+A` と宣言したから、`A` を入力として
受け取ることは許可できない。」* 下限境界（`[B >: A]`）が脱出口だった ― 上に向かって拡大。

鏡像もある：`-A` は `A` を出力位置で禁止し、上限境界（`[B <: A]`）が
脱出口だ ― 下に向かって制限。対応関係は固定されている：

| 変位 | 問題 | 脱出口 | 方向 |
|---|---|---|---|
| `+A` 共変 | `A` が入力に現れない | `[B >: A]` 下限境界 | 上に拡大 |
| `-A` 反変 | `A` が出力に現れない | `[B <: A]` 上限境界 | 下に制限 |

下限境界は共変の問題を解決する。上限境界は反変の問題を解決する。
交差しない ― 共変は拡大するから脱出口も拡大、反変は制限するから脱出口も制限。

だが脱出口ではなく、クラス自体の境界 ― `[+A <: Item]` や `[-A >: Item]` ―
はどうか？ 有用なのは片方だけだ：

| | 共変 | 反変 |
|---|---|---|
| **脱出口** | `[B >: A]` 下限境界 | `[B <: A]` 上限境界 |
| **クラス境界** | `[+A <: Item]` — **有用** | `[-A >: Item]` — **コードスメル** |

`[+A <: Item]` は働きに見合う：クラス内部で `A` に対して `Item` のメソッド
― `.name`、`.price` ― を呼べる。`WeakReference[+T <: AnyRef]` や
`Shipment[+A <: Shippable]` ですでに見た。

`[-A >: Item]` は冗長だ。`PriceFormatter[Book]` の存在を防ぐが、
反変がすでに `PriceFormatter[Book]` を `PriceFormatter[Item]` が期待される場所で
使えなくしている ― 境界は反変がすでに使用側で処理していることを定義側で繰り返しているだけだ。

<details>
<summary><strong>コードで見る：<code>[-A >: Item]</code></strong></summary>

```scala
{{#include ../../../examples/step2/step2crossed.scala}}
```

</details>

### 実例：ZIO

`PriceFormatter[-A]` や `Function1[-A, +B]` のような単純なコンシューマーでは、
反変の脱出口は問題にならない ― `String` や `Boolean` を返すだけで、`A` は返さないからだ。
だが反変型を**合成する**ときには問題になる ― 実際の例で見るのが
脱出口の存在意義を理解する最良の方法だ。

[ZIO](https://zio.dev/)（最も人気のある Scala ライブラリの一つ）は依存関係管理に
興味深いアプローチを取る：依存関係（データベース接続、ロガー）を関数に直接渡す
代わりに、*必要なもの*を型で記述する。プログラムは一つの値になる：
*「Database があれば実行でき、String を生産する。」*
コンパイラがプログラム実行前にすべての依存関係が満たされていることを検証する
― 型レベルの依存性注入だ。

ZIO はこれを `ZIO[-R, +E, +A]` としてエンコードする：

- **`-R`**（環境） ― プログラムが実行に*必要とする*もの。ここまで見てきたとおり反変。
- **`+E`**（エラー） ― プログラムがどう*失敗しうる*か。共変 ―
  `DatabaseError` を処理する関数は、`NetworkError` で失敗するコードと合成しても
  動作し、`DatabaseError | NetworkError` に拡大する。
- **`+A`**（値） ― 成功時にプログラムが*生産する*もの。共変。

型付きエラーチャネルは ZIO がプロダクションで有用な大きな理由だ：
`Exception` を catch して最善を祈る代わりに、コンパイラが各操作のエラーを
正確に教え、処理を強制する。変位がこれを合成可能にする：
異なる失敗をする二つの操作を組み合わせると、エラー型のユニオンが得られる。

簡略版 `Effect[-R, +A]` を作る ― 変位と上限境界に集中するためエラーチャネルは
省く。この `Effect` は反変の脱出口がなぜ必要かを見るための教材にすぎない：
`R1 <: R` なしでは `flatMap` が書けず、`flatMap` なしではエフェクトを合成できない。

`Effect[-R, +A]` は環境 `R` を必要として実行し、値 `A` を生産する計算だ。
内部的には関数 `R => A` をラップしている：

```scala
class Effect[-R, +A](val run: R => A)
```

`R => A` は `Function1[R, A]` であり、`R` は `-A` スロット、`A` は `+B`
スロットにある。つまり `Effect[-R, +A]` は `Function1[-A, +B]` と同じ変位の
形だ ― ただのラッパーなのだから当然だ。

`Effect[-R, +A]` が実際にどう動くか見るために、異なる機能を表す2つのトレイトと、
両方を提供する `AppEnv` を定義しよう：

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
`db => db.lookup(1)` を渡すと、型は `Effect[Database, String]` になる。
`logMsg` は `String` を受け取り、`Logger` を必要とするエフェクトを返す ―
メッセージをクロージャで閉じ込める：

```scala
val fetchUser: Effect[Database, String] = Effect(db => db.lookup(1))
val logMsg: String => Effect[Logger, Unit] = msg => Effect(logger => logger.log(msg))
```

for 内包表記で合成すると、`Database & Logger` を必要とするエフェクトになる
― 環境を渡して実行する：

```scala
val program: Effect[Database & Logger, Unit] =
  for
    user <- fetchUser
    _    <- logMsg(s"fetched $user")
  yield ()

program.run(AppEnv())  // LOG: fetched user-1
```

for 内包表記は `flatMap` と `map` にデシュガーされる ― だが `flatMap` の
定義こそ、変位が立ちはだかるところだ。

### 問題：`flatMap` と `-R`

直感的には `flatMap` はクラスの `R` をそのまま使うはずだ ―
現在のエフェクトも次のエフェクトも同じ環境で：

```scala
def flatMap[B](f: A => Effect[R, B]): Effect[R, B]
```

だがコンパイラはこれを拒否する。理由を見るには、コンパイラが**位置**をどう
読むかを知る必要がある：

- メソッドパラメータは**入力** ― 位置は `(-)`
- 戻り値型は**出力** ― 位置は `(+)`
- 型がネストされると、位置は符号のように掛け合わされる：

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

修正：メソッドに新しい型パラメータ `R1 <: R` を導入する。
変位ルール（`+` と `-`）は*クラス自身の*型パラメータにのみ適用される。
`R1` は*メソッド*の型パラメータなので、変位チェッカーはまったく追跡しない。
どの位置にも現れられる。コンパイラがチェックするのは、呼び出し側で
`R1 <: R` が成立するかだけだ。

```scala
def flatMap[R1 <: R, B](f: A => Effect[R1, B]): Effect[R1, B] =
    Effect(r => f(run(r)).run(r))
```

<details>
<summary><strong>比較：なぜ List[+A] の flatMap は境界なしで動くのか？</strong></summary>

`List[+A]` は共変だから、ここでも `A` が有効な位置に着地する必要がある。
デシュガー：`flatMap[B](f: Function1[A, IterableOnce[B]]): List[B]`

| ステップ | A はどこ？ | 位置 |
|------|-------------|----------|
| `f` はメソッドパラメータ | 開始 | `(-)` |
| `A` は `Function1` の `-A` スロット | `(-) × (-)` | `(+)` |

結果：`A` は `(+)` に着地。`+A` と宣言した。**OK。** ネストの追加層がないので
位置は反転せず、境界は不要だ。

</details>

### 実装の仕組み

`R1 <: R` は変位チェッカーのためだけじゃない ― 実装も動作させる。
`Effect(r => f(run(r)).run(r))` を名前付きステップに分解し、
`Function1[-A, +B]` を明示的に書いて各型パラメータの着地位置を見てみよう：

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

なぜ `r` は `R` ではなく `R1` なのか？ `flatMap` は `Effect[R1, B]` を返すからだ。
これは関数 `R1 => B` をラップしている。だからその関数に渡される `r` は型 `R1` だ。
そして `R1 <: R` なので、同じ `r` が `run(r)`（`R` を期待）と
`effect2.run(r)`（`R1` を期待）の両方に使える。
これは反変のパターンだ：`run` は `R` を期待し、`R1` は `R` が持つものをすべて
持っている ― より広い型のコンシューマーはより狭い型で動作する。

`scala-cli run step2g2.scala` で完全な例を実行できる：

```scala
{{#include ../../../examples/step2/step2g2.scala}}
```

## 2-8. クイックリファレンス

たくさんの要素が出てきた。この章のすべてをまとめる：

```
不変（デフォルト）     → 互換性を仮定しない
+A（共変）            → 出力側 — 安全に拡大
-A（反変）            → 入力側 — 逆方向で安全
>:（下限境界）         → 共変型の脱出口（拡大）
<:（上限境界）         → 反変型の脱出口（制限）
```

だが仕組みを知るのは話の半分だ。次の問いは：
*実際にどのくらい不変が欲しくて、いつ `+` や `-` を使うべきか？*

> **一息つこう。** 難しい概念的な作業は終わった。Step 3 では変位が実際のコードで
> どう働くかを見る ― より短く、より具体的だ。
