# セットアップ手順

## GitHubへのアップロード方法

`.github/` や `gradle/wrapper/` はGitHub Web UIでは追加できません。
**GitHub Codespaces** を使うと、ブラウザだけで完結します。

---

### 手順

#### 1. GitHubで空のリポジトリを作成
- `Add a README file` にチェックを入れて作成（mainブランチが必要）

#### 2. Codespacesを起動
- リポジトリページで `<> Code` → `Codespaces` タブ → `Create codespace on main`
- ブラウザでVSCode+ターミナルが開く（1〜2分かかる）

#### 3. ターミナルで以下を実行

```bash
# zipをダウンロード（自分のzipのURLに変更してください）
wget -O mod.zip "https://github.com/YOUR/REPO/releases/download/.../PrivateDimension-mod.zip"

# 展開して中身をリポジトリルートにコピー
unzip mod.zip
cp -r privatedimension-mod/. .
rm -rf privatedimension-mod mod.zip

# gitに追加してpush
git add -A
git commit -m "Initial commit: PrivateDimension mod"
git push
```

これでCodespacesを閉じても大丈夫です。

---

### 代替: zipをGitHub Releases/Gistに置いてwgetする場合

zipを一時的にどこか（Google Drive, Dropbox等）に置いて
そのダウンロードURLを wget に渡してください。

または claude.ai からダウンロードしたzipをCodespacesの
エクスプローラーにドラッグ&ドロップでアップロードできます。

---

### Codespaces無料枠について
- 月60時間まで無料（個人アカウント）
- このセットアップは5分以内に終わります
