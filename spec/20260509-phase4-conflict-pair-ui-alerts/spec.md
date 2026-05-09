# Phase 4 — ペア間隔数値表示と間隔違反の強通知（スライス 2）

## メタデータ

- **Status**: Done（2026-05-09 実装反映）
- **Date**: 2026-05-09

## 概要

[20260509-phase4-conflict-bff-ui](20260509-phase4-conflict-bff-ui/spec.md) で BFF・統計ストリップ・シンボル強調まで入った前提で、**選択中（または全機）のコンフリクトを数値として読める UI**（Issue #61 / 4-2）と、**違反・高リスクの見逃しを減らす強い通知**（Issue #62 / 4-3 の残り）をフロントに追加する。バックエンド API は既存 `ConflictAlertController` / DTO を主に利用し、OpenAPI の拡張は必須としない。

## 背景・課題

### 現状

- `utility/api/conflict.ts` に `fetchAircraftConflicts(callsign)` および `fetchConflictViolations` / `fetchConflictCritical` がある。BFF は `/api/conflict/aircraft/[callsign]` 等で転送済み。
- `ConflictAlertDto` は `pairId`（例: テスト上 `CF1-CF2`）、`closestHorizontalDistance` / `closestVerticalDistance`、`timeToClosest`、`alertLevel`、`riskLevel` 等を持つ。
- レーダーは `riskLevel` 由来のラベル色・`R` 行、STCA ストリップ、シンボルリングはあるが、**どの機と何 NM / 何 ft 離れているか**はラベルからは読みにくい。
- ストリップはパッシブ表示に留まり、**新規の違反発生時に注意を引く**仕組みがない。

### 課題（Problem Statement）

- 訓練・デモで「ペアの実距離・鉛直隔離」を説明・確認したいが、開発者ツールなしでは見えない。
- 高リスク・Sep 違反がストリップの数字更新だけだと気づきにくい。

### なぜ今か（Motivation）

- ロードマップ「推奨着手順」3 の次工程が 4-2 / 4-3 の UI 接続であり、スライス 1 と同じく **Backend 変更なしで縦切り可能**な範囲から着手する。

---

## 方針

### 決定方針（Decision）

1. **4-2（ペア数値）**
   - **データ取得**: 位置ポーリングと同周期、またはやや低頻度で、**フォーカス機**（クリック選択中の `callsign`）に対して `fetchAircraftConflicts` を呼ぶ。全機分が必要なら `fetchConflictAll` / critical / violations との使い分けを設計コメントで固定する（N+1 過多を避ける）。
   - **表示場所（いずれかまたは併用）**:
     - **A**: `drawAircraft` のデータブロック近傍に、当該機のコンフリクト 1 件目（または最大 `riskLevel`）の **H/V 距離・相手識別**を短い行で表示。
     - **B**: 既存パネル（例: フライトプラン操作付近）に **選択機のコンフリクト一覧**（`pairId` から相手コールサインを表示。`pairId` の分解規約は `ConflictDetector` / `StringUtils.generatePairId` に合わせ、コールサインに `-` が含まる場合は別途バックエンド仕様を確認する）。
   - **単位**: Backend が返す距離の単位（NM / m 等）を `Frontend/README.md` または型コメントで明示し、UI 表記と一致させる。

2. **4-3（強通知）**
   - **トリガ**: `fetchConflictViolations` または `fetchConflictCritical` の結果で、**前回ポーリングと比較して新規の `pairId` が現れた**、または `separationViolationCount`（統計）が増加したときに通知。
   - **UI**: プロジェクトに既存のトースト／ダイアログ基盤があればそれを利用。なければ **軽量な固定ポジションのバナー**（レーダー下など）＋自動消去で開始し、後からコンポーネント化する。
   - **抑制**: 同一 `pairId` の連打を避けるため、**クールダウン**（例: 数秒〜数十秒）または「最後に通知した pairId + 時刻」を保持する。

### 検討した他案（Alternatives Considered）

- **Backend で「通知用イベント」ストリームを追加**: リアルタイム性は上がるが Phase 7 寄りでスコープが広いため今回は見送り。
- **全機ごとに `fetchAircraftConflicts`**: 機数が増えるとリクエストが線形に増えるため、デフォルトは「選択機＋ストリップ用の集約 API」の組み合わせとする。

### トレードオフ（Trade-offs）

- **メリット**: 既存 DTO・BFF のみで完結しやすい。
- **デメリット**: ポーリングベースのため、通知はサブ秒単位の確実性はない（現状シミュレーション 1Hz と整合）。

---

## 完了条件（Success Criteria）

- [x] シミュレーション中、**少なくとも 1 機を選択した状態**で、当該機に関連するコンフリクトの **水平・鉛直隔離の数値**（および可能なら相手識別）が UI 上で確認できる（4-2）。→ `selectedAircraftConflictsPanel.tsx` + `utility/conflict/pairId.ts`
- [x] Sep 違反またはクリティカル相当の **新規検知**に対し、ストリップ更新以外の **強い通知**が一度表示される（4-3）。誤爆が多い場合はクールダウンで抑制できる。→ `separationViolationAlerts.tsx`（30s / pair、バナー約 10s）
- [x] `npm test` / `npm run lint`（Frontend）が通る。
- [x] 距離単位・通知ポリシーが `Frontend/README.md` の該当節（レーダー／コンフリクト）に追記または更新されている。

---

## 影響範囲

- **Frontend**: `radarCanvas.tsx`、選択状態を持つ親（`controller` / `operator` ページ）、`drawAircraft.ts` または新コンポーネント、必要なら `utility/api/conflict.ts`（ヘルパ・型コメント）、`Frontend/README.md`
- **Backend**: 原則なし（DTO 不足が判明した場合のみ別タスクで OpenAPI / DTO 拡張を検討）

---

## 実装計画

### Phase 1（4-2）

1. 選択 `callsign` をレーダー系に伝播（既存の選択ハンドラがあれば流用）。
2. `fetchAircraftConflicts` の結果を state に保持し、描画パスまたはサイド一覧に接続。
3. `pairId` から相手コールサイン表示（仕様確認と単体テスト可能な純関数に切り出し）。

### Phase 2（4-3）

1. violations / critical / statistics のいずれかを用いた **差分検知**ユーティリティ。
2. トーストまたはバナー UI の実装とクールダウン。
3. 手動確認手順を README または MANUAL_TEST に 1 行追記。

---

## 検証

- [x] テストが通る（`utility/conflict/*.test.ts`）
- [ ] ビルドが通る（リリース前に `npm run build` 推奨）
- [ ] 手動: 2 機以上のシナリオでコンフリクト発生時に数値・通知が期待どおり（`Backend/docs/test-data/MANUAL_TEST.md` 1 行参照）

---

## 未解決事項（Unresolved Questions）

- `pairId` の文字列形式がコールサインに `-` を含む場合も一意に分解できるか（必要なら Backend で `otherCallsign` フィールド追加を別 spec 化）。
- Controller と Operator で通知の重複を避けるか（共有 Context にするかページ単位か）。

---

## 関連ドキュメント

- [20260509-phase4-conflict-bff-ui](20260509-phase4-conflict-bff-ui/spec.md)
- [spec/spec.md](../spec.md)（フェーズ 4 ロードマップ）
- [Frontend/README.md](../../Frontend/README.md)
