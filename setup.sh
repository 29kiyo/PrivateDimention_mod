#!/bin/bash
# PrivateDimension mod - Codespaces セットアップスクリプト
# 使い方: bash setup.sh <zip_url>
# 例: bash setup.sh "https://example.com/PrivateDimension-mod.zip"

set -e

ZIP_URL="${1}"
if [ -z "$ZIP_URL" ]; then
  echo "使い方: bash setup.sh <zip_url>"
  echo "zipをCodespacesにドラッグ&ドロップした場合は zip_url の代わりにファイルパスでも可"
  exit 1
fi

echo "==> Downloading zip..."
wget -O /tmp/mod.zip "$ZIP_URL"

echo "==> Extracting..."
unzip -q /tmp/mod.zip -d /tmp/mod_extract
cp -r /tmp/mod_extract/privatedimension-mod/. .
rm -rf /tmp/mod.zip /tmp/mod_extract

echo "==> Adding to git..."
git add -A
git commit -m "Initial commit: PrivateDimension mod"
git push

echo ""
echo "Done! GitHubリポジトリにpushしました。"
