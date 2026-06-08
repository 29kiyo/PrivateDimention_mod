# PrivateDimension Mod

**Dimension in a Bottle** アイテムで自分専用のプライベート次元に移動できるサーバーサイドMod。  
Paper版プラグインを Fabric / NeoForge 向けにフォークしたものです。

---

## 対応バージョン

| ファイル | Minecraft | ローダー | Java |
|---------|-----------|---------|------|
| `PrivateDimension-fabric-1.21.5-*.jar`  | 1.21.5 | Fabric + Fabric API | 21 |
| `PrivateDimension-neoforge-1.21.5-*.jar`| 1.21.5 | NeoForge 21.5.96+   | 21 |
| `PrivateDimension-fabric-26.1.2-*.jar`  | 26.1.2 | Fabric + Fabric API | 25 |
| `PrivateDimension-neoforge-26.1.2-*.jar`| 26.1.2 | NeoForge 26.1.2.73+ | 25 |

サーバーサイドのみで動作します（クライアント不要、入れても問題なし）。

---

## 機能

| 機能 | 説明 |
|------|------|
| 🌀 次元移動 | Dimension in a Bottle を右クリック → プライベート次元へ |
| 🔙 帰還 | 次元内で再使用 → 元の座標に戻る |
| 👥 エンティティ連行 | スニーク+使用で周囲の友好的Mobも連れていける |
| 🏠 48×48 プロット | プレイヤーごとに専用空間を自動割り当て |
| 🚫 プロット境界 | プロット外に出ると元の世界へ強制送還 |
| ☠️ 死亡対応 | 次元内で死亡しても元の世界でリスポーン |

---

## クラフトレシピ

```
# E #
# D #
# L #

# = ガラス
E = エンダーアイ
D = ダイヤモンド
L = ローデストーン
```

---

## コマンド

| コマンド | 説明 | 権限 |
|---------|------|------|
| `/pd give [player]` | アイテムを付与 | op (lv.2) |
| `/pd info` | プロット情報を表示 | 全員 |
| `/pd reload` | 設定をリロード | op (lv.2) |

エイリアス: `/privatedim`

---

## 設定ファイル

`config/privatedimension/config.json` に自動生成されます。

```json
{
  "worldName": "private_dimension",
  "plotSize": 48,
  "plotSpacing": 128,
  "plotFloorY": 64,
  "pullEntityLimit": 10,
  "pullEntityRadius": 3.0,
  "borderEnforcement": true,
  "msgBorderForced": "[Private Dimension] プロットの外には出られません！"
}
```

---

## ビルド方法 (GitHub Actions)

タグを付けてpushするとリリースが自動作成されます:

```bash
git tag v1.0.0
git push origin v1.0.0
```

または GitHub の Actions タブから `workflow_dispatch` で任意バージョンをビルドできます。

---

## ライセンス

MIT  
元データパックの著作権は [Chuzume](https://github.com/Chuzume) 様に帰属します。
