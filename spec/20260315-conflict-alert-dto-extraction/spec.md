# ConflictAlertService の DTO 化

## メタデータ

- **Status**: Draft
- **Date**: 2026-03-15
- **関連 Issue**: [#74](https://github.com/Futty93/Horus/issues/74)（spec 技術的負債 T-1）
- **親 spec**: [backend-redesign Phase 6.6](../../spec/20260308-backend-redesign/spec.md)

## 概要

`ConflictAlertService` 内の `ConflictAlert` および `ConflictStatistics` を `interfaces/dto/` へ切り出し、API 境界をレイヤー責務に沿って明確化する。backend-redesign Phase 2 の「DTO は純粋なデータホルダー、ドメイン依存を持たない」方針に合わせて実施する。

## 背景・課題

### 現状

- `ConflictAlert` と `ConflictStatistics` は `ConflictAlertService` の `public static` 内部クラスとして定義されている
- `ConflictAlert` はドメインの `RiskAssessment` を保持し、そのまま API レスポンスとして返している
- `ConflictAlertController` が `ConflictAlertService.ConflictAlert` を直接参照している
- application 層の型が interfaces 層（Controller）に漏れている

```
ConflictAlertController → ConflictAlertService.ConflictAlert (application 内部型)
                        → ConflictAlertService.ConflictStatistics (application 内部型)
```

### 課題（Problem Statement）

- **レイヤー逆転**: interfaces 層が application 層の内部実装詳細（内部クラス）に依存している
- **API コントラクトの曖昧さ**: レスポンス形式が `application` パッケージ内で定義され、API 契約の所在が不明瞭
- **Phase 2 方針との不整合**: backend-redesign Phase 2 で「DTO はドメインに依存しない」としているが、`ConflictAlert` は `RiskAssessment` を直接保持している

### なぜ今か（Motivation）

- backend-redesign Phase 6.6 の未着手タスクであり、既存 DTO（`AircraftLocationDto` 等）と同じ方針で API 層を整理する
- 4-3（管制間隔違反通知の UI 接続）や将来の拡張で `ConflictAlertService` を参照する際、クリーンな境界が役立つ
- 作業量が少なく（難易度 ★☆☆）、技術的負債解消の効果が大きい

---

## 方針

### 決定方針（Decision）

1. **`ConflictAlertDto`** を `interfaces/dto/` に新規作成する  
   - `pairId` と、`RiskAssessment` の公開フィールドをフラットに展開（`riskLevel`, `alertLevel`, `timeToClosest`, `closestHorizontalDistance`, `closestVerticalDistance`, `isConflictPredicted`）  
   - `getCallsigns()` 相当の `callsigns` 配列を DTO に持たせるか、`pairId` のみにしてクライアントで split するかは既存利用箇所に依存

2. **`ConflictStatisticsDto`** を `interfaces/dto/` に新規作成する  
   - 既存 `ConflictStatistics` のフィールドをそのまま record または POJO として移す（ドメイン依存なし）

3. **`ConflictAlertService`** は `RiskAssessment` から `ConflictAlertDto` への変換を application 層内で行い、戻り値の型を DTO に変更する

4. **`ConflictAlertController`** の戻り値型を `ConflictAlertDto` / `ConflictStatisticsDto` に変更する

### DTO 化のメリット

- **レイヤー分離**: interfaces 層は `interfaces/dto` のみに依存し、application 層の内部型を直接参照しなくなる
- **API コントラクトの明確化**: レスポンス形式が `interfaces/dto` に集約され、フロントエンドとの契約がコード上で明示される
- **変更耐性**: ドメイン（`RiskAssessment`）の内部変更が API に波及しにくくなる。DTO のフィールド変更は意図的に行う
- **テスト容易性**: DTO 単体のシリアライズ・構造のテストがしやすい
- **一貫性**: 既存の `AircraftLocationDto` 等と同様、API 境界が `interfaces/dto` に統一される

### なぜこの選択をしたか

- **Phase 2 方針への準拠**: 「DTO がドメインクラスへの依存を持たず」という決定に従い、`RiskAssessment` を DTO 内に保持せず、必要なフィールドをフラットに展開する
- **既存 API 互換性**: フロントエンドが `riskLevel` 等のフィールドを参照している場合、JSON 構造を維持すれば破壊的変更にならない
- **最小変更**: `ConflictStatistics` は元々ドメイン依存がなく、そのまま DTO に移すだけ。`ConflictAlert` のみ変換ロジックを追加する

### 検討した他案（Alternatives Considered）

- **案 A: 内部クラスをそのまま `interfaces/dto` に切り出すだけ（`RiskAssessment` をそのまま保持）**  
  採用しなかった理由: ドメイン漏れが残り、Phase 2 方針に反する。Jackson による `RiskAssessment` のシリアライズに依存し、ドメイン変更の影響を受けやすい

- **案 B: `RiskAssessmentDto` を別途作成し、`ConflictAlertDto` で保持**  
  採用しなかった理由: ネスト構造が増え、フロントエンドの利用形態を考慮するとフラットな方がシンプル。`RiskAssessment` は現状 `ConflictAlert` 経由でしか API に登場しないため、別 DTO は過剰

- **案 C: 本 spec の採用方針 — フラットな `ConflictAlertDto`**  
  採用理由: レイヤー境界を満たしつつ、既存 API 構造と整合し、変更量を抑えられる

### トレードオフ（Trade-offs）

- **メリット**: レイヤー分離、API 契約の明確化、将来の拡張への耐性
- **デメリット / 受容する制約**: `RiskAssessment` → `ConflictAlertDto` の変換コードが application 層に増える。フィールド追加時に DTO と変換の両方を更新する必要がある

---

## 完了条件（Success Criteria）

- [ ] `ConflictAlertDto` が `interfaces/dto/` に存在し、`RiskAssessment` に依存していない
- [ ] `ConflictStatisticsDto` が `interfaces/dto/` に存在する
- [ ] `ConflictAlertService` の `getCriticalAlerts`, `getSeparationViolationAlerts`, `getAircraftConflicts`, `getConflictStatistics` が DTO を返す
- [ ] `ConflictAlertController` が `ConflictAlertService.*` の内部型を参照していない
- [ ] 既存の統合テスト・API レスポンス形式が維持されている（破壊的変更なし）

---

## 影響範囲

- **`interfaces/dto/ConflictAlertDto.java`**: 新規作成
- **`interfaces/dto/ConflictStatisticsDto.java`**: 新規作成
- **`application/ConflictAlertService.java`**: 内部クラス削除、戻り値型を DTO に変更、`RiskAssessment` → `ConflictAlertDto` 変換ロジック追加
- **`interfaces/api/ConflictAlertController.java`**: 戻り値型と import の変更
- **`GetAllAircraftLocationsWithRiskUseCase`**: `getAllConflictAlerts()` の戻り値は `Map<String, RiskAssessment>` のまま（内部使用のため変更不要）
- **`BackendRedesignIntegrationTest`**: `ConflictAlert` / `ConflictStatistics` の参照を DTO に変更

---

## 実装計画

### Phase 1: DTO 作成と Service 修正

1. `ConflictStatisticsDto` を `interfaces/dto/` に作成（record 推奨）
2. `ConflictAlertDto` を `interfaces/dto/` に作成。`pairId` および `RiskAssessment` のフラット化フィールドを持つ
3. `ConflictAlertService` に `toConflictAlertDto(RiskAssessment)` の private 変換メソッドを追加
4. `ConflictAlertService` の各メソッドの戻り値を DTO に変更し、内部クラスを削除
5. `ConflictAlertController` の戻り値型と import を更新
6. `ConflictAlertController.HealthStatus` は本 spec のスコープ外（必要であれば別タスクで DTO 化）

### Phase 2: テスト・検証

1. `BackendRedesignIntegrationTest` の参照を DTO に更新
2. `./gradlew test` および `spotlessCheck` が通ることを確認
3. 既存 API の JSON レスポンス形式が変わっていないことを手動またはテストで確認

---

## 検証

- [ ] `./gradlew test` が通る
- [ ] `./gradlew spotlessCheck` が通る
- [ ] `/api/conflict/critical`, `/api/conflict/statistics` 等のレスポンスが既存フォーマットと互換であること

---

## 未解決事項（Unresolved Questions）

- `ConflictAlertController.HealthStatus` を DTO 化するか（本 spec では対象外とし、必要に応じて別 issue で対応）
- `ConflictAlertDto` に `callsigns: String[]` を追加するか、`pairId` のみでクライアントに任せるか — 既存フロントエンドの利用状況で判断

---

## 関連ドキュメント

- [backend-redesign spec Phase 6](../../spec/20260308-backend-redesign/spec.md)
- [backend-redesign spec Phase 2（DTO 方針）](../../spec/20260308-backend-redesign/spec.md#phase-2-dto-を純粋なデータホルダーに変換を-application-層に集約)
- [実装計画 spec 技術的負債 T-1](../../spec/spec.md)
- [GitHub Issue #74](https://github.com/Futty93/Horus/issues/74)
