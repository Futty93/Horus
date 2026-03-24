# データブロック（ラベル）表示項目追加（2-3）

## メタデータ

- **Status**: Draft
- **Date**: 2026-03-18
- **関連 Issue**: [#51](https://github.com/Futty93/Horus/issues/51)
- **親 spec**: [spec/spec.md Phase 2-3](../../spec/spec.md)

## 概要

レーダー上のデータブロック（航空機ラベル）に表示する項目を選択式で拡張する。スクオーク・機種・ETA などをユーザーが設定 UI で選択し、有効な項目のみをラベルに表示する。管制業務で必要な情報を必要に応じて表示・非表示できるようにする。

---

## 背景・課題

### 現状

| 項目 | 状態 |
|------|------|
| **描画場所** | `Frontend/utility/aircraft/drawAircraft.ts` の `drawAircraftLabel` |
| **固定表示項目** | コールサイン、高度（指示↑/↓現在）、対地速度（G）、目的地（ICAO 下3桁）、危険度（R） |
| **データソース** | `AircraftLocationDto` → `location.ts` の `mapDtoToAircraft` → `Aircraft` クラス |
| **利用可能データ** | `model`（機種）, `eta`（ISO 8601）, `destinationIcao` は既に取得済み |
| **スクオーク** | 未実装。spec 3-1（Issue #55）でバックエンドに追加予定 |

現在は表示項目が固定で、機種・ETA などは API から取得しているがラベルには出していない。表示項目の ON/OFF や追加の設定 UI も存在しない。

### 課題（Problem Statement）

- 機種・ETA といった管制上有用な情報がラベルに表示されない
- ユーザーが表示項目を選べない（例: 機種が不要な場合は非表示にしたい）
- 将来的なスクオーク表示（3-1）を見据えた拡張性がない

### なぜ今か（Motivation）

- Phase 2「レーダー表示の強化」の一環。難易度 ★☆☆ で実装コストが低い
- レンジリング（2-2）と同様、Context + 設定 UI のパターンで一貫した拡張が可能
- 3-1（スクオーク）完了前でも、表示スロットと設定 UI を先に入れておけば後から値だけ接続できる

---

## 詳細調査結果

### ラベル描画の現状レイアウト

- 各項目は縦並び。**グランドスピード（G）と目的地は同一行で横並び**（labelX と labelX+40）。

```
labelX, labelY+0    : callsign
labelX, labelY+15   : altitudeLabel（指示高度 ↑/↓ 現在高度）
labelX, labelY+30   : "G" + groundspeed/10  |  labelX+40, labelY+30: destinationIcao 下3桁
labelX, labelY+45   : "R" + riskLevel（色分け: 白/黄/赤）
```

### API / Aircraft で利用可能な項目

| 表示候補 | Aircraft プロパティ | DTO フィールド | 備考 |
|----------|---------------------|-----------------|------|
| 機種 | `model` | `model` | B738, B777 等。Commercial 以外は type 由来 |
| ETA | `eta` | `eta` | ISO 8601。Commercial のみ。空文字の場合あり |
| スクオーク | 未存在 | 未存在 | 3-1 で追加予定。当面は "---" または非表示 |
| コールサイン | `callsign` | `callsign` | 既存・常時表示を維持 |
| 高度・速度・目的地・危険度 | 既存 | 既存 | 既存のまま。表示 ON/OFF を追加するかは別途検討 |

### 参照実装

- **rangeRingsSettingContext**: `enabled`, `intervalNm` を持ち、設定パネルで変更。`routeRenderer.drawRangeRings` に渡して描画を制御
- 同様に `DataBlockDisplaySetting` を Context 化し、`drawAircraftLabel` に渡す構成とする

---

## 方針

### 決定方針（Decision）

**フロントエンド中心で完結。** バックエンドの変更は 3-1（スクオーク）で別途実施。

1. **表示項目の設定**
   - `DataBlockDisplaySetting` 型: `squawk`, `aircraftType`, `eta` を boolean で ON/OFF
   - デフォルト: 機種・ETA を ON、スクオークは OFF（値が無いため）
   - 3-1 完了後、スクオーク ON で 4 桁表示。OFF または値無し時は表示しない

2. **描画ロジック**
   - `drawAircraftLabel` に `DataBlockDisplaySetting` を渡す
   - 各項目を設定に応じて描画。行の順序・レイアウトは既存に合わせて追加
   - ETA: ISO 8601 を `HH:mm` 等にフォーマット。空の場合は表示しない
   - 機種: `model` をそのまま表示（長い場合は省略検討）
   - スクオーク: 値があれば 4 桁、なければ "---" または表示行自体をスキップ（設定による）

3. **設定 UI**
   - チェックボックスで「スクオーク」「機種」「ETA」の ON/OFF
   - **コラプシブル**: 滅多に変更しないため、デフォルトは折りたたみでコンパクト表示（「データブロック」と現在有効な項目のサマリを表示）。クリックで展開し、チェックボックスで設定変更可能
   - Operator / Controller の右パネルに配置（RangeRingsSetting 等と同列）
   - スクオークは「3-1 完了後に有効化」と明記するか、最初はグレーアウトでも可

### 検討した他案（Alternatives Considered）

- **案 A**: 既存項目（高度・速度・目的地・危険度）も ON/OFF 対象にする。採用しなかった理由: 必須情報が消えると混乱する。まずは追加項目のみ選択式にして様子見
- **案 B**: スクオークを 2-3 スコープ外にする。採用しなかった理由: 表示スロットと設定 UI を先に入れておけば 3-1 との統合が容易。プレースホルダ表示で十分

### トレードオフ（Trade-offs）

- **メリット**: 既存の Context/設定パネル構成を流用でき、変更が局所的
- **デメリット / 受容する制約**: スクオークは 3-1 完了まで実質未使用。ラベルが長くなりレーダー上で重なる可能性は、フォントサイズやレイアウト調整で対応

---

## 完了条件（Success Criteria）

- [ ] 表示項目の設定 UI が Operator / Controller に存在する（スクオーク・機種・ETA の ON/OFF）
- [ ] 機種（model）が ON のときラベルに表示される
- [ ] ETA が ON かつ値があるとき、フォーマット済み時刻がラベルに表示される
- [ ] スクオークが ON のとき、値があれば 4 桁表示、なければ "---" または非表示（3-1 前は常に無し想定）
- [ ] 設定の変更がレンダリングに即時反映される
- [ ] 既存のコールサイン・高度・速度・目的地・危険度の表示に影響がない

---

## 影響範囲

- **Frontend/utility/aircraft/drawAircraft.ts**: `drawAircraftLabel` に `DataBlockDisplaySetting` を渡し、条件付き描画を追加
- **Frontend/context/**: `DataBlockDisplaySettingContext` 新規作成（または既存設定 Context に統合）
- **Frontend/components/**: データブロック表示設定用 UI（チェックボックス群）を新規または既存パネルに追加
- **Frontend/components/radarCanvas.tsx**: 設定値を `DrawAircraft.drawAircraft` に渡す
- **Frontend/utility/aircraft/aircraftClass.ts**: スクオーク用プロパティは 3-1 で追加。本 spec では不要
- **Backend**: 変更なし（3-1 で AircraftLocationDto に squawk 追加予定）

---

## 実装計画

### Phase 1: 設定基盤と機種・ETA（Must-have）

1. **DataBlockDisplaySetting 型と Context**
   - `{ squawk: boolean; aircraftType: boolean; eta: boolean }`
   - デフォルト: `aircraftType: true`, `eta: true`, `squawk: false`
   - `DataBlockDisplaySettingContext` で Provider 提供

2. **drawAircraftLabel の拡張**
   - 引数に `DataBlockDisplaySetting` を追加
   - `aircraftType` ON 時: `model` を適切な行に描画
   - `eta` ON 時: `eta` を `HH:mm` 等にフォーマットして描画（空はスキップ）
   - レイアウトは既存の下に追記、または行間を調整

3. **設定 UI**
   - チェックボックス 3 つ（スクオーク・機種・ETA）
   - コラプシブル表示（折りたたみ時にサマリ表示）
   - Operator / Controller の右パネルに配置（RangeRingsSetting 付近）

### Phase 2: スクオーク表示対応（3-1 連携）

- 3-1 で `AircraftLocationDto` に `squawk` 追加後
- `Aircraft` に squawk プロパティ追加、`mapDtoToAircraft` でマッピング
- `drawAircraftLabel` で squawk ON かつ値ありなら 4 桁表示

---

## 検証

- [ ] `npm run build` が通る
- [ ] Operator / Controller 画面で設定 UI が表示され、チェックで表示 ON/OFF が切り替わる
- [ ] 機種・ETA がラベルに表示される（データがある場合）
- [ ] 既存の表示（コールサイン・高度・速度・目的地・危険度）が従来どおり動作する

---

## 未解決事項（Unresolved Questions）

- ラベル行の最大数・レイアウト（機種名が長い場合の省略ルール）
- ETA のフォーマット（`HH:mm` vs `HH:mm:ss` vs 日付を含めるか）

---

## 関連ドキュメント

- [spec/spec.md Phase 2](../../spec.md)
- [Issue #51](https://github.com/Futty93/Horus/issues/51)
- [Issue #55（スクオーク 3-1）](https://github.com/Futty93/Horus/issues/55)
- [spec/20260318-range-rings-display — 参照実装](../20260318-range-rings-display/spec.md)
- [Frontend/utility/aircraft/drawAircraft.ts](../../Frontend/utility/aircraft/drawAircraft.ts)
