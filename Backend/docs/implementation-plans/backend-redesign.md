# バックエンド再設計 実装計画

## 概要

クリーンアーキテクチャ・DDD 原則の徹底を目的とした、バックエンド全体の再設計実装計画。

**参照**: [Backend README 実装の改善点](../../README.md#-継続的な改善項目優先順位付き)

## 推奨実施順序

依存関係を考慮し、以下の順で実施することを推奨する。

1. ドメイン層から Spring/config 依存を完全に除去
2. DTO を純粋なデータホルダーにし、変換を application 層に集約
3. ScenarioService を application 層へ移動
4. AtsRouteRepository のインターフェース分離と infrastructure への移動
5. AirSpace / AirSpaceImpl の整理
6. API レスポンスの JSON 化と Use Case の切り出し
7. 命名・typo・ドキュメントの修正

---

## 前提条件

- [ ] 全テストが現状 green であること
- [ ] フロントエンドとの API 互換性方針の決定（破壊的変更の許容範囲）
- [ ] フライトプラン機能など他機能との競合・依存の確認

---

## Phase 1: ドメイン層から Spring/config 依存の除去

**目的**: ドメイン層がフレームワーク・設定に依存しない純粋な Java にし、クリーンアーキテクチャを満たす

| # | タスク | 対象ファイル | 状態 |
|---|--------|--------------|------|
| 1.1 | `ConflictDetector` から `@Service` 削除 | `domain/model/service/conflict/ConflictDetector.java` | 完了 |
| 1.2 | `ScenarioServiceImpl` から `@Service` 削除、config で `@Bean` 登録 | `domain/.../ScenarioServiceImpl.java` | 完了 |
| 1.3 | `AirSpaceImpl` から `@Service` 削除 | `domain/model/aggregate/airspace/AirSpaceImpl.java` | 完了 |
| 1.4 | `AirspaceManagementImpl` から `@Configuration`, `@Scheduled` 削除 | `domain/model/aggregate/airspace/AirspaceManagementImpl.java` | 完了 |
| 1.5 | シミュレーション駆動ロジックの移動 | `@Scheduled` を config 層または application 層へ | 完了 |
| 1.6 | `AircraftBase` の `GlobalConstants.REFRESH_RATE` 依存の除去 | `shared/constants/AtcSimulatorConstants` へ移行 | 完了 |
| 1.7 | `AirspaceManagementImpl` の `GlobalVariables` 依存の除去 | `AtcSimulatorDomainConfig.SimulationScheduler` へ移動 | 完了 |
| 1.8 | Phase 1 テスト実行 | 既存テストが通ることを確認 | 完了 |

**完了条件**: ドメイン層に `org.springframework.*` および `config.globals.*` の import が残っていない

---

## Phase 2: DTO を純粋なデータホルダーに、変換を application 層に集約

**目的**: DTO がドメインオブジェクトを生成・操作しないようにし、レイヤー境界を明確にする

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 2.1 | `CreateAircraftDto.createCommercialAircraft()` を削除 | `interfaces/dto/CreateAircraftDto.java` | 完了 |
| 2.2 | DTO → ドメイン変換を application 層に実装 | `application/AircraftFactory.java` | 完了 |
| 2.3 | `ControlAircraftDto.setInstruction()` を削除 | `interfaces/dto/ControlAircraftDto.java` | 完了 |
| 2.4 | 管制指示の適用ロジックを interface 層に実装 | `ControlAircraftService` で DTO → InstructedVector 変換 | 完了 |
| 2.5 | API 層での DTO 使用を新変換ロジックに合わせて更新 | `CreateAircraftService`, `ControlAircraftService` | 完了 |
| 2.6 | Phase 2 テスト実行 | 航空機作成・管制指示のテスト | 完了 |

**完了条件**: DTO がドメインクラスへの依存を持たず、変換は application 層で完結している

---

## Phase 3: ScenarioService を application 層へ移動

**目的**: シナリオ実行をユースケースとして application 層に配置する

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 3.1 | `ScenarioService` インターフェースを `application/` へ移動 | `application/ScenarioService.java` | 完了 |
| 3.2 | `ScenarioServiceImpl` を `application/` へ移動 | `application/ScenarioServiceImpl.java` | 完了 |
| 3.3 | DTO 依存を Phase 2 の変換ロジック経由に変更 | `ScenarioServiceImpl` | 完了（Phase 2 で対応済み） |
| 3.4 | 参照元の import を更新 | `CreateAircraftService`, `ControlAircraftService` 等 | 完了 |
| 3.5 | 旧 `domain/model/service/scenario/` を削除 | 空ディレクトリの削除 | 完了 |
| 3.6 | Phase 3 テスト実行 | シナリオ関連のテスト | 完了 |

**完了条件**: `ScenarioService` が `application` パッケージにあり、`domain` から `ScenarioService` への参照がなくなっている

---

## Phase 4: AtsRouteRepository のインターフェース分離と infrastructure への移動

**目的**: Fix 位置取得をインフラ層として正しく抽象化し、 domain をインフラから分離する

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 4.1 | `FixPositionRepository` インターフェースを domain に定義 | `domain/model/entity/fix/FixPositionRepository.java` | 完了 |
| 4.2 | `AtsRouteRepository` を `FixPositionRepository` 実装に変更 | 実装クラスのリネーム・移設 | 完了 |
| 4.3 | 実装を `infrastructure/` へ移動 | `infrastructure/fix/AtsRouteFixPositionRepository.java` | 完了 |
| 4.4 | JSON パスを `ClassPathResource` 等で解決 | `src/main/resources/fix/` へ移設 | 完了 |
| 4.5 | Singleton 廃止、DI で注入 | `getInstance()` 削除、`@Bean` 登録 | 完了 |
| 4.6 | `AirspaceManagement` の依存をインターフェースに変更 | `AtsRouteRepository` → `FixPositionRepository` | 完了 |
| 4.7 | `AtsRouteService` の依存を更新 | `AtsRouteFixPositionRepository` を注入 | 完了 |
| 4.8 | Phase 4 テスト実行 | Fix 取得・Direct To の統合テスト追加 | 完了 |

**完了条件**: domain に `FixPositionRepository` インターフェースのみがあり、実装は infrastructure にある

---

## Phase 5: AirSpace / AirSpaceImpl の整理

**目的**: 未使用・重複した抽象化を整理し、AirspaceManagement に責務を集約する

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 5.1 | `AirSpace` / `AirSpaceImpl` の参照元を特定 | grep 等で確認 | 完了（参照なし） |
| 5.2 | 参照が無ければ削除 | `AirSpace.java`, `AirSpaceImpl.java` | 完了 |
| 5.3 | 参照があれば `AirspaceManagement` への移行 | 呼び出し元の変更 | N/A |
| 5.4 | Phase 5 テスト実行 | 空域管理のテスト | 完了 |

**完了条件**: `AirSpace` が使用されていない、または `AirspaceManagement` に統合されている

---

## Phase 6: API レスポンスの JSON 化と Use Case の切り出し

**目的**: テキスト形式レスポンスを JSON 化し、責務を Use Case に集約する

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 6.1 | `AircraftLocationDto` 等の JSON 用 DTO 作成 | `interfaces/dto/AircraftLocationDto.java` | 完了 |
| 6.2 | `GetAllAircraftLocationsWithRiskUseCase` の作成 | `application/` | 完了 |
| 6.3 | `LocationService` を Use Case 経由に変更 | `LocationService` が Use Case のみを呼ぶ | 完了 |
| 6.4 | レスポンス形式を JSON に変更 | `ResponseEntity<List<AircraftLocationDto>>` | 完了 |
| 6.5 | フロントエンド互換性 | Frontend `location.ts` を JSON 対応に更新 | 完了 |
| 6.6 | `ConflictAlertService` の `ConflictAlert`, `ConflictStatistics` を DTO 化 | `interfaces/dto/` へ切り出し | 未着手 |
| 6.7 | Phase 6 テスト実行 | API 統合テスト、UranosAPI 更新 | 完了 |

**完了条件**: 位置情報 API が JSON を返し、Use Case が明確に分離されている

---

## Phase 7: 命名・typo・ドキュメントの修正

**目的**: 一貫性の向上とドキュメントの正確性確保

| # | タスク | 対象 | 状態 |
|---|--------|------|------|
| 7.1 | `persistance` → `persistence` へのパッケージ名修正 | `infrastructure/persistence/` | 完了 |
| 7.2 | `NextStep()` → `nextStep()` メソッド名修正 | `AircraftRepository`, 実装クラス | 完了 |
| 7.3 | API クラス名の統一 | `*Service` → `*Controller` または `*Api` | 保留（影響範囲大） |
| 7.4 | `ControlAircraftDto` の `@NotBlank` 修正 | `int` 用に `@Min` 等へ変更 | 完了（Phase 2 で対応済み） |
| 7.5 | `javax.validation` → `jakarta.validation` 移行 | spring-boot-starter-validation 使用 | 完了 |
| 7.6 | `System.out.println` をロガーに置換 | `AtcSimulatorApplicationConfig` | 完了 |
| 7.7 | README の GeodeticUtils 配置の修正 | `shared/utility/` として明記 | 完了 |
| 7.8 | Phase 7 全体テスト実行 | リグレッション確認 | 完了 |

**完了条件**: 命名規則が統一され、ドキュメントと実装が一致している

---

## 推奨ブランチ

- **ブランチ名**: `refactor/backend-clean-architecture`
- **ベース**: `main`

## 他機能との関係

- **フライトプラン機能**: Phase 3 完了後に着手することを推奨（`ScenarioService` 配置確定後）
- **既存 API**: Phase 6 の JSON 化はフロントエンドの対応が必要な場合は段階的に実施

## 変更履歴

| 日付 | 変更内容 |
|------|----------|
| 2026-03-08 | 初版作成（バックエンド再設計調査に基づく） |
| 2026-03-08 | Phase 1, Phase 2 完了。TDD で統合テスト追加、Spring/config 依存除去、DTO 純粋化、AircraftFactory 追加 |
| 2026-03-08 | Phase 3 完了。ScenarioService / ScenarioServiceImpl を application 層へ移動 |
| 2026-03-08 | Phase 4 完了。FixPositionRepository インターフェース分離、AtsRouteFixPositionRepository を infrastructure へ移動、JSON を ClassPathResource 化 |
| 2026-03-08 | Phase 5 完了。未使用の AirSpace / AirSpaceImpl を削除 |
| 2026-03-08 | Phase 6 完了。AircraftLocationDto・GetAllAircraftLocationsWithRiskUseCase 追加、位置情報 API を JSON 化、Frontend を JSON 対応に更新 |
| 2026-03-08 | Phase 7 完了。persistance→persistence、NextStep→nextStep、System.out→logger、jakarta.validation、README 修正 |
