# Phase 4 — Conflict API BFF・レーダー STCA 表示（スライス 1）

## メタデータ

- **Status**: Done
- **Date**: 2026-05-09

## 概要

バックエンド `ConflictAlertController`（`/api/conflict/*`）を Next.js BFF 経由でフロントから利用可能にし、Controller / Operator のレーダー上に **STCA 統計ストリップ**と **リスクに応じた機体シンボル強調（リング＋赤レベル時の点滅）**を追加する。Phase 4-1 / 4-3 の一部を満たす縦切り。

## 背景・課題

### 現状

- `ConflictAlertController` と DTO は実装済み。
- フロントは `GET /aircraft/location/all` の `riskLevel` のみ利用し、`/api/conflict` を呼んでいなかった（[spec/spec.md](../spec.md) のギャップ表どおり）。

### 課題（Problem Statement）

- レーダー訓練でペア統計・違反件数を UI に出せない。
- シンボルは常に同一の白マーカーで、ラベル `Rxx` 以外の視覚的 STCA が弱い。

### なぜ今か（Motivation）

- ロードマップ「推奨着手順」3 に沿い、BFF 接続と描画強調を先に入れると 4-2（ペア数値）や将来の選択機との突合の土台になる。

---

## 方針

### 決定方針（Decision）

1. **BFF**: `proxyToBackend` で Java のパスをそのまま転送（`/api/conflict/...`）。`LocationService` が `/aircraft` 直下であることとは異なるが、バックエンド実装に合わせる。
2. **クライアント**: `utility/api/conflict.ts` に DTO 型と `fetchConflictStatistics` 等を定義。位置更新と同じ `LOCATION_UPDATE_INTERVAL` で統計をポーリング。
3. **UI**: `ConflictSummaryStrip` をレーダー左上に絶対配置（`ConflictStatisticsDto`）。
4. **描画**: `drawAircraft` のマーカーで `riskLevel >= 30` を黄系リング、`>= 70` を赤リングの点滅（`performance.now()` ベース）。

### 検討した他案（Alternatives Considered）

- **位置 API に統計を載せる**: バックエンド変更が大きいため却下。将来まとめてもよい。
- **WebSocket**: Phase 7 領域のため今回はポーリングのまま。

### トレードオフ（Trade-offs）

- **メリット**: OpenAPI 変更なし、既存 DTO に追従するだけ。
- **デメリット**: ポーリングが 1 本増える（間隔は位置と同じ 1s）。

---

## 完了条件（Success Criteria）

- [x] `app/api/conflict/**` が `ConflictAlertController` の GET と対応している。
- [x] `/controller`・`/operator` で STCA ストリップが表示され、統計が更新される（バックエンド起動時）。
- [x] `riskLevel` に応じて機体マーカーにリング強調・赤レベル時点滅がある。
- [x] `npm test` / `npm run lint`（Frontend）が通る。

---

## 影響範囲

- **Frontend**: `app/api/conflict/*`、`utility/api/conflict.ts`、`conflictSummaryStrip.tsx`、`radarCanvas.tsx`、`drawAircraft.ts`、`controller` / `operator` ページ、`Frontend/README.md`
- **Backend**: 変更なし

---

## 実装計画

### Phase 1（本 spec の範囲）

1. BFF ルート追加（all / filtered / critical / violations / statistics / health / aircraft/{callsign}）。
2. クライアント API とストリップ、描画強調。

### 後続（別 spec / Issue）

- 4-2: 選択機またはラベル横へのペア距離（`getAircraftConflicts` 等の利用）。
- 4-1 残り: 閾値・表示設定 UI、ペア強調の連動。

---

## 検証

- [x] `npm test` / `npm run lint`（Frontend）
- [ ] 手動: バックエンド＋フロント起動、衝突検知サンプル機でストリップとマーカー変化を確認

---

## 関連ドキュメント

- [spec/spec.md](../spec.md) — Phase 4 ロードマップ
- `Backend/.../ConflictAlertController.java`
