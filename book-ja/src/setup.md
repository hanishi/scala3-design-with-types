# セットアップ

```bash
# scala-cli をインストール（まだの場合）
curl -sSLf https://scala-cli.virtuslab.org/get | sh

# 確認
scala-cli version
# Scala 3.x が必要です
```

## サンプルファイルの使い方

本書のすべてのコード例は `examples/` ディレクトリにそのまま実行できるファイルとして用意されている。リポジトリをクローンして、任意の例を実行してほしい：

```bash
git clone https://github.com/hanishi/scala3-design-with-types.git
cd scala3-design-with-types

# 特定の例を実行
scala-cli run examples/step0/step0.scala

# またはステップのディレクトリに移動して
cd examples/step2
scala-cli run step2c.scala
```

各ファイルは自己完結している ― 読んでいるステップに対応するファイルを選んで実行するだけだ。

> **注意:** 必ず1ファイルずつ実行すること ― ディレクトリ全体をコンパイルしない（例：`scala-cli compile examples/step2/`）こと。各ステップのファイルは、進行を示すために同じクラス（`Product`、`Book`、`Box` など）を異なるシグネチャで意図的に再定義している。まとめてコンパイルすると重複定義エラーになる。
