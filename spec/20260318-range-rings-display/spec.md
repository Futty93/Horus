# レンジリング（距離環）表示（2-2）

## メタデータ

- **Status**: Draft
- **Date**: 2026-03-17
- **関連 Issue**: [#50](https://github.com/Futty93/Horus/issues/50)
- **親 spec**: [spec/spec.md Phase 2-2](../../spec/spec.md)

## 概要

レーダー画面の中心（`centerCoordinate`）から同心円状の**レンジリング（距離環）**を描画し、距離を視覚的に把握しやすくする。管制業務では 5NM/10NM などの基準間隔が重要であり、表示 NM 数は設定可能とする。

---

## 目的（Purpose）

### 背景・課題

| 項目 | 現状 | 課題 |
|------|------|------|
| **距離の視覚的把握** | 航空機・Fix のみ表示 | 中心からの距離感覚を掴みにくい。管制間隔（5NM/1000ft）の判断が直感的でない |
| **ATC 標準との整合** | 表示範囲は km 単位（10〜4000km） | 管制では海里（NM）が standard。レンジリングの間隔も NM であることが望ましい |
| **既存 UI** | DisplayRangeSetting で km を設定 | レンジリングの有無・間隔の設定 UI が存在しない |

### 期待される価値

- **訓練品質の向上**: 管制官役（Controller）・パイロット役（Operator）が距離を直感的に把握できる
- **管制間隔の可視化**: 5NM などの基準間隔がリング上に明示され、衝突リスク判断の補助になる
- **フェーズ 2 の基盤**: 履歴ドット（2-1）、データブロック拡張（2-3）と並行してレーダー表示を強化

### なぜ今か（Motivation）

- spec Phase 2 の優先度高タスクの一つ。実装コストが低い（★☆☆）わりに訓練効果への貢献が大きい
- 既存の `renderMap` / `RadarCanvas` / `displayRange` / `centerCoordinate` の流れに乗りやすい

---

## 詳細調査結果

### 現状の構成

| コンポーネント | 役割 | 備考 |
|----------------|------|------|
| **RadarCanvas** | Canvas の描画ループ、`renderMapOnCanvas` → `renderAircraftsOnCanvas` | 2層 Canvas（ダブルバッファ） |
| **routeRenderer.renderMap** | Waypoint、RadioNavAid、ATS/RNAV ルートを描画 | `centerCoordinate`, `displayRange` を渡す |
| **displayRangeContext** | `range`（km）を保持。10〜4000 | 1辺の表示幅 |
| **centerCoordinateContext** | レーダー中心の緯度・経度 | ドラッグで変更可能 |
| **CoordinateManager** | 緯度・経度 ↔ Canvas 座標変換 | `displayRange.range`（km）、`GLOBAL_SETTINGS.canvasWidth` を利用 |
| **DisplayRangeSetting** | スライダーで range（km）変更 | Operator / Controller の右パネル |

### 座標系・単位

- **Canvas 中心**: `canvasWidth/2`, `canvasHeight/2` が `centerCoordinate` に対応
- **1ピクセルあたりの距離**: `displayRange.range / canvasWidth` (km/px)
- **海里変換**: 1 NM = 1.852 km。`range` が 400km の場合、中心から約 108 NM まで表示可能

### 既存 spec との整合

| ドキュメント | 関連記載 |
|-------------|----------|
| spec/spec.md 2-2 | 中心から同心円。表示 NM 数は設定可能に |
| spec/spec.md 2-1 | 履歴ドット — 同じく RadarCanvas / routeRenderer 周辺の描画拡張 |
| 20260308-ui-ux-improvement | ATC テーマ（atc-*）を使用。レンジリングも控えめな色で統一 |

---

## 方針（Approach）

### 決定方針（Decision）

**フロントエンドのみで完結する。**バックエンド API の追加は不要。

1. **描画ロジック**: `routeRenderer` に `drawRangeRings` を追加し、`renderMap` 内で呼び出す（または別関数として RadarCanvas から呼び出す）
2. **レンジリングの中心**: `centerCoordinate` = Canvas 中心
3. **単位**: 海里（NM）。内部で km に変換して Canvas 座標を算出
4. **間隔の設定**: 新規 Context または既存 DisplayRangeSetting に「レンジリング間隔」を追加。デフォルト 10 NM（5, 10, 20, 50 など選択式を検討）
5. **ON/OFF**: レンジリングの表示可否も設定可能にする

### 実装の流れ

```
centerCoordinate (lat, lon)
    ↓
Canvas 中心 = (canvasWidth/2, canvasHeight/2)
    ↓
半径 r NM の円 → 中心からの距離 r_km = r * 1.852
    ↓
Canvas 上での半径(px) = (r_km / displayRange.range) * (canvasWidth/2)
    ↓
ctx.arc(centerX, centerY, radiusPx, 0, 2*Math.PI)
```

### 検討した他案（Alternatives Considered）

- **案 A**: 表示範囲（km）の 1/4, 1/2, 3/4 に自動でリングを引く。採用しなかった理由: NM 単位の方が ATC 標準と合致し、訓練価値が高い
- **案 B**: レンジリングを RadarCanvas 内に直接実装する。採用しなかった理由: routeRenderer は地図要素の描画責務を持つため、レンジリングも地図レイヤーの一部として routeRenderer または専用モジュールに集約する方が一貫する

### トレードオフ（Trade-offs）

- **メリット**: 実装が軽量。既存の座標系・Context をそのまま利用できる
- **デメリット / 受容する制約**: 緯度によって円の見た目が歪む（北極に近いほど楕円に見える）が、日本の緯度帯では実用上問題ない。高緯度・全球表示は本 spec のスコープ外

---

## 完了条件（Success Criteria）

- [ ] レーダー画面中心から同心円状のレンジリングが描画される
- [ ] 表示間隔（NM）を設定可能（例: 5, 10, 20, 50 NM から選択）
- [ ] レンジリングの ON/OFF が設定可能
- [ ] 各リングに距離ラベル（例: "10 NM"）を表示（任意で Phase 2 に含める）
- [ ] ATC テーマ（atc-text-muted, atc-border 等）に合わせた控えめな色・線種
- [ ] DisplayRange（表示範囲）変更時もレンジリングの物理的距離（NM）が正しく保たれる

---

## 影響範囲

- **Frontend/utility/AtsRouteManager/routeRenderer.ts**: `drawRangeRings` 追加、または新規 `rangeRingsRenderer.ts`
- **Frontend/components/radarCanvas.tsx**: レンジリング描画の呼び出し、必要に応じて設定値の受け渡し
- **Frontend/context/**: レンジリング設定用の Context 追加（`RangeRingsSettingContext` 等）、または `displayRangeContext` 拡張
- **Frontend/components/displayRangeSetting.tsx** または新規 **RangeRingsSetting.tsx**: 間隔・ON/OFF の UI
- **Frontend/app/operator/page.tsx**, **controller/page.tsx**: RangeRingsSetting の配置

---

## 実装計画

### Phase 1: 最小実装（Must-have）

1. **描画関数の追加**
   - `routeRenderer` に `drawRangeRings(ctx, centerCoordinate, displayRange, intervalNm, enabled)` を追加
   - 中心座標は Canvas 中心。`intervalNm` ごとに円を描画（例: 10, 20, 30, ... NM）
   - 線色: `atc-border` 相当（#30363d）、`ctx.globalAlpha = 0.6` 程度で控えめに

2. **固定値で動作確認**
   - `intervalNm = 10`、`enabled = true` でハードコードし、レンダリングループに組み込み
   - DisplayRange 変更時にリングの見た目が正しくスケールすることを確認

3. **Context と設定 UI**
   - `RangeRingsSettingContext`: `{ enabled: boolean, intervalNm: number }`
   - デフォルト: `enabled: true`, `intervalNm: 10`
   - 設定パネル: チェックボックス（ON/OFF）、セレクトまたは数値入力（5, 10, 20, 50 NM）

### Phase 2: ラベル表示（Should-have）

- 各リングに距離ラベル（"5 NM", "10 NM" 等）を描画
- ラベル位置: 円の右端（0°方位）または右上 45° 付近。他の要素と重ならないよう調整
- フォント: `GLOBAL_CONSTANTS.FONT_STYLE_IN_CANVAS` を流用

### Phase 3: 拡張（Could-have）

- 北線（0°/360° の放射状ライン）との併用オプション（別 spec で検討）
- リング間隔のカスタム値入力（5〜100 NM の範囲）

---

## 検証

- [ ] `npm run build` が通る
- [ ] Operator / Controller 画面でレンジリングが表示される
- [ ] 表示範囲（DisplayRange）を変更しても、リングの物理的間隔（NM）が維持される
- [ ] ON/OFF 切り替えで表示が消える
- [ ] 間隔変更でリングの本数が変わる

---

## 未解決事項（Unresolved Questions）

- ラベル表示の有無を Phase 1 に含めるか Phase 2 に回すか
- リングの最大本数（表示範囲が 4000km のとき 50NM 間隔だと約 43 本）— 多すぎる場合の間引きや上限検討
- 既存の routeInfoDisplaySetting と統合するか、独立した RangeRingsSetting にするか

---

## 関連ドキュメント

- [spec/spec.md Phase 2](../../spec.md)
- [20260308-ui-ux-improvement — ATC テーマ](../20260308-ui-ux-improvement/spec.md)
- [Frontend/README.md — 表示範囲設定](../../Frontend/README.md)
