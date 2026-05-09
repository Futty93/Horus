# T-3 — フライトプラン経路のテスト・サンプル・README整備

## メタデータ

- **Status**: Done
- **Date**: 2026-05-09
- **完了**: 2026-05-09（実装・ドキュメント反映済み）
- **親ロードマップ**: [spec/spec.md](../spec.md)（技術的負債 **T-3**）
- **関連 Issue**: [#76](https://github.com/HorusATC/Horus/issues/76)
- **親機能 spec**: [20260308-flight-plan Phase 6](../20260308-flight-plan/spec.md)

## 概要

フライトプラン・シナリオロード周りについて、**テストの網羅の明示**、**Swagger/手動用サンプル JSON**、**README/MANUAL_TEST の単一の真実**を揃えた。

## API × テスト × サンプル（カバレッジマトリクス）

主要 API と、**統合テスト**（`FlightPlanApiIntegrationTest`）、**単体テスト**、**test-data JSON** の対応。空白なし。

| API / ユースケース | 統合テスト（`FlightPlanApiIntegrationTest`） | 単体・その他テスト | test-data JSON |
|---------------------|-----------------------------------------------|---------------------|----------------|
| `POST /api/aircraft/spawn-with-flightplan` | `spawnWithFlightPlan_createsAircraft` | `ScenarioServiceFlightPlanTest` 等 | `spawn-with-flightplan-minimal.json`, `spawn-with-flightplan-sample.json` |
| `GET /api/aircraft/{cs}/flightplan` | `getFlightPlan_returnsStatus` | `FlightPlanNavigationTest`, `FlightPlanDomainModelTest` | （spawn 後に参照） |
| `POST /api/aircraft/{cs}/direct-to` | `directTo_appliesInstruction` | 同上 | `direct-to-sample.json` |
| `POST /api/aircraft/{cs}/resume-navigation` | `resumeNavigation_appliesInstruction` | 同上 | （body なし） |
| `POST /api/aircraft/{cs}/flightplan`（付与・差替） | `assignFlightPlan_toExistingAircraft` | — | `assign-flightplan-sample.json` |
| `POST /api/scenario/load` 正常（複数機） | `loadScenario_successWithMultipleAircraft` | — | `scenario-load-minimal.json`（1 機・Swagger 最小） |
| `POST /api/scenario/load` シミュ非開始 | `loadScenario_doesNotStartSimulation` | — | 同上 |
| `POST /api/scenario/load` 空配列 400 | `loadScenario_returns400_whenAircraftEmpty` | — | — |
| `POST /api/scenario/load` 重複コールサイン 400 | `loadScenario_returns400_onDuplicateCallsign` | — | — |
| `POST /api/scenario/load` initialPosition 欠損スキップ | `loadScenario_skipsAircraftWithoutInitialPosition` | — | — |
| `POST /api/scenario/load` Fix 不存在 400 | `loadScenario_returns400_whenFixNotFound` | — | — |

**手動手順**: [Backend/docs/test-data/MANUAL_TEST.md](../../Backend/docs/test-data/MANUAL_TEST.md)（`scenario/load` は手順 1.4）。**サンプル一覧**: [Backend/docs/test-data/README.md](../../Backend/docs/test-data/README.md)。

---

## 背景・課題（完了時メモ）

コード先行でテスト・サンプルが既に存在していたが、`POST /api/scenario/load` の **専用サンプル JSON** と **MANUAL_TEST への手順**、**`POST .../flightplan` の統合テスト**、**Frontend README の scenario/load 誤記**が不足していた。本 spec の作業で解消した。

---

## 方針（要約）

1. ギャップのみ埋める（厳密なカバレッジ数値目標は置かない）。
2. README 変更は Backend / Frontend 両方、プロジェクトルールに従い同期。

---

## 完了条件（Success Criteria）

### Must-have

- [x] **カバレッジマトリクス**: 上表
- [x] **`POST /api/scenario/load`**: `Backend/docs/test-data/scenario-load-minimal.json`、README / MANUAL_TEST から辿れる
- [x] **親 spec 整合**: [20260308-flight-plan Phase 6](../20260308-flight-plan/spec.md) 更新済み
- [x] **Backend README**: フライトプラン API 表・test-data・T-3 spec への導線、[FlightPlanController](../../Backend/README.md) の構造記載

### Should-have

- [x] 統合テスト: `assignFlightPlan_toExistingAircraft` 追加
- [x] [#76](https://github.com/HorusATC/Horus/issues/76) に `gh issue comment` で完了を通知（マージ後または本コミット push 後）

### Optional

- [x] Frontend README: BFF 一覧の `scenario/load` 説明修正、Backend MANUAL_TEST / test-data への短い導線
- [ ] JaCoCo しきい値は別 spec

---

## 影響範囲（実施済み）

- `Backend/docs/test-data/scenario-load-minimal.json`（新規）
- `Backend/docs/test-data/README.md`, `MANUAL_TEST.md`
- `Backend/README.md`
- `Backend/.../FlightPlanApiIntegrationTest.java`
- `Frontend/README.md`
- `spec/20260308-flight-plan/spec.md`, `spec/spec.md`（T-3 完了反映）

---

## 検証

- [x] `./gradlew test --tests "...FlightPlanApiIntegrationTest"` 成功
- [x] 本 spec の Must-have / Should-have（Issue コメント除く）を満たす

---

## 関連ドキュメント

- [spec/spec.md](../spec.md)
- [20260308-flight-plan/spec.md](../20260308-flight-plan/spec.md)
- [20260315-scenario-load-api](../20260315-scenario-load-api/spec.md)

---

## 変更履歴

| 日付 | 内容 |
|------|------|
| 2026-05-09 | 初版（T-3 / Phase 6 着手用） |
| 2026-05-09 | Done。マトリクス、scenario-load-minimal、assign 統合テスト、README 一式、Frontend README 修正 |
