# Horus 実装計画

- **Date**: 2026-05-09
- **Status**: Planning（ロードマップ継続更新）

**関連**: バックエンド再設計の詳細は [spec/20260308-backend-redesign/spec.md](20260308-backend-redesign/spec.md)。フライトプラン機能の分解は [spec/20260308-flight-plan/spec.md](20260308-flight-plan/spec.md)。

---

## 凡例

- **優先度**: 🔴 高 / 🟡 中 / 🟢 低
- **難易度**: ★☆☆ 低 / ★★☆ 中 / ★★★ 高
- **実装状況（短縮）**: ✅ おおむね満たす / 🔄 一部・仕上げあり / ⬜ 未着手

---

## コードベース・マップ（2026-05-09 時点）

計画をファイル名に落とすための索引。リネームされた場合は本節を更新する。

### Backend（Spring Boot）

| 層 | 代表パス | 内容 |
|----|-----------|------|
| API（`*Controller`） | `.../interfaces/api/ScenarioController.java` | `POST /api/scenario/load` |
| | `.../interfaces/api/FlightPlanController.java` | spawn-with-flightplan, direct-to, resume-navigation, flightplan CRUD |
| | `.../interfaces/api/ConflictAlertController.java` | コンフリクト・統計 JSON API |
| | `.../interfaces/api/AtcClearanceController.java` | 管制メモ（クリアランス） |
| API（`*Service` 名のまま） | `.../interfaces/api/LocationService.java` | 位置一覧（Use Case 経由） |
| | `.../interfaces/api/ControlAircraftService.java` | 管制指示 POST |
| | `.../interfaces/api/CreateAircraftService.java` | 航空機生成 |
| | `.../interfaces/api/SimulationService.java` | シミュレーション開始/停止等 |
| | `.../interfaces/api/AtsRouteService.java` | ATS ルート参照 |
| Application | `.../application/ScenarioServiceImpl.java` | スポーン・Direct To・フライトプラン操作の中核 |
| | `.../application/ConflictAlertService.java` | 検出結果 → DTO |
| 定数 | `.../shared/constants/AtcSimulatorConstants.java` | `REFRESH_RATE`（現状 1Hz 相当）、`TICK_INTERVAL_MS` |
| OpenAPI | `Backend/UranosAPI.yml` | 公開 API スキーマ |

フロントの BFF は `Frontend/app/api/**/route.ts`（例: `app/api/scenario/load/route.ts`）がバックエンドへのプロキシになる。

### Frontend（Next.js）

| 領域 | 代表パス | 内容 |
|------|-----------|------|
| レーダー | `Frontend/components/radarCanvas.tsx` | ポーリング、`drawRangeRings`、速度ベクトル、`DrawAircraft` |
| 軌跡ドット | `aircraftClass.ts`（`positionHistory`）、`drawAircraft.ts`、`trackHistoryDisplaySamples.ts` | クライアント記録・間引き表示（既定 3 点） |
| レンジリング | `rangeRingsSettingContext.tsx`、`rangeRingsSetting.tsx`、`routeRenderer.ts`（`drawRangeRings`） | 有効化・間隔 NM、`radarCanvas` から描画 |
| 速度ベクトル時間 | `velocityVectorLookaheadContext.tsx`、`radarCanvas.tsx` | 先読み時間（分）をベクトル長に反映 |
| データブロック項目 | `dataBlockDisplaySettingContext.tsx`、`drawAircraft.ts` | squawk / type / ETA / メモ行の表示トグル（squawk は未連携時 `---`） |
| 位置・リスク | `Frontend/utility/api/location.ts` | `AircraftLocationDto` → `Aircraft`、`riskLevel` をラベルに反映 |
| フライトプラン設定 | `app/flight-plan-setup/page.tsx` ほか | `page.tsx`（state・Suggest・Import/Export・開始）、`AircraftTable`（選択・削除）、`AddAircraftForm`、`InitialPositionEditor`、`utility/api/scenario.ts`（`loadScenarioAndStart` と `parseJsonMessage`） |
| オペレーター操作 | `Frontend/components/flightPlanControl.tsx` | Direct To / Resume 等 |
| トップ | `Frontend/app/page.tsx` | Controller / Operator / Flight Plan Setup へのリンクのみ（JSON アップロード起動は無し） |

