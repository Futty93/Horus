# フライトプラン機能 実装計画

## 概要

航空機がフライトプランに従って自動飛行し、管制官が Direct To・Resume Navigation 等の指示を出せるようにする機能の実装計画。

**設計ドキュメント**: [flight-plan-implementation.md](../../Backend/docs/design/flight-plan-implementation.md)

## 前提条件

- [x] 設計ドキュメントのレビュー完了
- [x] `AircraftBase`、`CommercialAircraft`、`ScenarioServiceImpl` の現状把握
- [x] 既存の `FixPositionRepository`（Fix 位置取得）の動作確認
- [x] フロントエンドとの API 互換性確認（既存 `/aircraft/location` 等）

---

## 再調査結果（2026-03-08）

### 現状コードベースの確認

| コンポーネント | 状態 | 備考 |
|----------------|------|------|
| `AircraftBase` | フライトプラン関連フィールドなし | `instructedVector` のみで管制指示を反映。`calculateNextAircraftVector` で `flightBehavior` に委譲 |
| `ScenarioService` | `directFixAircraft(callsign, fixName)` のみ | Fix へのヘディング指示のみ。フライトプラン再開オプションなし |
| `ScenarioServiceImpl.directFixAircraft` | Fix 方位を `InstructedVector` に設定 | `AirspaceManagement.getFixPosition` → `FixPositionRepository.findFixPositionByName` 経由 |
| `FixPositionRepository` | `findFixPositionByName(String)` | `AtsRouteFixPositionRepository` が waypoints/radio_nav/ats_routes から検索。設計書の WP 名（SPENS, ABENO 等）は waypoints.json に存在 |
| `FlightBehavior` | `calculateNextHeading(current, target, maxTurnRate)` | 目標方位への旋回。FLIGHT_PLAN 時は「次 WP への方位」を target として渡せばよい |
| `CommercialAircraft` | `AircraftBase` を継承 | 特別な飛行動作なし。フライトプランは基底で扱える |

### 最低限からのスタート方針

**MVP（最小可行産物）として実装する範囲：**

1. **Phase 1**: ドメインモデルは設計通り実装（拡張の土台のため省略不可）
2. **Phase 2**: FLIGHT_PLAN による自動ナビゲーションのみ。高度・速度制約は Phase 2.5 で後付可能
3. **Phase 3**: `directFixAircraft` を拡張して `resumeFlightPlan` を付加。新規 `resumeNavigation` を追加。既存 `instructAircraft` はヘディング指示時に `HEADING` へ遷移させる
4. **Phase 4**: まず `spawn-with-flightplan` と `direct-to` / `resume-navigation` を優先。シナリオロードは後回し可

### 発展・拡張性の評価

| 観点 | 評価 | 理由 |
|------|------|------|
| **WaypointAction** | ◎ | `HOLD`, `HANDOFF` は enum で予約済み。将来的に `REMOVE_AIRCRAFT` と同様の分岐追加で対応可能 |
| **AltitudeConstraint** | ◎ | `BETWEEN` 等の拡張余地あり。`AT`/`AT_OR_ABOVE`/`AT_OR_BELOW` でまずは十分 |
| **NavigationMode** | ◎ | 3 モードで状態遷移が明確。将来的なモード追加も enum 拡張で対応 |
| **通過判定** | ○ | 距離ベース方式は FMS に近く調整しやすい。動的しきい値の定数（5秒、0.5–3.0 NM）は設定化可能 |
| **FixPosition 依存** | ○ | `FixPositionRepository` は既に Clean Architecture で分離済み。waypoints.json の形式変更時も infrastructure のみ修正 |
| **API 設計** | ◎ | 設計書のエンドポイントは RESTful。シナリオロードは `aircraft[]` を拡張する形で自然に統合可能 |

### 実装計画への補足

- **Phase 3.1**: `directToFix(callsign, fixName, resumeFlightPlan)` は新規メソッド。既存 `directFixAircraft` は `resumeFlightPlan=false` 相当として内部で統合するか、ラッパーとして残すか要検討
- **Phase 2.5（高度・速度制約）**: MVP では `InstructedVector` へ target を設定するのみでよい。厳密な制約チェック（AT_OR_ABOVE 等）は段階的に追加可能

## 実装 Phase 一覧

### Phase 1: ドメインモデル実装（基盤）

**目的**: フライトプラン機能の基盤となる値オブジェクト・ドメインクラスを実装

| # | タスク | ファイル | 状態 |
|---|--------|----------|------|
| 1.1 | `NavigationMode` enum 作成 | `domain/model/entity/flightplan/NavigationMode.java` | 完了 |
| 1.2 | `AltitudeConstraint` enum 作成 | `domain/model/entity/flightplan/AltitudeConstraint.java` | 完了 |
| 1.3 | `WaypointAction` enum 作成 | `domain/model/entity/flightplan/WaypointAction.java` | 完了 |
| 1.4 | `FlightPlanWaypoint` クラス作成 | `domain/model/entity/flightplan/FlightPlanWaypoint.java` | 完了 |
| 1.5 | `FlightPlan` クラス作成 | `domain/model/entity/flightplan/FlightPlan.java` | 完了 |
| 1.6 | Phase 1 ユニットテスト | `FlightPlanDomainModelTest` | 完了 |

**完了条件**: 全ドメインモデルが単体で正しく動作し、テストが通る

---

### Phase 2: 航空機へのナビゲーション機能追加

