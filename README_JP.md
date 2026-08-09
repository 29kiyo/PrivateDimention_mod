[English](README.md)

## このModについて
A derivative work of "Private_Dimension" by Chuzume.<br>

このModは、Chuzume様が作成された「Private_Dimension」をMod版に改変したものです。
本Modに関する著作権その他の権利はChuzume様に帰属します。

また、本Modを導入・使用したことによって発生したいかなる問題や損害についても、制作者および配布者は一切の責任を負いかねます。ご了承ください。


## 原作・参考リンク

- 制作者: <br>[@Chuzume](https://x.com/Chuzume)
- レポジトリ: <br>[Private_Dimension](https://github.com/Chuzume/Private_Dimension)
- 動画: <br>[【マイクラ】"次元の瓶"で、家とか拠点を持ち歩いちゃおう！！！【データパック】](https://www.youtube.com/watch?v=NrwN3NJLuiA)


## 使い方
レシピ
通常の作業台にて作れます

[Dimension in a Bottle]

![Image](https://cdn-ak.f.st-hatena.com/images/fotolife/C/Chuzume/20230105/20230105085556.png)

# PrivateDimension

プライベート次元Mod for Fabric / NeoForge

## 概要

**Dimension in a Bottle** アイテムを使うと、自分専用のプライベート次元に移動できます。
次元内は 48×48 の構造物が生成された専用スペースです。

元データパック [Private Dimension by Chuzume](https://github.com/Chuzume/Private_Dimension) の機能を、Fabric/NeoForge Mod として 29kiyo が再実装したものです。

## 対応バージョン

| Minecraft | Fabric | NeoForge |
|-----------|--------|----------|
| 1.21.5 〜 1.21.8 | ✅ | ✅ |
| 1.21.9 〜 1.21.11 | ✅ | ✅ |
| 26.1.1 〜 26.1.2 | ✅ | ✅ |

## 機能

| 機能 | 説明 |
|------|------|
| 🌀 次元移動 | Dimension in a Bottle を右クリックするとプライベート次元へ移動 |
| 🔙 帰還 | 次元内で再び使用すると元の座標に戻る |
| 👥 エンティティ連行 | スニーク+使用で半径3ブロック内の友好的エンティティを連れていける |
| 🏠 48×48 プロット | プレイヤーごとに専用の 48×48 空間を自動割り当て |
| 🚫 プロット境界 | プロット外に出ると強制的に元の世界へ送還（OPおよびタグ付きプレイヤーは対象外） |
| ☠️ 死亡対応 | 次元内で死亡しても元の世界でリスポーン |

## 必要環境

- **Fabric Loader** または **NeoForge**（対応バージョン参照）
- **Java** 21+（26.x系は Java 25+）

## インストール

1. 対応するバージョンの `PrivateDimension-*.jar` をダウンロード
2. `.minecraft/mods/` フォルダへ配置
3. ゲームを起動

## コマンド

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/pd give <プレイヤー>` | 1人以上のプレイヤーにアイテムを付与(`@a`などのセレクターにも対応) | OP (レベル2以上) |
| `/pd info` | 自分のプロット情報表示 | 全員 |
| `/pd reload` | 設定をリロード | OP (レベル2以上) |

> 通常プレイヤーは **Dimension in a Bottle をクラフトして右クリックで使用**できます。コマンドは `/pd info` のみ利用可能です。

## 設定 (config.json)

設定ファイルは `config/privatedimension/config.json` に生成されます。

```json
{
  "plotSize": 48,
  "plotHeight": 46,
  "plotSpacing": 128,
  "plotFloorY": 64,
  "pullEntityLimit": 10,
  "pullEntityRadius": 3.0,
  "borderEnforcement": true,
  "cooldownSeconds": 2,
  "plotBypassTag": "pd_free",
  "structureFile": "plot48x48.nbt"
}
```

## プロット境界のバイパス

デフォルトでは、プロット外に出ると自分のプロット内へ押し戻されます。以下の2通りでバイパスできます。

- **OP**（権限レベル2以上）は常に境界チェックの対象外です。
- **タグ付きプレイヤー**もバイパス可能です。バニラの`/tag`コマンドでバイパス用タグ（デフォルト: `pd_free`、設定ファイルの`plotBypassTag`で変更可）を付与してください。

  ```
  /tag <プレイヤー名> add pd_free
  ```

  外す場合は `/tag <プレイヤー名> remove pd_free` です。

  例: `/tag Steve add pd_free` とすると、Steveはどのプロットでも境界を気にせず出入りできるようになります。

## カスタム島(.nbt)

デフォルトでは、48x48の同梱アイランド(`plot48x48.nbt`)が使われます。自分で用意した構造物に差し替えることもできます。

1. 一度サーバーを起動、またはシングルプレイに参加してください。ログに構造物フォルダの絶対パスが出力されます(例):
   ```
   <ワールドフォルダ>/generated/privatedimension/structures
   ```
2. そのフォルダに、自分で用意した`.nbt`ファイル(例: `my_island.nbt`)を置いてください。
3. `config.json`の`structureFile`をそのファイル名(例: `"my_island.nbt"`)に設定してください。
4. `plotSize`(横幅)・`plotHeight`(高さ)・`plotSpacing`(間隔)を、実際の構造物のサイズに合わせて調整してください。

プレイヤーが初めてプロットに入る際、既定のスポーン地点付近で自動的に安全な着地点(足場があり、頭上・足元が空いている場所)を探すため、変わった形の構造物でも落下や窒息が起きにくくなっています。

## タブ補完

`/pd give <プレイヤー名>` は、バニラの`/give`コマンドなどと同じように、オンラインプレイヤー名のタブ補完に対応しています。

## 言語 / ローカライズ

本Modは英語(デフォルト)と日本語のメッセージを同梱しています。初回起動時、両方が以下に展開されます。
```
config/privatedimension/lang/
```

デフォルト設定(`config.json`の`"language": "auto"`)では、対応する言語ファイルがあれば各プレイヤーのクライアント言語で、無ければ英語で表示されます。

**全員に固定の言語を使わせたい場合:** `config.json`の`"language"`に言語コード(例: `"ja"`)を指定してください。

**独自の言語を追加したい場合:** `config/privatedimension/lang/`に`<言語コード>.json`(例: `fr.json`)というファイルを、`en.json`と同じキーで作成し、`"language"`をそのコードに設定してください(クライアント言語と一致していれば`"auto"`のままでも構いません)。

Modを更新した際、新しく追加されたメッセージキーは既存の言語ファイルに自動で追記されます(あなたが編集したカスタム翻訳が上書きされることはありません)。