---

## 現状認識とギャップ（コードベース照合）

| 観点 | コード上の事実 | 計画への反映 |
|------|----------------|-------------|
| **Phase 1（入口）** | 上記フローは実装済み。 | 1-1〜1-4 は**コア Done**（子 spec 2026-05-09 更新済）。**残り**: Optional・手動 E2E・Issue #45–#47 の Close 運用。1-5 は未着手。 |
| **Phase 2（レーダー）** | 履歴ドット・レンジリング・速度ベクトル時間は **Canvas ＋ Context で動作**。データブロックは **表示トグルと描画**あり。スクオーク行は **プレースホルダ**（`drawAircraft.ts`、Backend 未連携）。 | 2-1・2-2・2-4 はロードマップ上「未着手」扱いが不整合。**残りは拡張（点数・間隔の設定、子 spec の DoD との一致確認）** と 2-3 の **実データ連携（3-1 と接続）**、2-5・2-6。 |
| **Phase 4（安全 UI）** | `riskLevel` は API → `Aircraft` → ラベル色（赤/黄）と `R` 行表示。**BFF・ストリップ・シンボル**は [20260509-phase4-conflict-bff-ui](20260509-phase4-conflict-bff-ui/spec.md)。**選択機ペア数値・違反バナー**は [20260509-phase4-conflict-pair-ui-alerts](20260509-phase4-conflict-pair-ui-alerts/spec.md)。加えて 2026-05 に **片側不足のみで赤張り付きしないよう H/V 同時性を反映したリスク合成へ調整**。 | **残り**: シンボル/閾値 UI の仕上げ（4-1）など。 |
| **シミュレーション速度（5-1）** | `AtcSimulatorConstants.REFRESH_RATE` が静的。 | 動的変更 API とスケジューラの見直しが必要。 |
| **命名負債（T-2）** | `interfaces/api` に `*Service` が複数残存。 | `FlightPlanController` / `ScenarioController` と混在。リネームは OpenAPI・BFF・README を一括追従。 |

---

## 実装方針（共通）

1. **子 spec を正**: タスクの完了条件・調査結果は各 `spec/YYYYMMDD-*/spec.md` に書き、本ファイルはロードマップと優先度の索引に留める。コードが先行している項目は **子 spec の Status / 調査節を Done / 実装済みに更新**する。
2. **API 契約を先に**: Backend の DTO / OpenAPI（`UranosAPI.yml`）と Frontend の型・BFF を同じ PR か直近で揃える。
3. **統合テストで入口を守る**: `FlightPlanApiIntegrationTest`（`scenario/load` 等）、`BackendRedesignIntegrationTest` 等で退行を検知する。
4. **レーダー変更は描画と状態を分離**: Canvas パフォーマンス（T-9）は「データ取得・状態」と「描画ループ」を分けてから最適化すると効く。

---

## フェーズ 1 — シナリオ作成・開始フロー（最重要基盤）

