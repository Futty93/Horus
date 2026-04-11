# フライトプラン設定ページ — JSON エクスポート/インポート（1-2）

## メタデータ

- **Status**: In Progress
- **Date**: 2026-03-08
- **関連 Issue**: [#45](https://github.com/Futty93/Horus/issues/45)
- **親 spec**: [spec/spec.md Phase 1-2](../../spec/spec.md)、[20260308-flight-plan-setup-page Phase 2](../20260308-flight-plan-setup-page/spec.md)

## 概要

フライトプラン設定ページ（`/flight-plan-setup`）におけるシナリオの JSON エクスポート・インポート機能を対象とする。本タスクは**フロントエンドのみ**で、ブラウザの Download API および FileReader API を用いたオフラインでのシナリオ保存・再利用を実現する。調査の結果、**基本実装は既に存在し動作している**。本 spec では現状の整理、完了条件の充足確認、および必要に応じた堅牢化を定義する。

---

## 詳細調査結果

### 現状の実装

| 項目 | 状態 | 場所 |
|------|------|------|
| **エクスポート** | 実装済み | `utility/api/scenario.ts` の `exportScenario()` |
| **インポート** | 実装済み | `FlightPlanSetupActionBar` → ファイル選択 → `parseScenarioJson()` |
| **UI ボタン** | 実装済み | 「Import JSON」「Export JSON」が ActionBar に配置 |
| **型定義** | 実装済み | `ScenarioJson`, `ScenarioAircraft`, `FlightPlanDto`, `InitialPositionDto` |

### エクスポート処理フロー（現状）

```
1. ユーザーが「Export JSON」をクリック（aircraft が 1 機以上あるときのみ有効）
2. exportScenario(scenario) が呼ばれる
3. JSON.stringify(scenario, null, 2) で整形
4. Blob + URL.createObjectURL + <a download> でダウンロード
5. ファイル名: scenario-{scenarioName|export}-{timestamp}.json
```

### インポート処理フロー（現状）

```
1. ユーザーが「Import JSON」をクリック → 隠し <input type="file" accept=".json,application/json">
2. FileReader.readAsText でファイル内容を取得
3. parseScenarioJson(text) でパース・検証
4. 成功時: setScenario(sc), setStatus(`Imported ${sc.aircraft.length} aircraft`)
5. 失敗時: setStatus(`Error: ${String(e)}`)
```

### parseScenarioJson のバリデーション（現状）

| チェック項目 | 実装 | 備考 |
|-------------|------|------|
| `aircraft` 配列の存在 | ✅ | `isScenarioLike()` で検証 |
| 各要素に `flightPlan` と `initialPosition` | ✅ | 型ガードで検証 |
| `flightPlan.callsign` が string | ✅ | 検証済み |
| `initialPosition` の各フィールド（lat, lon, alt 等） | ❌ | 未検証。空オブジェクト `{}` も通過 |
| `flightPlan` の departureAirport, arrivalAirport 等 | ❌ | 未検証 |
| JSON 構文エラー | ✅ | `JSON.parse` が throw。catch で status 表示 |

**結論**: 最小限の構造チェックはあるが、`initialPosition` や `flightPlan` の詳細は未検証。不正データは「これで始める」送信時にバックエンドで 400 になる。

### 親 spec Phase 2 との対応

| Phase 2 タスク | 20260308-flight-plan-setup-page | 現状 |
|----------------|---------------------------------|------|
| 2.1 エクスポート | Blob + download 属性 | ✅ 実装済み |
| 2.2 インポート | ファイル選択 → JSON パース → ローカル状態に反映 | ✅ 実装済み |
| 2.3 バリデーション | インポート時の形式チェック、エラー表示 | ⚠️ 部分実装（基本構造のみ。エラーは status で表示） |

---

## 方針

### 決定方針（Decision）

**既存実装をベースに、完了条件を充足させる**。spec/spec.md 1-2 の範囲は「フロントのみ。ブラウザ download API」であり難易度 ★☆☆ のため、大規模な追加実装は行わない。

1. **完了条件の充足確認**: 20260308-flight-plan-setup-page Phase 2.1, 2.2 は実装済み。本 spec で完了として記録する。
2. **2.3 バリデーション**: 現状の `parseScenarioJson` は最小限の検証で十分。バックエンドの `POST /api/scenario/load` が「これで始める」時に詳細検証を行うため、インポート時点での厳密な検証は Must-have としない。
3. **エラー UX**: 現状 `status` に `Error: ...` を表示。実用上問題なければ現状維持。改善する場合は Should-have として扱う。
4. **テスト**: `exportScenario` と `parseScenarioJson` のユニットテスト追加を Should-have とする（リグレッション防止）。

### 検討した他案（Alternatives Considered）

- **案 A（厳密バリデーション）**: `parseScenarioJson` で initialPosition の全フィールドを検証。採用しなかった理由: バックエンド検証と二重化し、保守コストが増える。不正 JSON の多くは「これで始める」時に検知可能。
- **案 B（ゼロから実装）**: 既存実装を無視して新規実装。採用しなかった理由: 既に動作しており、重複・混乱を招く。

### トレードオフ（Trade-offs）

- **メリット**: 実装工数を抑えつつ、spec と実装の整合を図れる。
- **デメリット / 受容する制約**: インポート時のバリデーションは最小限のまま。不正データは「これで始める」クリック時に初めてエラーとなる。

---

## 完了条件（Success Criteria）

### Must-have

- [x] 編集中シナリオを JSON ファイルとしてダウンロードできる（Blob + download 属性）
- [x] JSON ファイルをアップロードしてシナリオを読み込み・編集できる
- [x] インポート時に基本構造（aircraft 配列、flightPlan、initialPosition）の検証を行う
- [x] インポートエラー時にユーザーにメッセージを表示する

### Should-have

- [ ] `exportScenario` と `parseScenarioJson` のユニットテストを追加する
- [ ] 20260308-flight-plan-setup-page の Phase 2 完了チェックを更新する

### Optional

- [ ] インポート時の詳細バリデーション（initialPosition 数値チェック等）を強化する
- [ ] エラー表示の UX 改善（トースト、モーダル等）

---

## 影響範囲

- **Frontend**
  - `utility/api/scenario.ts`: `exportScenario`, `parseScenarioJson`（既存。変更はテスト追加時のみ）
  - `components/flight-plan-setup/FlightPlanSetupActionBar.tsx`: 既存。変更なし
  - `app/flight-plan-setup/page.tsx`: 既存。変更なし
- **spec**
  - `spec/20260308-flight-plan-setup-page/spec.md`: Phase 2 の完了フラグ更新

---

## 実装計画

### Phase 1: 完了確認・ドキュメント更新（Must-have）

| # | タスク | 備考 |
|---|--------|------|
| 1.1 | 現状のエクスポート・インポート動作を手動で確認 | テンプレート読み込み → 編集 → エクスポート → インポートの往復 |
| 1.2 | 20260308-flight-plan-setup-page の Phase 2 完了チェックを更新 | 2.1, 2.2, 2.3 を完了とする |

### Phase 2: テスト追加（Should-have）

| # | タスク | 備考 |
|---|--------|------|
| 2.1 | `exportScenario` のユニットテスト | 空・1機・複数機のシナリオで Blob 生成と download 属性を検証（可能なら） |
| 2.2 | `parseScenarioJson` のユニットテスト | 正常系、aircraft 欠損、flightPlan/initialPosition 欠損、不正型 |

---

## 検証

- [ ] フロントエンドのビルドが通る（`npm run build`）
- [ ] Lint が通る（`npm run lint`）
- [ ] テンプレート読み込み → 編集 → エクスポート → インポートの往復が正しく動作する
- [ ] 不正 JSON をインポートした際にエラーメッセージが表示される

---

## UI テスト用サンプルシナリオ

`Frontend/public/samples/tokyo-approach-scenario.json` を配置済み。フライトプラン設定ページのインポートで利用可能。

- **内容**: 羽田・成田・関空を結ぶ 3 機のシナリオ
- **FIX**: KOITO, BOKJO, AOIKU, ABENO, ACTOR, CHESS, SAMON（バックエンド waypoints.json に存在）
- **用途**: Import JSON の動作確認、「これで始める」の E2E テスト

---

## 未解決事項（Unresolved Questions）

- なし。基本実装は完了しており、本 spec で方針を定めた。

---

## 関連ドキュメント

- [spec/20260308-flight-plan-setup-page](../20260308-flight-plan-setup-page/spec.md) — 親 spec。Phase 2 が JSON 保存・読み込み
- [Frontend/utility/api/scenario.ts](../../Frontend/utility/api/scenario.ts) — エクスポート・インポートの実装
- [spec/20260315-scenario-load-api](../20260315-scenario-load-api/spec.md) — バックエンド `POST /api/scenario/load`。インポートしたシナリオは「これで始める」でここに送信される
