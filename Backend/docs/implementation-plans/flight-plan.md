# フライトプラン機能 実装計画

## 概要

航空機がフライトプランに従って自動飛行し、管制官が Direct To・Resume Navigation 等の指示を出せるようにする機能の実装計画。

**設計ドキュメント**: [flight-plan-implementation.md](../design/flight-plan-implementation.md)

## 前提条件

- [ ] 設計ドキュメントのレビュー完了
- [ ] `AircraftBase`、`CommercialAircraft`、`ScenarioServiceImpl` の現状把握
- [ ] 既存の `AtsRouteRepository`（Fix 位置取得）の動作確認
- [ ] フロントエンドとの API 互換性確認（既存 `/aircraft/location` 等）

## 実装 Phase 一覧

### Phase 1: ドメインモデル実装（基盤）

**目的**: フライトプラン機能の基盤となる値オブジェクト・ドメインクラスを実装

| # | タスク | ファイル | 状態 |
|---|--------|----------|------|
| 1.1 | `NavigationMode` enum 作成 | `domain/model/entity/flightplan/NavigationMode.java` | 未着手 |
| 1.2 | `AltitudeConstraint` enum 作成 | `domain/model/entity/flightplan/AltitudeConstraint.java` | 未着手 |
| 1.3 | `WaypointAction` enum 作成 | `domain/model/entity/flightplan/WaypointAction.java` | 未着手 |
| 1.4 | `FlightPlanWaypoint` クラス作成 | `domain/model/entity/flightplan/FlightPlanWaypoint.java` | 未着手 |
| 1.5 | `FlightPlan` クラス作成 | `domain/model/entity/flightplan/FlightPlan.java` | 未着手 |
| 1.6 | Phase 1 ユニットテスト | `ConflictDetectorTest` 等の参考に新規テストクラス | 未着手 |

**完了条件**: 全ドメインモデルが単体で正しく動作し、テストが通る

---

### Phase 2: 航空機へのナビゲーション機能追加

**目的**: 航空機がフライトプランに従ってウェイポイントを順次通過するようにする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 2.1 | `Aircraft` インターフェース拡張 | フライトプラン関連メソッド | 未着手 |
| 2.2 | `AircraftBase` フィールド追加 | flightPlan, currentWaypointIndex, navigationMode 等 | 未着手 |
| 2.3 | ウェイポイント通過判定ロジック | `hasPassedWaypoint`, `calculateDynamicThreshold` | 未着手 |
| 2.4 | `calculateNextAircraftVector` 拡張 | FLIGHT_PLAN 時の自動ナビゲーション | 未着手 |
| 2.5 | 高度・速度制約の自動適用 | FlightPlanWaypoint の制約に基づく | 未着手 |
| 2.6 | `CommercialAircraft` 対応 | 必要に応じてオーバーライド | 未着手 |
| 2.7 | Phase 2 ユニットテスト | 通過判定・自動飛行のテスト | 未着手 |

**完了条件**: フライトプラン付き航空機がウェイポイントを順次通過して飛行する

---

### Phase 3: 管制指示の拡張

**目的**: Direct To、Resume Navigation 等の管制指示を処理できるようにする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 3.1 | `ScenarioService` インターフェース拡張 | `directToFix`, `resumeNavigation` | 未着手 |
| 3.2 | `ScenarioServiceImpl` 実装 | Direct To ロジック | 未着手 |
| 3.3 | `ScenarioServiceImpl` 実装 | Resume Navigation ロジック | 未着手 |
| 3.4 | `instructAircraft` 拡張 | ヘディング指示時に NavigationMode → HEADING | 未着手 |
| 3.5 | `directFixAircraft` との連携 | 既存 Direct 機能とフライトプラン再開の統合 | 未着手 |
| 3.6 | Phase 3 ユニットテスト | 管制指示のテスト | 未着手 |

**完了条件**: 管制指示により航空機のナビゲーションモードが正しく切り替わる

---

### Phase 4: API・DTO 実装

**目的**: REST API 経由でフライトプラン機能を利用可能にする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 4.1 | `FlightPlanDto` 作成 | interfaces/dto | 未着手 |
| 4.2 | `FlightPlanWaypointDto` 作成 | interfaces/dto | 未着手 |
| 4.3 | `CreateAircraftWithFlightPlanDto` 作成 | interfaces/dto | 未着手 |
| 4.4 | `POST /api/aircraft/spawn-with-flightplan` | FlightPlanService | 未着手 |
| 4.5 | `POST /api/aircraft/{callsign}/flightplan` | FlightPlanService | 未着手 |
| 4.6 | `POST /api/aircraft/{callsign}/direct-to` | FlightPlanService | 未着手 |
| 4.7 | `POST /api/aircraft/{callsign}/resume-navigation` | FlightPlanService | 未着手 |
| 4.8 | `GET /api/aircraft/{callsign}/flightplan` | FlightPlanService | 未着手 |
| 4.9 | `POST /api/scenario/load` | ScenarioController 等 | 未着手 |
| 4.10 | UranosAPI.yml 更新 | OpenAPI 仕様 | 未着手 |
| 4.11 | Phase 4 統合テスト | API エンドポイントのテスト | 未着手 |

**完了条件**: 全 API が仕様通り動作し、OpenAPI ドキュメントが更新されている

---

### Phase 5: フロントエンド対応

**目的**: UI からフライトプラン機能を操作可能にする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 5.1 | フライトプラン表示コンポーネント | 残り WP 一覧等 | 未着手 |
| 5.2 | Direct To 指示 UI | 管制指示入力 | 未着手 |
| 5.3 | Resume Navigation 指示 UI | 管制指示入力 | 未着手 |
| 5.4 | シナリオファイルアップロード | シナリオロード画面 | 未着手 |

**備考**: フロントエンドは別リポジトリ/ディレクトリのため、Backend 側は API 提供までの責任範囲

---

### Phase 6: テスト・ドキュメント

**目的**: 品質保証とドキュメント整備

| # | タスク | 状態 |
|---|--------|------|
| 6.1 | 単体テスト網羅 | 未着手 |
| 6.2 | 統合テスト | 未着手 |
| 6.3 | サンプルシナリオファイル作成 | 未着手 |
| 6.4 | Backend README.md 更新 | 未着手 |
| 6.5 | ユーザーマニュアル更新（該当する場合） | 未着手 |

---

## 推奨ブランチ

- **ブランチ名**: `feature/flight-plan`（既存ブランチありの場合は `feature/flight-plan-v2` 等）
- **ベース**: `main`

## 依存関係・注意事項

1. **AtsRouteRepository**: Fix 位置の取得に使用。`findFixPositionByName` の動作確認が必要
2. **既存 ScenarioService の設計**: 現状 `domain/model/service` にあるが、再設計提案では `application` 層への移動が検討されている。Phase 3 で拡張する際は配置を再確認する
3. **フロントエンド互換性**: 位置情報 API のレスポンス形式が変更される場合は、フロントエンド側の対応が必要

## 変更履歴

| 日付 | 変更内容 |
|------|----------|
| 2025-03-08 | 初版作成（実装計画ディレクトリ構築に伴い） |