**目的**: 航空機がフライトプランに従ってウェイポイントを順次通過するようにする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 2.1 | `Aircraft` インターフェース拡張 | `shouldBeRemovedFromSimulation` 追加 | 完了 |
| 2.2 | `AircraftBase` フィールド追加 | flightPlan, currentWaypointIndex, navigationMode 等 | 完了 |
| 2.3 | ウェイポイント通過判定ロジック | `applyWaypointPassCheck`, `calculateDynamicThreshold` | 完了 |
| 2.4 | `calculateNextAircraftVector` 拡張 | FLIGHT_PLAN / DIRECT_TO 時の自動ナビゲーション | 完了 |
| 2.5 | 高度・速度制約の自動適用 | `resolveTargetAltitude`, `resolveTargetSpeed` | 完了 |
| 2.6 | `CommercialAircraft` 対応 | super 呼び出しで自動継承 | 完了 |
| 2.7 | Phase 2 ユニットテスト | `FlightPlanNavigationTest` | 完了 |

**完了条件**: フライトプラン付き航空機がウェイポイントを順次通過して飛行する

---

### Phase 3: 管制指示の拡張

**目的**: Direct To、Resume Navigation 等の管制指示を処理できるようにする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 3.1 | `ScenarioService` インターフェース拡張 | `directToFix`（resumeFlightPlan 付き）, `resumeNavigation` | 完了 |
| 3.2 | `ScenarioServiceImpl` 実装 | Direct To ロジック | 完了 |
| 3.3 | `ScenarioServiceImpl` 実装 | Resume Navigation ロジック | 完了 |
| 3.4 | `instructAircraft` 拡張 | ヘディング指示時に NavigationMode → HEADING | 完了 |
| 3.5 | `directFixAircraft` との連携 | `directToFix(callsign, fixName, false)` に委譲 | 完了 |
| 3.6 | Phase 3 ユニットテスト | `ScenarioServiceFlightPlanTest` | 完了 |

**完了条件**: 管制指示により航空機のナビゲーションモードが正しく切り替わる

---

### Phase 4: API・DTO 実装

**目的**: REST API 経由でフライトプラン機能を利用可能にする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 4.1 | `FlightPlanDto` 作成 | interfaces/dto | 完了 |
| 4.2 | `FlightPlanWaypointDto` 作成 | interfaces/dto | 完了 |
| 4.3 | `SpawnWithFlightPlanDto` 作成 | interfaces/dto | 完了 |
| 4.4 | `POST /api/aircraft/spawn-with-flightplan` | FlightPlanController | 完了 |
| 4.5 | `POST /api/aircraft/{callsign}/flightplan` | FlightPlanController | 完了 |
| 4.6 | `POST /api/aircraft/{callsign}/direct-to` | FlightPlanController | 完了 |
| 4.7 | `POST /api/aircraft/{callsign}/resume-navigation` | FlightPlanController | 完了 |
| 4.8 | `GET /api/aircraft/{callsign}/flightplan` | FlightPlanController | 完了 |
| 4.9 | `POST /api/scenario/load` | ScenarioController | 完了 |
| 4.10 | UranosAPI.yml 更新 | OpenAPI 仕様 | 完了 |
| 4.11 | Phase 4 統合テスト | FlightPlanApiIntegrationTest | 完了 |

**完了条件**: 全 API が仕様通り動作し、OpenAPI ドキュメントが更新されている

---

### Phase 5: フロントエンド対応

**目的**: UI からフライトプラン機能を操作可能にする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 5.1 | フライトプラン表示コンポーネント | `FlightPlanDisplay`（残り WP・ナビモード） | 完了 |
| 5.2 | Direct To 指示 UI | `FlightPlanControl` 内、SelectFixMode も新 API 対応 | 完了 |
| 5.3 | Resume Navigation 指示 UI | `FlightPlanControl` 内 | 完了 |
| 5.4 | シナリオファイルアップロード | 未着手（後回し） | 未着手 |

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

1. **FixPositionRepository**: Fix 位置の取得に使用。`AirspaceManagement.getFixPosition` が `findFixPositionByName` を呼び出し。waypoints.json に WP 名が存在することを前提
2. **既存 ScenarioService の設計**: `application` パッケージに配置済み（[backend-redesign](../backend-redesign/spec.md) Phase 3 完了）
3. **フロントエンド互換性**: 位置情報 API のレスポンス形式が変更される場合は、フロントエンド側の対応が必要
4. **FlightPlan 構築時の Fix 解決**: `FlightPlan` / `FlightPlanWaypoint` 作成時に `FixPositionRepository` で position を解決する必要あり。`AircraftFactory` 等の application 層で変換する想定

## 変更履歴

| 日付 | 変更内容 |
|------|----------|
| 2026-03-08 | 初版作成（実装計画ディレクトリ構築に伴い） |
| 2026-03-08 | 再調査結果・最低限スタート方針・拡張性評価を追加。FixPositionRepository に修正 |
| 2026-03-08 | Phase 1 完了。ドメインモデル・ユニットテスト実装。FixPosition に equals/hashCode 追加 |
| 2026-03-08 | Phase 2 完了。AircraftBase にフライトプラン・ナビゲーション機能、REMOVE_AIRCRAFT 時削除 |
| 2026-03-08 | Phase 3 完了。ScenarioService に directToFix/resumeNavigation、instructAircraft で HEADING 設定 |
| 2026-03-08 | Phase 4 完了。FlightPlanController、DTO、FlightPlanFromDtoConverter、UranosAPI.yml 更新 |
| 2026-03-08 | Phase 5 完了。FlightPlanDisplay、FlightPlanControl、flightPlan API、SelectFixMode 新 API 対応 |