| # | タスク | 状態 | 優先度 | 難易度 | Issue | 実装の所在 / 次の具体タスク |
|---|--------|------|--------|--------|-------|---------------------------|
| 1-1 | `POST /api/scenario/load` | ✅ | 🔴 | ★★☆ | [#44](https://github.com/Futty93/Horus/issues/44) | `ScenarioController` + `FlightPlanApiIntegrationTest`。子 spec [20260315-scenario-load-api](20260315-scenario-load-api/spec.md)。 |
| 1-2 | JSON エクスポート/インポート | ✅ | 🔴 | ★☆☆ | [#45](https://github.com/Futty93/Horus/issues/45) | コア Done（[20260308-json-export-import](20260308-json-export-import/spec.md)）。**残り**: Optional（厳密バリデーション・トースト）、`exportScenario` の直接テスト。 |
| 1-3 | 「これで始める」 | ✅ | 🔴 | ★★☆ | [#46](https://github.com/Futty93/Horus/issues/46) | Must/Should 完了（[20260315-start-with-this-button](20260315-start-with-this-button/spec.md)、`scenario.test.ts`）。**残り**: 手動 E2E、ネットワーク文言 Optional。 |
| 1-4 | 航空機テーブル編集 | ✅ | 🔴 | ★★☆ | [#47](https://github.com/Futty93/Horus/issues/47) | コア Done（[20260315-aircraft-table-edit](20260315-aircraft-table-edit/spec.md)）。**残り**: 手動 E2E、行内 FP 編集は Optional。 |
| 1-5 | トップ等から JSON アップロード起動 | ⬜ | 🟡 | ★☆☆ | [#48](https://github.com/Futty93/Horus/issues/48) | `app/page.tsx` はリンクのみ。**新規: ファイル選択 → `parseScenarioJson` → `loadScenarioAndStart` または setup へ引き渡し**の要否を決める。 |

---

## フェーズ 2 — レーダー表示の強化

| # | タスク | 状況 | 優先度 | 難易度 | Issue | 実装の所在 / 次の具体タスク |
|---|--------|------|--------|--------|-------|---------------------------|
| 2-1 | 履歴ドット（軌跡） | ✅ | 🔴 | ★☆☆ | [#49](https://github.com/Futty93/Horus/issues/49) | `aircraftClass.ts`（最大 120 点）、`trackHistoryDisplaySamples.ts`（表示 3 点・間隔）、`drawAircraft.ts`。**拡張**: 点数/間隔の設定 UI、Controller 側の更新経路確認（`updateAircraftLocationInfo`）。 |
| 2-2 | レンジリング | ✅ | 🔴 | ★☆☆ | [#50](https://github.com/Futty93/Horus/issues/50) | `rangeRingsSettingContext` + `rangeRingsSetting.tsx` + `radarCanvas.tsx` の `drawRangeRings`。子 spec [20260318-range-rings-display](20260318-range-rings-display/spec.md) を実装に合わせ更新。 |
| 2-3 | データブロック項目追加 | 🔄 | 🟡 | ★☆☆ | [#51](https://github.com/Futty93/Horus/issues/51) | `dataBlockDisplaySettingContext` + `drawAircraft.ts`。**スクオーク実値は 3-1 とセット**（現状 `---`）。 |
| 2-4 | 速度ベクトル線の時間 | ✅ | 🟡 | ★☆☆ | [#52](https://github.com/Futty93/Horus/issues/52) | `velocityVectorLookaheadContext` + `radarCanvas.tsx`。子 spec [20260404-velocity-vector-line-duration](20260404-velocity-vector-line-duration/spec.md) と突合。 |
| 2-5 | セクター境界線 | ⬜ | 🟡 | ★★☆ | [#53](https://github.com/Futty93/Horus/issues/53) | 着手用子 spec [20260509-phase2-sector-boundary-lines](20260509-phase2-sector-boundary-lines/spec.md)。**現時点は後回し（公式データ整備コストのため Deferred）**。 |
| 2-6 | 指示メモをラベル隣 | 🔄 | 🟡 | ★★☆ | [#54](https://github.com/Futty93/Horus/issues/54) | `drawAircraft.ts` のメモ行、`LocationService` 系 DTO の `atcClearance`。子 spec [20260326-instruction-memo-radar-label](20260326-instruction-memo-radar-label/spec.md)。 |

---

## フェーズ 3 — 管制指示の拡充

| # | タスク | 優先度 | 難易度 | Issue | 実装方針のフック |
|---|--------|--------|--------|-------|------------------|
| 3-1 | スクオーク割当・表示 | 🔴 | ★☆☆ | [#55](https://github.com/Futty93/Horus/issues/55) | Backend: 航空機状態 + API。Frontend: `AircraftLocationDto` / `drawAircraft` の実値化（現プレースホルダ）。 |
| 3-2 | ホールディング（`HOLD`） | 🔴 | ★★★ | [#56](https://github.com/Futty93/Horus/issues/56) | `WaypointAction`・`CommercialAircraft` / `FlightBehavior` の飛行ループ拡張。 |
| 3-3 | 高度制限の指示 | 🟡 | ★★☆ | [#57](https://github.com/Futty93/Horus/issues/57) | `AltitudeConstraint` と `ControlAircraftService` / DTO の拡張。 |
| 3-4 | ハンドオフ（`HANDOFF`） | 🟡 | ★★★ | [#58](https://github.com/Futty93/Horus/issues/58) | 表示から入るなら UI 状態のみでも可。 |
| 3-5 | Mach 数指示 | 🟢 | ★☆☆ | [#59](https://github.com/Futty93/Horus/issues/59) | 指示 DTO と速度変換。 |

---

## フェーズ 4 — コンフリクト検出・安全機能の UI 強化

| # | タスク | 状況 | 優先度 | 難易度 | Issue | 実装の所在 / 次の具体タスク |
|---|--------|------|--------|--------|-------|---------------------------|
| 4-1 | STCA 視覚強調 | 🔄 | 🔴 | ★☆☆ | [#60](https://github.com/Futty93/Horus/issues/60) | `riskLevel` によるラベル色・`R` 表示に加え、**シンボルリング強調・赤レベル時の点滅**（[20260509-phase4-conflict-bff-ui](20260509-phase4-conflict-bff-ui/spec.md)）。**残り**: 閾値の設定 UI、ペア連動強調。 |
| 4-2 | ペア間隔の数値表示 | ⬜ | 🟡 | ★★☆ | [#61](https://github.com/Futty93/Horus/issues/61) | 着手用子 spec [20260509-phase4-conflict-pair-ui-alerts](20260509-phase4-conflict-pair-ui-alerts/spec.md)。BFF 済み。**新規**: ラベル横またはサイドで `fetchAircraftConflicts` 等を表示。 |
| 4-3 | 間隔違反の明示通知 | 🔄 | 🟡 | ★★☆ | [#62](https://github.com/Futty93/Horus/issues/62) | ストリップは [20260509-phase4-conflict-bff-ui](20260509-phase4-conflict-bff-ui/spec.md)。**残り（強通知）**は [20260509-phase4-conflict-pair-ui-alerts](20260509-phase4-conflict-pair-ui-alerts/spec.md)。 |
| 4-4 | MSAW 簡易版 | ⬜ | 🟢 | ★★☆ | [#63](https://github.com/Futty93/Horus/issues/63) | 最低高度しきい値と `AircraftLocationDto` 拡張または別エンドポイント。 |

---

## フェーズ 5 — 訓練・評価機能

| # | タスク | 優先度 | 難易度 | Issue | 実装方針のフック |
|---|--------|--------|--------|-------|------------------|
| 5-1 | シミュレーション速度変更 | 🔴 | ★★☆ | [#64](https://github.com/Futty93/Horus/issues/64) | `REFRESH_RATE` / `TICK_INTERVAL_MS` の動的化、`@Scheduled` 側の取り込み。 |
| 5-2 | セッション記録 | 🟡 | ★★☆ | [#65](https://github.com/Futty93/Horus/issues/65) | Application 層でイベント追記、永続は後から DB に差し替え可能な設計。 |
| 5-3 | 採点・レポート | 🟡 | ★★★ | [#66](https://github.com/Futty93/Horus/issues/66) | 5-2 の集計。 |
| 5-4 | 難易度設定 | 🟢 | ★★☆ | [#67](https://github.com/Futty93/Horus/issues/67) | スポーンスケジューラ。 |
| 5-5 | 緊急事態フラグ | 🟢 | ★★★ | [#68](https://github.com/Futty93/Horus/issues/68) | `Aircraft` 状態 + DTO + ラベル表示。 |

---

## フェーズ 6 — 気象・環境

| # | タスク | 優先度 | 難易度 | Issue | 実装方針のフック |
|---|--------|--------|--------|-------|------------------|
| 6-1 | 風と対地速度 | 🟡 | ★★☆ | [#69](https://github.com/Futty93/Horus/issues/69) | `FixedWingFlightBehavior`（または相当）への風ベクトル。 |
| 6-2 | ATIS 表示 | 🟡 | ★☆☆ | [#71](https://github.com/Futty93/Horus/issues/71) | 静的 JSON + レーダー周辺パネル。 |
| 6-3 | 気象レーダー | 🟢 | ★★★ | [#70](https://github.com/Futty93/Horus/issues/70) | 外部データ・タイル。 |

---

## フェーズ 7 — マルチユーザー・リアルタイム通信

| # | タスク | 優先度 | 難易度 | Issue | 実装方針のフック |
|---|--------|--------|--------|-------|------------------|
| 7-1 | WebSocket 位置配信 | 🟡 | ★★★ | [#73](https://github.com/Futty93/Horus/issues/73) | 現状 `fetch` ポーリング（`location.ts`）の置換。 |
| 7-2 | マルチセクター | 🟢 | ★★★ | [#72](https://github.com/Futty93/Horus/issues/72) | 7-1 後。 |

---

## 技術的負債（並行・スロット化）

| # | タスク | 優先度 | 難易度 | Issue | 状態 | コード上のフック |
|---|--------|--------|--------|-------|------|------------------|
| T-1 | ConflictAlert DTO 化 | 🟡 | ★☆☆ | [#74](https://github.com/Futty93/Horus/issues/74) | ✅ | `ConflictAlertDto` / `ConflictStatisticsDto` / `ConflictAlertController` |
| T-2 | `*Service` → `*Controller` 統一 | 🟢 | ★★☆ | [#75](https://github.com/Futty93/Horus/issues/75) | - | `LocationService`, `CreateAircraftService`, `ControlAircraftService`, `SimulationService`, `AtsRouteService` 等 + BFF パス |
| T-3 | テスト・サンプル・README | 🟡 | ★★☆ | [#76](https://github.com/HorusATC/Horus/issues/76) | ✅ | **Done** [20260509-t3-flight-plan-tests-docs](20260509-t3-flight-plan-tests-docs/spec.md)。マトリクス・`scenario-load-minimal.json`・MANUAL_TEST・Backend/Frontend README |
| T-4 | API 応答時間 | 🟡 | ★★☆ | - | - | 位置一覧のペイロード・シリアライズ・キャッシュ |
| T-5 | テスタビリティ | 🟡 | ★★☆ | - | - | [20260317-backend-testability](20260317-backend-testability/spec.md) |
| T-6 | DDD 徹底 | 🟡 | ★★★ | - | - | 集約境界・ドメインイベント |
| T-7 | 設定外部化 | 🟢 | ★☆☆ | - | - | `application.yml` / env |
| T-8 | シナリオ DSL 等 | 🟢 | ★★☆ | - | - | `scenario/load` とは別 |
| T-9 | RadarCanvas 最適化 | 🟡 | ★★☆ | - | - | `radarCanvas.tsx` の rAF ループ・依存配列 |
| T-10 | FE 状態管理 | 🟡 | ★★☆ | - | - | Context 分割・型 |
| T-11 | Next dev キャッシュ復旧導線 | 🟡 | ★☆☆ | - | ✅ | [20260509-nextjs-dev-cache-recovery](20260509-nextjs-dev-cache-recovery/spec.md) |

---

## 推奨着手順序（コードベース反映版）

1. **Phase 1 の「ドキュメントと DoD の整合」** — ✅ **完了**（[20260509-phase1-flight-plan-setup-spec-alignment](20260509-phase1-flight-plan-setup-spec-alignment/spec.md)）。Issue #45–#47 へコメント・Close は人手。次は **T-3** または **Phase 4** へ。

2. **（並行）T-3** — ✅ **完了**（[20260509-t3-flight-plan-tests-docs](20260509-t3-flight-plan-tests-docs/spec.md)）。次は **Phase 4**（Conflict UI）または Phase 2 残り。

3. **Phase 4 のフロント接続（4-1 強化 → 4-2 / 4-3）**  
   **スライス 1 完了**: BFF・統計ストリップ・シンボル強調（[20260509-phase4-conflict-bff-ui](20260509-phase4-conflict-bff-ui/spec.md)）。**スライス 2（着手用 spec）**: [20260509-phase4-conflict-pair-ui-alerts](20260509-phase4-conflict-pair-ui-alerts/spec.md)（4-2 数値表示、4-3 強通知）。

4. **Phase 2 の残り（2-5、2-6 の仕上げ、2-3 のデータ連携準備）**  
   2-1・2-2・2-4 は実装済みのため、**新規は主に 2-5 とメモ spec の仕上げ**。2-3 のスクオーク実値は **3-1 と同一スプリント**にすると二度手間が減る。

5. **Phase 3**  
   ドメイン変更が大きいため、上記でレーダー・安全表示の土台を固めてから。

6. **Phase 5 → 6 ∥ 7**  
   5-1 は `AtcSimulatorConstants` とスケジューラ全体への影響が大きいので、短いスパイク（動的 tick の安全な切り替え）を先に。

```text
Phase 1 spec/Issue 整合 ──→ T-3（並行）
       │
       ├─→ Phase 4（BFF + Conflict API + 描画強化）
       │
       ├─→ Phase 2 残り（2-5, 2-6）＋ 3-1（スクオーク）で 2-3 完結
       │
       └─→ Phase 3 → Phase 5 → Phase 6 ∥ Phase 7
```

---

## 変更履歴

| 日付 | 内容 |
|------|------|
| 2026-05-09 | 2-5（セクター境界線）は公式データ整備コストを踏まえて Deferred に更新。調査メモを [20260509-phase2-sector-boundary-lines](20260509-phase2-sector-boundary-lines/spec.md) に追記。 |
| 2026-05-09 | Phase 2-5（セクター境界線）の着手用 spec を追加（[20260509-phase2-sector-boundary-lines](20260509-phase2-sector-boundary-lines/spec.md)）。 |
| 2026-05-09 | Next.js 開発時の `.next` chunk 欠損対策 spec を追加（[20260509-nextjs-dev-cache-recovery](20260509-nextjs-dev-cache-recovery/spec.md)）。 |
| 2026-05-09 | Phase 4 スライス 2 着手用 spec: ペア数値・強通知（[20260509-phase4-conflict-pair-ui-alerts](20260509-phase4-conflict-pair-ui-alerts/spec.md)）。フェーズ 4 表・ギャップ・着手順にリンク。 |
| 2026-05-09 | Phase 4 スライス: Conflict BFF・STCA ストリップ・シンボル強調（[20260509-phase4-conflict-bff-ui](20260509-phase4-conflict-bff-ui/spec.md)）。4-1/4-3 を部分完了に更新。 |
| 2026-05-09 | T-3 完了（テスト・サンプル・README・マトリクス）。着手順 2 を完了扱いに。 |
| 2026-05-09 | T-3 着手用 spec 追加: [20260509-t3-flight-plan-tests-docs](20260509-t3-flight-plan-tests-docs/spec.md)。T-3 行・着手順 2 にリンク。 |
| 2026-05-09 | Phase 1 子 spec 整合実施（1-2〜1-4 を Done 扱いに更新）。着手順 1 を完了扱いに。 |
| 2026-05-09 | Phase 1 着手用 spec 追加: [20260509-phase1-flight-plan-setup-spec-alignment](20260509-phase1-flight-plan-setup-spec-alignment/spec.md)。推奨着手順 1 にリンク。 |
| 2026-05-09 | コードベース照合: Backend/Frontend マップ、Phase 1〜4 の実装状況列、着手順の更新（2-1/2-2/2-4 実装済みを反映）。 |
| 2026-05-09 | Phase 1 の進捗・ギャップ・T-3 対応づけ。 |
| 2026-03-11 | 初版ロードマップ |
