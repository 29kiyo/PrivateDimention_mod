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
| 🚫 プロット境界 | プロット外に出ると強制的に元の世界へ送還 |
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
| `/pd give [player]` | アイテムを付与 | OP (レベル2以上) |
| `/pd info` | 自分のプロット情報表示 | 全員 |
| `/pd reload` | 設定をリロード | OP (レベル2以上) |

> 通常プレイヤーは **Dimension in a Bottle をクラフトして右クリックで使用**できます。コマンドは `/pd info` のみ利用可能です。

## 設定 (config.json)

設定ファイルは `config/privatedimension/config.json` に生成されます。

```json
{
  "plotSize": 48,
  "plotSpacing": 128,
  "plotFloorY": 64,
  "pullEntityLimit": 10,
  "pullEntityRadius": 3.0,
  "enableBorderEnforcement": true
}
```
