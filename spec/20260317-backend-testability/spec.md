# Backend テスタビリティの向上

## メタデータ

- **Status**: Draft
- **Date**: 2026-03-17
- **関連**: [spec/spec.md](../spec.md) 技術的負債 T-5

## 概要

Backend のテスト容易性を高めるため、インターフェース設計の見直し、モック化容易性の向上、テストヘルパーの充実を実施する。単体テスト・統合テストの追加・維持を効率化し、リファクタリングの安全網を強化する。

## 背景・課題

### 現状

#### インターフェース設計

- **interface がある**: `AircraftRadarService`, `ScenarioService`, `AircraftRepository`, `FixPositionRepository`
- **interface がない**: `ConflictAlertService`, `ConflictDetector`, `RouteSuggestionService`, `GetAllAircraftLocationsWithRiskUseCase`

application 層で interface の有無が混在している。

#### 依存関係の問題

- `RouteSuggestionService` が domain の `FixPositionRepository` ではなく、infrastructure の `AtsRouteFixPositionRepository` に直接依存している
- `FixPositionRepository` には `findFixPositionByName` のみ定義されており、経路提案で必要な `getWaypoints()`, `getAtsLowerRoutes()`, `findAirportPositionByIcao()` などが interface に存在しない
- `SimulationService` が `GlobalVariables` を static 参照しており、テストでシミュレーション状態を差し替えられない

#### モック化の現状

- `ConflictDetector` は具象クラスだが Mockito でモック可能。`ConflictAlertServiceTest` で `@Mock ConflictDetector` を使用
- `RouteSuggestionServiceTest` では `mock(AtsRouteFixPositionRepository.class)` を使用。モックは動作するが、application が infrastructure に依存する設計のため DIP に反する

#### テストヘルパー

- `RouteSuggestionServiceTest` 内の private static メソッド `wp()`, `route()`, `rp()` で `Waypoint`, `Route`, `RoutePoint` を生成
- `ConflictAlertServiceTest` では各テストで `new RiskAssessment(...)` を直接記述
- `Aircraft`, `Waypoint`, `Route` 等のテストデータを共通生成する `TestFixtures` や `TestDataFactory` は存在しない
- `AircraftRepositoryInMemory` へ事前データ投入するヘルパーもない

### 課題（Problem Statement）

- **一貫性の欠如**: 同層のサービスで interface の有無がばらつき、テスタビリティに差がある
- **依存性逆転の原則違反**: application が infrastructure の具象クラスに依存し、モック差し替えの意図が設計に反映されていない
- **static 依存**: `GlobalVariables` の直接参照により、テスト間の分離や状態の制御が困難
- **テストデータの重複**: 各テストで同様のオブジェクト構築コードが散在し、変更時の修正箇所が増える

### なぜ今か（Motivation）

- spec 技術的負債 T-5 として計画済み
- フェーズ 2 以降の機能追加（STCA UI、管制指示拡充など）でテストが増える見込み。基盤整備を早めることで後続のテスト追加コストを下げる
- `ConflictAlertService` や `RouteSuggestionService` は既に単体テストがあるため、改善の影響範囲が把握しやすい

---

## 方針

### 決定方針（Decision）

#### 1. インターフェース設計の見直し

| 対象 | 方針 |
|------|------|
| `ConflictAlertService` | interface を定義し、`ConflictAlertServiceImpl` に実装を移す。既存の `ConflictAlertService` は実装クラスにリネーム |
| `ConflictDetector` | interface `ConflictDetector` を domain 層に定義し、既存実装を `ConflictDetectorImpl` 等にリネーム |
| `RouteSuggestionService` | domain に `RouteDataRepository`（または既存拡張）を定義し、`getWaypoints()`, `getAtsLowerRoutes()`, `findAirportPositionByIcao()` を含める。`RouteSuggestionService` はこの interface に依存 |
| `SimulationService` | `SimulationStateProvider` 等の interface を導入し、`GlobalVariables` への直接参照を抽象化。テスト用実装で状態を差し替え可能にする |

#### 2. モック化容易性の向上

- 上記 interface 化により、テストでのモック差し替えが明示的になる
- 依存性注入（コンストラクタインジェクション）を徹底し、`@Mock` での差し替えを標準化
- `config/TestConfig.java` や `@MockBean` による統合テスト用のモック設定を整備

#### 3. テストヘルパーの充実

- `test/` 配下に `TestFixtures`（または `test-fixtures` サブプロジェクト）を用意
- 提供するヘルパー:
  - `Waypoint wp(String name, double lat, double lon)` — `RouteSuggestionServiceTest` の `wp()` を抽出
  - `Route route(String name, RoutePoint... points)` — 同上
  - `RiskAssessment riskAssessment(double riskLevel, ...)` — `RiskAssessment` のデフォルト値付きファクトリ
  - `AircraftTestHelper` — `AircraftRepositoryInMemory` へ航空機を投入するヘルパー、または `Aircraft` ビルダー

### 検討した他案（Alternatives Considered）

- **案 A: interface 化を最小限にする（ConflictDetector のみ interface 化）**
  採用しなかった理由: 他サービスも同様の課題を抱えており、段階的に対応するにせよ計画としては全体像を示す方がよい

