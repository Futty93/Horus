# T-3 — フライトプラン経路のテスト・サンプル・README整備

## メタデータ

- **Status**: Draft
- **Date**: 2026-05-09
- **親ロードマップ**: [spec/spec.md](../spec.md)（技術的負債 **T-3**、推奨着手順序 2）
- **関連 Issue**: [#76](https://github.com/HorusATC/Horus/issues/76)
- **親機能 spec**: [20260308-flight-plan Phase 6](../20260308-flight-plan/spec.md)（テスト・ドキュメント）

## 概要

フライトプラン・シナリオロード周りについて、**テストの網羅・Swagger/手動用サンプル JSON・README/MANUAL_TEST の単一の真実**を揃える。コード先行で一部は既にあるため、**現状棚卸し → ギャップにだけ着手**する。

## 背景・課題

### 現状（2026-05-09 時点のコードベース）

| 領域 | 既にあるもの |
|------|----------------|
| **統合テスト** | `Backend/.../FlightPlanApiIntegrationTest.java`（spawn、load、direct-to、resume、異常系など） |
| **単体・ドメイン** | `FlightPlanDomainModelTest`、`FlightPlanNavigationTest`、`ScenarioServiceFlightPlanTest`、`CommercialAircraftFlightPlanRegressionTest` |
| **サンプル JSON** | `Backend/docs/test-data/`（`spawn-with-flightplan-*.json`、`direct-to-sample.json`、`assign-flightplan-sample.json` 等）と [README](../../Backend/docs/test-data/README.md)、[MANUAL_TEST](../../Backend/docs/test-data/MANUAL_TEST.md) |
| **OpenAPI** | `Backend/UranosAPI.yml` にフライトプラン系・`POST /api/scenario/load` が記載されている想定（変更時は本 spec の完了条件に追随） |

### 課題（Problem Statement）

- [20260308-flight-plan Phase 6](../20260308-flight-plan/spec.md) は **6.1〜6.5 が「未着手」のまま**だが、上記のとおり **実装・テスト・サンプルは部分済み**。計画と現場の認識がずれる。
- **網羅の空白**が明示されていない（例: `POST /api/scenario/load` の Swagger 用最小サンプル、`POST .../flightplan` の統合テストの有無）ため、退行時に「どこまで守れているか」が分かりにくい。
- **Backend README** のフライトプラン API 説明が、test-data / 統合テストと **一本化されていない**可能性がある。

### なぜ今か（Motivation）

- [spec/spec.md](../spec.md) の推奨着手順序で Phase 1 完了後の **次候補が T-3**。
- Phase 4（Conflict UI）などフロント変更の前に、**バックエンド契約と手動手順を固める**と検証コストが下がる。

---

## 方針

### 決定方針（Decision）

1. **棚卸しファースト**: 既存テストクラスと `Backend/docs/test-data` を一覧化し、[20260308-flight-plan Phase 6](../20260308-flight-plan/spec.md) の各行を **実装済 / 部分 / 未** にマッピングする。
2. **Must-have は「ギャップの明示 + 最小追加」**: 高リスク API（`scenario/load`、spawn-with-flightplan、direct-to、resume）で **統合テストに無い経路**があればテストを追加する。サンプル JSON は **MANUAL_TEST / README で参照されるもの**を優先。
3. **ドキュメント同期**: 追加・変更したテスト・サンプルに合わせて **Backend README** および **test-data の README / MANUAL_TEST** を更新する（プロジェクトルールに従い、README 変更は本 spec のスコープに含める）。
4. **親 spec の更新**: Phase 6 の表を本 spec の結果に合わせて **状態とリンク**を更新する（同一 PR または直後 PR）。

### 検討した他案（Alternatives Considered）

- **案 A: Phase 4（Conflict UI）を先にやる**  
  採用しなかった理由（本スプリントの「次」として）: ロードマップ上 T-3 が先で、契約テストの土台がまだ spec 上曖昧。
- **案 B: カバレッジ 100% を目標にする**  
  採用しなかった理由: コストが高い。本 spec は **回帰に効くギャップ**に限定する。

### トレードオフ（Trade-offs）

- **メリット**: フライトプラン変更の PR が安全になる。新メンバーが Swagger と手順で追える。
- **デメリット / 受容する制約**: E2E（ブラウザ）は本 specの Must-have に含めず、**MANUAL_TEST の手順更新**でカバーする。

---

## 完了条件（Success Criteria）

### Must-have

- [ ] **カバレッジマトリクス**: 本 spec に「API またはユースケース × テスト種別（単体/統合）」の表があり、主要フライトプラン API が **どのテストで守られているか**が分かる（既存のみでも可。空白は「追加予定」または Issue 化）。
- [ ] **`POST /api/scenario/load`**: `Backend/docs/test-data/` に **Swagger 用の最小サンプル JSON**（または既存ファイルへの明記）があり、MANUAL_TEST または test-data README から **コピペ手順**で辿れる。
- [ ] **親 spec 整合**: [20260308-flight-plan Phase 6](../20260308-flight-plan/spec.md) の 6.1〜6.5 を実態に合わせ更新（チェック済み項目・本 spec への参照）。
- [ ] **Backend README**: フライトプラン / `scenario/load` / test-data への導線が [Backend/README.md](../../Backend/README.md) にあり、パスとコマンドが正しい。

### Should-have

- [ ] 統合テストで **未カバー**のフライトプラン API があれば 1 本以上追加（理由を表に記載）。
- [ ] [#76](https://github.com/HorusATC/Horus/issues/76) に進捗コメントまたは Close 理由を記載。

### Optional

- [ ] Frontend README にフライトプラン BFF パスとローカル検証手順の短い節を追加。
- [ ] JaCoCo 等の数値目標（しきい値）は別 spec で検討。

---

## 影響範囲

- **Backend**: `src/test/java/.../FlightPlanApiIntegrationTest.java` 等、`docs/test-data/*`、`UranosAPI.yml`（サンプルと乖離している場合のみ）
- **ドキュメント**: `Backend/README.md`、`Backend/docs/test-data/README.md`、`Backend/docs/test-data/MANUAL_TEST.md`、`spec/20260308-flight-plan/spec.md`

---

## 実装計画

### Phase 1 — 棚卸し（コード変更なし可）

| # | タスク | 出力 |
|---|--------|------|
| 1.1 | `FlightPlanApiIntegrationTest` の `@DisplayName` / メソッドを一覧 | マトリクス初稿 |
| 1.2 | `docs/test-data` の各 JSON と対応 API を対応づけ | マトリクス初稿 |
| 1.3 | [20260308-flight-plan Phase 6](../20260308-flight-plan/spec.md) の各行に 1.1–1.2 をマッピング | ギャップリスト |

### Phase 2 — テスト・サンプル（ギャップから）

| # | タスク | 例 |
|---|--------|-----|
| 2.1 | `scenario/load` 用サンプル（複数機・最小機数）を test-data に追加し README に掲載 | `scenario-load-minimal.json` 等 |
| 2.2 | マトリクス上の空白を埋める統合テスト | 優先度は load → spawn → direct-to → resume → GET flightplan |

### Phase 3 — README / 親 spec

| # | タスク |
|---|--------|
| 3.1 | Backend README の API・検証セクション更新 |
| 3.2 | MANUAL_TEST の手順とサンプルファイル名を一致 |
| 3.3 | flight-plan spec Phase 6 の状態更新と本 spec へのリンク |

---

## 検証

- [ ] `./gradlew test`（Backend）が通る
- [ ] 本 spec の Must-have チェックボックスをすべて満たす
- [ ] 任意: `MANUAL_TEST.md` の該当節を 1 通り手動実行

---

## 未解決事項（Unresolved Questions）

- `POST /api/aircraft/{callsign}/flightplan`（機体への付与）を統合テストで常時回すか、負荷・前提（既存機スポーン）次第で **Should-have に落とす**かは Phase 1 終了時に決定。

---

## 関連ドキュメント

- [spec/spec.md](../spec.md)
- [20260308-flight-plan/spec.md](../20260308-flight-plan/spec.md)
- [20260315-scenario-load-api](../20260315-scenario-load-api/spec.md)
- [Backend/docs/test-data/README.md](../../Backend/docs/test-data/README.md)

---

## 変更履歴

| 日付 | 内容 |
|------|------|
| 2026-05-09 | 初版（T-3 / Phase 6 着手用） |
