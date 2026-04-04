# 速度ベクトル線の長さ（予測時間）調整（2-4）

## メタデータ

- **Status**: Draft
- **Date**: 2026-04-04
- **関連 Issue**: [#52](https://github.com/Futty93/Horus/issues/52)
- **親インデックス**: [spec/spec.md Phase 2-4](../../spec/spec.md)

## 概要

レーダー上の各機の**速度ベクトル線**（針路方向の先端までの白線）は、現状 **固定で「現在から 1 分後の位置」**に相当する長さで描画されている。本 spec は、利用者が **予測時間（分など）を設定パネルから変更**できるようにし、訓練・デモの見え方を調整できるようにする。バックエンドのシミュレーション計算は変更しない（**表示のみ**）。

## 背景・課題

### 現状

- `CoordinateManager.calculateFuturePositionOnCanvas` が地速（kt）から **1 分間の移動**を仮定し、キャンバス上の終点を求めている（JSDoc も「1 minute」）。
- `DrawAircraft.drawHeadingLine` が上記を用いてベクトル線を描画。
- 利用者が長さを変えられないため、低速機では線が短く、高速機では長く見えるが **時間軸は常に 1 分**。

### 課題（Problem Statement）

- 教材やシナリオによっては **30 秒分・2 分分**など、見せたい「先読み時間」を変えたい。
- 実運用レーダーでもベクトル長と時間の対応はシステム設定であり、**研究用シミュレータでも調整可能であるべき**。

### なぜ今か（Motivation）

- フェーズ 2 の「レーダー表示の強化」の一つで、**難易度低・UI のみ中心**（`spec/spec.md` ★☆☆）。既存のレンジリング等と同様、設定パネルへの追加で完結しやすい。

---

## 方針

### 決定方針（Decision）

1. **入力 UI はスライダー**とする。予測時間（分）の取りうる値は **0.5～10**、**ステップ 0.5 分**（0.5, 1, 1.5, …, 10）。既定の初期値は **1 分**（現状の 1 分先ベクトルと一致）。
2. `calculateFuturePositionOnCanvas`（または呼び出し側）に **「分」倍率**を渡し、現状の 1 分相当の位移に **`durationMinutes` を乗算**する形で終点を計算する（数式の意図は「地速由来の 1 分位移 × 分」）。
3. 設定は **フロントのみ**（React Context または既存のレーダー表示設定に準拠）。**永続化**は `localStorage` 等でも可。バックエンド API は不要。
4. **Operator / Controller 両方**のレーダーで同一設定を共有するか、ページ別にするかは実装時に決定（既定は **グローバル Context で両画面共通**が簡潔）。

### 検討した他案（Alternatives Considered）

- **案 A**: ピクセル長を直接指定する。却下: 表示範囲（NM）とズームに依存し、**時間との対応が崩れる**。
- **案 B**: 自由入力の数値フィールドのみ。却下: **スライダー + 0.5 分刻み**の方が誤入力が少なく、レンジが明確。

### トレードオフ（Trade-offs）

- **メリット**: 実装範囲が Canvas／座標系に閉じ、テストしやすい。仕様の意図が「時間」で説明しやすい。
- **デメリット / 受容する制約**: 極端に長い時間を選ぶと終点が画面外にクリップされ、線が短く見える（現状どおり `Math.min/Math.max` でクリップ）。仕様上は許容。

---

## 完了条件（Success Criteria）

- [ ] 設定パネルに **スライダー**があり、**0.5～10 分・0.5 分刻み**で速度ベクトル用の予測時間を変更できる。
- [ ] 変更後、レーダー上のベクトル線の長さが **地速と設定時間に整合**して変化する。
- [ ] 初期値は **1 分**（現状の 1 分先ベクトルと一致）。
- [ ] `Frontend` の `npm run lint` / `npm test`（該当する場合）が通る。

---

## 影響範囲

- **Frontend**: `CoordinateManager.calculateFuturePositionOnCanvas` の引数または内部計算、`DrawAircraft.drawHeadingLine`、新規または既存の Context（例: 表示設定）、設定 UI コンポーネント（レーダー周りのパネル）。
- **Backend**: なし。
- **ドキュメント**: `Frontend/README.md` のレーダー／設定の説明を更新。

---

## 実装計画

### Phase 1 — 最小実装

1. 定数: `MIN = 0.5`, `MAX = 10`, `STEP = 0.5`, 既定 `DEFAULT = 1`（分）。
2. `calculateFuturePositionOnCanvas` に **分**パラメータを追加し、位移に反映。
3. Context（または既存設定）に値を保持し、レーダー Canvas が参照。
4. 設定パネルに **range スライダー**（`min` / `max` / `step` を上記に合わせる。ラベル例: Vector lookahead (min)／日本語併記は README）。
5. 単体テスト: `CoordinateManager` の位移が **時間倍率に比例**すること（既存テストがあれば拡張）。

### Phase 2 — 任意

- `localStorage` への永続化。

---

## 検証

- [ ] テストが通る
- [ ] ビルドが通る
- [ ] 手動で Operator / Controller の両方でベクトル長が変わることを確認

---

## 未解決事項（Unresolved Questions）

- 設定を **Operator と Controller で共有**するか独立か（既定は共有）。

---

## 関連ドキュメント

- [spec/spec.md Phase 2-4](../../spec/spec.md)
- [Issue #52](https://github.com/Futty93/Horus/issues/52)
- `Frontend/utility/coordinateManager/CoordinateManager.ts`（現行の 1 分先計算）
- `Frontend/utility/aircraft/drawAircraft.ts`（ベクトル線描画）
