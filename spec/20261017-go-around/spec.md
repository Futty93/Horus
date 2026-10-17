# 進入復行（Phase 1 の次段）

## メタデータ

- **Status**: Done
- **Date**: 2026-10-17
- **親**: [terminal-radar-gaps](../20261017-terminal-radar-gaps/spec.md)
- **前段**: [滑走路と最終進入](../20261017-runway-final-approach/spec.md)

## 概要

`JOIN FINAL` のあと、進入をやめて滑走路針路のまま指定高度まで上昇する復行を出せるようにする。進入許可、公示の旋回、グライドパスは含まない。

## 背景・課題

### 現状

- 最終進入は延長線上の会合点へ Direct To し、到達後に進入コース針路の `HEADING` になる。
- 会合待ちも進入コース追従も、別の針路指示・Direct To・Hold・Resume 以外では捨てられない。
- 指示対気速度は `HEADING` のときだけ目標地速へ換算される。フライトプラン追従中は対象外のままである。

### 課題（Problem Statement）

- 到着機がファイナルに入ったあと、進入を中止して上昇へ戻す指示がない。
- 復行中であることがレーダーのラベルから分からない。

### なぜ今か（Motivation）

台帳の最初の4スライスは main に入っている。1-4 は「進入許可と進入復行は別スライス」としており、到着の一連の指示を閉じる次段が復行である。

---

## 方針

### 決定方針（Decision）

1. `POST /api/aircraft/{callsign}/go-around` で復行する。ボディは空港 ICAO、滑走路名、上昇目標高度（フィート）。
2. 針路は `runways.json` の滑走路方位（進入コースの直進）とする。公示の復行旋回は入れない。
3. 上昇目標は現在高度より 500 フィート以上高いこと。未満は 400。滑走路末端から 120NM を超える機体も 400（`JOIN FINAL` と同じ上限）。
4. ナビモードは `HEADING`。最終進入の会合待ち、Direct To、Hold は捨てる。地速目標と、既に入っている指示対気速度は維持する。復行は `HEADING` なので、既存の IAS 換算がそのまま効く。
5. フライトプラン追従中へ IAS を広げる変更は本スライスに含めない。
6. 位置 API に `goAround: true` を出し、レーダーラベルに `GA` を描く。次の針路指示、Direct To、Hold、Resume、`JOIN FINAL`、フライトプラン付与でフラグを消す。

### 検討した他案（Alternatives Considered）

- **案 A: 復行針路をリクエストで自由入力する。** 滑走路と無関係な針路になり、訓練の「滑走路方位で上昇」が崩れるため却下。
- **案 B: 風や IAS の適用範囲拡大と同時に行う。** 復行の成否と速度モデルの変更が混ざるため却下。

### トレードオフ（Trade-offs）

- **メリット**: ファイナルに入れた機体を、同じ滑走路の針路で上昇に戻せる。
- **デメリット / 受容する制約**: 公示の旋回高度・旋回方向はない。進入許可の状態は持たない。

---

## 完了条件（Success Criteria）

- [x] 復行で会合待ちが消え、`HEADING`・滑走路方位・指定上昇高度になる
- [x] 上昇高度が現在高度 + 500 フィート未満、または末端から 120NM 超なら 400
- [x] 既にある指示対気速度は復行後も残る
- [x] 位置 API の `goAround` が真のあいだ、レーダーラベルに `GA` が出る
- [x] 次の針路指示で `goAround` が消える

---

## 影響範囲

- **Backend**: `AircraftBase`、`ScenarioService`、`FlightPlanController`、位置 DTO、`UranosAPI.yml`、`Backend/README.md`
- **Frontend**: Operator の `FlightPlanControl`、BFF、ラベル描画、`Frontend/README.md`
- **台帳**: `spec/20261017-terminal-radar-gaps/spec.md` から本 spec へリンク

---

## 実装計画

### Phase 1

1. ドメインの復行と、位置 DTO の `goAround`。
2. API と Operator の `GO AROUND`（上昇高度の入力、既定 3000 フィート）。
3. ラベルの `GA`。

---

## 検証

- [x] `GoAroundTest` と `ScenarioServiceFlightPlanTest` の復行ケースが通る
- [x] Frontend の単体テストが通る
- [x] Operator で `GO AROUND` のあとラベルに `GA` が出る

---

## 未解決事項（Unresolved Questions）

- 公示復行（旋回開始高度と旋回針路）は滑走路データの次の拡張にする。

---

## 関連ドキュメント

- [spec/20261017-terminal-radar-gaps/spec.md](../20261017-terminal-radar-gaps/spec.md)
- [spec/20261017-runway-final-approach/spec.md](../20261017-runway-final-approach/spec.md)
