# フライトプラン設定ページ — 航空機テーブル編集（1-4）

## メタデータ

- **Status**: Done（Must-have / Should-have 実装済み。手動 E2E・Optional のみ）
- **Date**: 2026-03-15
- **整合更新**: 2026-05-09（[20260509-phase1-flight-plan-setup-spec-alignment](../20260509-phase1-flight-plan-setup-spec-alignment/spec.md)）
- **関連 Issue**: [#47](https://github.com/Futty93/Horus/issues/47)
- **親 spec**: [spec/spec.md Phase 1-4](../../spec/spec.md)、[20260308-flight-plan-setup-page Phase 1](../20260308-flight-plan-setup-page/spec.md)

## 概要

フライトプラン設定ページ（`/flight-plan-setup`）において、航空機の**追加**・**削除**・**初期位置の編集**を GUI で行う機能。**2026-05-09 時点で実装済み**（`AddAircraftForm`、`AircraftTable` の削除列、`InitialPositionEditor`、`page.tsx` のハンドラ）。本 spec は当初の設計と完了条件の記録および残タスクの整理用。

---

## 目的（Purpose）

### 背景・課題（spec 初版時）

| 項目 | 当時の課題 | 2026-05-09 の実装 |
|------|------------|-------------------|
| **航空機の追加** | UI がなかった | `AddAircraftForm`、空シナリオ時の「Add aircraft」リンク、既存シナリオでもフォーム表示 |
| **航空機の削除** | 不可 | `AircraftTable` の Delete + `confirm` |
| **初期位置の編集** | 不可 | `InitialPositionEditor` + `handleUpdateAircraft` |

### 期待される価値

- **柔軟なシナリオ編集**: テンプレートをベースに微調整（機の追加・削除・位置ずらし）が GUI で完結する
- **「空から開始」の実現**: 20260308-flight-plan-setup-page Phase 1.4 で未着手の「航空機 0 の状態から 1 機ずつ追加」を実現
- **JSON 編集の代替**: 軽微な変更のため JSON を触る必要がなくなる

---

## 詳細調査結果

### 現状の構成（2026-05-09）

| コンポーネント | 役割 | 編集可否 |
|----------------|------|----------|
| **OdGroupList / OdGroupSection** | O/D グループ単位のルート編集 | ○ |
| **AircraftTable** | 一覧・行選択・**削除** | ○ 削除 / 行選択 |
| **AddAircraftForm** | 新規機追加（コールサイン、O/D、初期位置、空港から座標反映） | ○ |
| **InitialPositionEditor** | 選択機の `initialPosition` 6 項目 | ○ |
| **RoutePreviewMap** | 選択機の経路・初期位置プレビュー | 表示（state 連動） |
| **FlightPlanSetupActionBar** | Template, Import/Export, これで始める | ○ |

### データ構造（参照）

`ScenarioAircraft`（`@/types/scenario`）:

```typescript
interface ScenarioAircraft {
  flightPlan: FlightPlanDto;    // callsign, aircraftType?, departureAirport, arrivalAirport, cruiseAltitude, cruiseSpeed, route[]
  initialPosition: InitialPositionDto;  // latitude, longitude, altitude, heading, groundSpeed, verticalSpeed
  spawnTime?: number;
}

interface InitialPositionDto {
  latitude: number;
  longitude: number;
  altitude: number;
  heading: number;
  groundSpeed: number;
  verticalSpeed: number;
}
```

### 既存 spec との整合

| ドキュメント | 関連記載 |
|-------------|----------|
| 20260308-flight-plan-setup-page Phase 1.4 | 空から開始 — 航空機 0 の状態から 1 機ずつ追加する UI（簡易フォーム） |
| 20260308-flight-plan-setup-page Phase 1 | テーブル編集: Callsign \| Origin \| Dest \| Route \| Initial Position \| 編集 |
| 20260315-scenario-load-api | `POST /api/scenario/load` は `ScenarioJson` を受け付け、`aircraft[]` の全フィールドを利用 |

---

## 方針（Approach）

### 決定方針（Decision）

**フロントエンドのみで完結する編集機能を段階的に実装する**。バックエンド API の追加は不要（`POST /api/scenario/load` が既存の `ScenarioJson` 形式をそのまま受け付ける）。

1. **航空機の削除（Must-have）**
   - AircraftTable の各行に削除ボタン、または選択時のサイドパネルに削除ボタンを配置
   - 削除時は `scenario.aircraft` から該当機を除外し `setScenario` で更新

2. **航空機の追加（Must-have）**
   - **空から開始**: `aircraft.length === 0` のとき「航空機を追加」ボタン + 簡易フォーム（Callsign, Origin, Dest, 初期位置の最小必須項目）を表示
   - **既存シナリオに追加**: AircraftTable 直上または ActionBar に「航空機を追加」ボタン。同様のフォームで新規機を生成し `scenario.aircraft` に push

3. **初期位置の編集（Must-have）**
   - 航空機選択時にサイドパネル（RoutePreviewMap 配下など）で `initialPosition` の各項目を編集可能にする
   - 編集フォーム: latitude, longitude, altitude, heading, groundSpeed, verticalSpeed
   - 変更は即時 `setScenario` に反映し、RoutePreviewMap のプレビューも更新される

### なぜこの方針か

- **一貫性**: 既存の OdGroupSection のルート編集と同様、状態は `scenario` に集約され、Export / これで始めるでそのまま利用できる
- **段階的**: 削除 → 追加 → 初期位置の順で、依存関係が少なく個別に検証しやすい
- **既存 API との整合**: `ScenarioJson` 構造は変更せず、フロントの state 更新のみで済む

### 検討した他案（Alternatives Considered）

| 案 | 内容 | 採用しなかった理由 |
|----|------|--------------------|
| A | AircraftTable をインライン編集可能にする | 初期位置は 6 項目あり表形式では cramped。選択 → サイドパネル編集の方が UX が良い |
| B | フライトプラン（callsign, Origin, Dest 等）も個別編集 | 1-4 のスコープを「追加・削除・初期位置」に限定。フライトプラン編集は OdGroup でカバー済み or 将来拡張 |
| C | 初期位置を地図上ドラッグで変更 | 将来的に有用だが実装コスト大。まずは数値入力で着手 |

### トレードオフ（Trade-offs）

- **メリット**: テンプレート／JSON ベースのワークフローを補完し、細かな調整が GUI で可能になる
- **デメリット**: 航空機追加時の初期位置はユーザーが手入力する必要がある（空港座標からの自動算出は将来拡張）

---

## 実装計画

### Phase 1: 航空機の削除

| # | タスク | 備考 |
|---|--------|------|
| 1.1 | AircraftTable に削除ボタン列を追加 | 行ホバー時または常時表示。`onDeleteAircraft(callsign)` コールバック |
| 1.2 | page.tsx で `handleDeleteAircraft` を実装 | `setScenario` で該当機を除外。削除後に `selectedAircraft` をクリア |
| 1.3 | 確認ダイアログ（Optional） | 誤削除防止のため「削除しますか？」を表示 |

### Phase 2: 航空機の追加

| # | タスク | 備考 |
|---|--------|------|
| 2.1 | 「航空機を追加」フォームコンポーネント | `AddAircraftForm`: Callsign, departureAirport, arrivalAirport, initialPosition の最小項目 |
| 2.2 | 空のときの表示変更 | 「Load Haneda Template or Import JSON」に加え「または航空機を追加」ボタン。クリックでフォーム表示 |
| 2.3 | 既存シナリオ時の追加 UI | ActionBar または AircraftTable 上部に「航空機を追加」ボタン |
| 2.4 | デフォルト初期位置 | 追加時は departureAirport の座標を取得し、その付近に配置。取得できない場合は固定値（例: 34.5, 138.5） |
| 2.5 | バリデーション | Callsign 重複チェック、ICAO 4 文字チェック（軽量） |

### Phase 3: 初期位置の編集

| # | タスク | 備考 |
|---|--------|------|
| 3.1 | AircraftDetailPanel（または既存サイドエリア拡張） | 選択機がいる場合、RoutePreviewMap の下に初期位置編集フォームを表示 |
| 3.2 | 編集項目 | latitude, longitude, altitude, heading, groundSpeed, verticalSpeed の input |
| 3.3 | 更新ハンドラ | `handleUpdateAircraft(callsign, partial)` で `scenario.aircraft` の該当機を更新 |
| 3.4 | RoutePreviewMap との連動 | `selectedAircraft` の変更が即反映されるよう、同一 state を参照 |

---

## 影響範囲（実装済み）

- **Frontend**
  - `app/flight-plan-setup/page.tsx`: `handleDeleteAircraft`, `handleAddAircraft`, `handleUpdateAircraft`
  - `components/flight-plan-setup/AircraftTable.tsx`: `onDeleteAircraft`
  - `components/flight-plan-setup/AddAircraftForm.tsx`, `InitialPositionEditor.tsx`
  - 空港座標: `GET /api/ats/airports` + `AddAircraftForm` の departure 反映
- **Backend**: 変更なし

---

## 完了条件（Success Criteria）

### Must-have

- [x] 航空機を削除できる。削除後はテーブルと OdGroupList から該当機が消え、Export にも含まれない
- [x] 空のシナリオから「航空機を追加」で 1 機以上追加し、シナリオを構築できる
- [x] 既存シナリオに「航空機を追加」で機を追加できる
- [x] 航空機選択時に初期位置（lat, lon, altitude, heading, groundSpeed, verticalSpeed）を編集でき、RoutePreviewMap に即時反映される
- [ ] 追加・編集・削除後のシナリオで「これで始める」が正常に動作する（**手動 E2E 推奨**。自動テストは別途）

### Should-have

- [x] 追加時に Callsign 重複をチェックし、重複時はエラー表示する
- [x] 削除時に確認ダイアログを表示する（誤操作防止）

### Optional

- [x] 追加時に出発空港 ICAO から座標を反映（`AddAircraftForm` の「空港から反映」相当）
- [ ] テーブル行内での callsign / O/D / route の直接編集（現状は OdGroup 編集・フォーム追加で代替）
- [ ] 地図ドラッグによる初期位置変更

---

## 検証

- [x] フロントエンドのビルドが通る（`npm run build`）
- [x] Lint が通る（`npm run lint`）
- [x] 単体テストが通る（`npm test`）
- [ ] 手動: テンプレート読み込み → 1 機削除 → Export → 機数が減っている
- [ ] 手動: 空の状態 → 航空機追加（2 機）→ これで始める → Operator で 2 機表示
- [ ] 手動: 機選択 → 初期位置変更 → Apply → RoutePreviewMap の「Now」マーカーが移動する

---

## 残タスク（2026-05-09 時点）

| 分類 | 内容 |
|------|------|
| **運用** | 検証節の手動チェック（Export 往復、空から追加→load、初期位置とプレビュー連動） |
| **Optional** | 行内 FP 編集、地図ドラッグ |
| **別 Issue** | 仕上げを #47 コメントで本 spec 参照に更新 |

---

## 未解決事項（Unresolved Questions）

- フライトプランの行内編集はスコープ外。OdGroup / 将来タスク。

---

## 変更履歴

| 日付 | 内容 |
|------|------|
| 2026-05-09 | Status Done。実装済みコンポーネントを反映。Optional の空港座標を実装済に更新。 |

---

## 関連ドキュメント

- [spec/spec.md Phase 1](../../spec/spec.md)
- [spec/20260308-flight-plan-setup-page](../20260308-flight-plan-setup-page/spec.md)
- [spec/20260315-scenario-load-api](../20260315-scenario-load-api/spec.md)
- [Frontend types/scenario.ts](../../Frontend/types/scenario.ts)