- **案 B: test-fixtures を Gradle の java-test-fixtures プラグインで別ソースセットにする**
  採用しなかった理由: 現状のテスト数・規模では overkill。`test/` 配下のユーティリティクラスで十分。必要になったら移行を検討

- **案 C: SimulationService の状態管理をそのまま維持する**
  採用しなかった理由: `GlobalVariables` の static 参照はテストの並列実行や分離の障壁になる。abstract 化の工数は小さいため実施する

### トレードオフ（Trade-offs）

- **メリット**:
  - 新規テスト追加時のセットアップが簡潔になる
  - リファクタリング時のテストの信頼性向上
  - 依存関係が明確になり、変更影響範囲の把握が容易になる

- **デメリット / 受容する制約**:
  - 一時的に interface と実装クラスの対応が増え、クラス数が増える
  - `RouteDataRepository` の導入に伴い、既存 `FixPositionRepository` との役割分担・リネームの検討が必要

---

## 完了条件（Success Criteria）

- [ ] `ConflictAlertService`, `ConflictDetector`, `RouteSuggestionService` が interface に依存する形でテスト可能である
- [ ] `TestFixtures`（または相当）で `Waypoint`, `Route`, `RiskAssessment` のファクトリが利用可能である
- [ ] 既存の単体テストがすべて通り、リグレッションがない
- [ ] `RouteSuggestionService` が domain 定義の repository interface に依存する（infrastructure の具象クラスに直接依存しない）
- [ ] `SimulationService` のテストで、シミュレーション状態をモックで制御可能である（任意・Phase 2）

---

## 影響範囲

- **application/**: `ConflictAlertService`, `ConflictAlertServiceImpl`, `RouteSuggestionService`, `GetAllAircraftLocationsWithRiskUseCase`（依存注入の見直し）
- **domain/model/service/conflict/**: `ConflictDetector` → interface + `ConflictDetectorImpl`
- **domain/model/entity/fix/**: `FixPositionRepository` の拡張、または `RouteDataRepository` の新規
- **infrastructure/fix/**: `AtsRouteFixPositionRepository` が新しい interface を実装
- **interfaces/api/**: `SimulationService` の依存注入変更（Phase 2）
- **config/**: `AtcSimulatorDomainConfig` の Bean 定義更新
- **test/**: 各テストクラスの import・コンストラクタ呼び出しの修正、`TestFixtures` の新規

---

## 実装計画

### Phase 1: インターフェース化（ConflictDetector, ConflictAlertService）

1. `ConflictDetector` の interface 化
   - `domain/model/service/conflict/ConflictDetector` を interface に変更
   - 既存実装を `ConflictDetectorImpl` にリネーム
   - `AtcSimulatorDomainConfig` の Bean 定義を更新
   - `ConflictAlertServiceTest` の `@Mock ConflictDetector` はそのまま有効

2. `ConflictAlertService` の interface 化
   - `ConflictAlertService` を interface に、実装を `ConflictAlertServiceImpl` に分離
   - Controller や UseCase の依存を interface に変更

### Phase 2: RouteSuggestionService の依存逆転

3. `RouteDataRepository`（または `FixPositionRepository` 拡張）の設計
   - `getWaypoints()`, `getAtsLowerRoutes()`, `getRnavRoutes()`, `findAirportPositionByIcao()` を interface に定義
   - `AtsRouteFixPositionRepository` がこの interface を実装（既に実装済みのメソッドを interface に合わせる）

4. `RouteSuggestionService` の依存を interface に変更
   - コンストラクタで `RouteDataRepository` を受け取る形に
   - `RouteSuggestionServiceTest` のモック型を interface に変更

### Phase 3: テストヘルパーの整備

5. `TestFixtures` クラスの作成
   - `wp()`, `route()`, `rp()` を `RouteSuggestionServiceTest` から抽出
   - `riskAssessment()` ファクトリを追加
   - 既存テストを `TestFixtures` 利用にリファクタ

6. （任意）`AircraftTestHelper` の追加
   - `AircraftRepositoryInMemory` への投入ヘルパー
   - 統合テストでの利用を想定

### Phase 4: SimulationService の状態抽象化（任意）

7. `SimulationStateProvider` の導入
   - `isSimulationRunning()` 等の interface を定義
   - `GlobalVariables` をラップする実装と、テスト用のモック可能実装を用意
   - `SimulationService` を `SimulationStateProvider` に依存する形に変更

---

## 検証

- [ ] `./gradlew test` が通る
- [ ] `./gradlew spotlessCheck` が通る
- [ ] 既存の統合テスト（`BackendRedesignIntegrationTest` 等）が通る
- [ ] 手動でアプリケーション起動・主要フローが動作することを確認

---

## 未解決事項（Unresolved Questions）

- `FixPositionRepository` を拡張するか、`RouteDataRepository` を別 interface として新設するか。既存の `FixPositionRepository` の利用箇所との関係を確認して決定する
- `GetAllAircraftLocationsWithRiskUseCase` の interface 化は Phase 1 に含めるか、別 Phase にするか

---

## 関連ドキュメント

- [spec/spec.md](../spec.md) — 技術的負債 T-5
- [Backend README](../../Backend/README.md)
- [spec/20260308-backend-redesign/spec.md](../20260308-backend-redesign/spec.md) — 全体設計方針
