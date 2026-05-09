# Next.js Dev Cache Recovery

## メタデータ

- **Status**: Done
- **Date**: 2026-05-09

## 概要

`Frontend` の開発中に発生する `.next/server` チャンク欠損（`Cannot find module './948.js'` など）を、再現条件を明確化したうえで恒久対策する。目的は、Controller / Operator 画面の API Route が 500 連鎖する状態を防ぎ、開発者が手動キャッシュ削除に依存しない運用にすること。

## 背景・課題

### 現状

- `npm run dev` 実行中に API Route（例: `/api/aircraft/location/all`, `/api/conflict/statistics`）で `.next/server` の chunk module が見つからない例外が断続的に発生することがある。
- 既知の暫定回避は `rm -rf .next && npm run dev`。CI（クリーン環境）では再現しない一方、ローカルの長時間セッションで再発しやすい。
- 直近では `app/api/conflict/*` の更新が多く、dev server の HMR/invalidations が頻発している。

### 課題（Problem Statement）

- `.next` キャッシュ破損時に API Route 全体が 500 化し、フロントのポーリング機能が連鎖失敗する。
- 問題が一時的に消えるため根本原因の切り分けが進まず、作業再開コストが高い。

### なぜ今か（Motivation）

Phase 4 の STCA 実装で API Route の開発頻度が高く、ローカルの再発が生産性ボトルネックになっている。次タスク着手前に、開発体験の安定化を優先する。

---

## 方針

### 決定方針（Decision）

1. **再現条件の固定化**: 端末ログと `next dev` 実行条件（Node / Next / OS）を記録し、発生時の最小再現手順を spec 化する。  
2. **回復導線の標準化**: `Frontend` に `dev:clean`（`.next` 消去 + `next dev`）を追加し、README にトラブルシュート手順を明記する。  
3. **予防策の導入**: API Route の依存パス解決で揺れやすい箇所（動的 import / 参照の循環）を点検し、必要なら import 形を固定（相対依存の解消、循環排除）する。  
4. **監視フック**: dev 起動時に `.next` 欠損エラーを検知したら明示メッセージを出し、再起動手順へ誘導する（scripts 化）。

### 検討した他案（Alternatives Considered）

- **案 A（何もしない）**: 再発時だけ手動で `.next` 削除。採用しない理由: 調査が進まず再発コストが残る。
- **案 B（Next バージョン即時更新）**: 14.2.35 から最新版へ上げる。採用しない理由: 影響範囲が広く、今回の原因切り分け前に行うと回帰の切り分けが難しい。

### トレードオフ（Trade-offs）

- **メリット**: 開発中断時間の短縮、原因の追跡可能性向上、オンボーディング負荷の軽減。
- **デメリット / 受容する制約**: 初期対応は「回復導線 + 観測強化」が中心で、根本原因修正は次スライスにまたがる可能性がある。

---

## 完了条件（Success Criteria）

- [x] `Frontend/package.json` に再現性ある回復コマンド `dev:clean` を追加し、README の手順と一致させた。
- [x] `Frontend/README.md` に `.next` chunk 欠損時の標準復旧手順を追加した。
- [x] 2回以上の再現試行で、`.next` 削除後の再起動により API Route 500 連鎖が解消することを確認した（手動ログ）。
- [x] `npm run lint` / `npm test` / `npm run build` が通る。

---

## 影響範囲

- **Frontend/package.json**: `dev:clean` などの運用スクリプト追加
- **Frontend/README.md**: 開発時トラブルシュート更新
- **必要に応じて Frontend/app/api/**: 依存関係の安定化修正
- **spec/spec.md**: T系タスクとして追記

---

## 実装計画

### Phase 1

1. 現象ログを整理し、最小再現手順を記録。
2. `dev:clean` スクリプトを追加（`.next` 削除 → `next dev`）。
3. README に復旧手順と再発時の採取情報（node/next/version）を追記。
4. 必要なら API Route 周辺の import 経路を点検し、揺れやすい参照を修正。

---

## 検証

- [x] テストが通る
- [x] ビルドが通る
- [x] 手動動作確認（`dev:clean` 後に API Route 500 が解消）

---

## 未解決事項（Unresolved Questions）

- Node v24 固有問題か、Next 14.2.35 固有問題かの切り分けをどこまで行うか。
- `dev:clean` をデフォルト運用にするか、復旧時のみ使用にするか。

---

## 関連ドキュメント

- [spec/spec.md](../spec.md)
- [Frontend/README.md](../../Frontend/README.md)
