# 指示メモをレーダーラベル隣に表示（2-6）

## メタデータ

- **Status**: Draft
- **Date**: 2026-03-26
- **関連 Issue**: [#54](https://github.com/Futty93/Horus/issues/54)
- **親インデックス**: [spec/spec.md Phase 2-6](../../spec/spec.md)

## 概要

**管制官（Controller）が発出したクリアランス**を、`InstructionMemo` で記録している内容と同一の意味で、レーダー上のデータブロック近傍に短いテキストとして表示する。表示は **管制クリアランス（ATC メモ）** と **航空機の実測**（位置・針路・速度など）の関係を示すためのものであり、**パイロット（Operator）がバックエンドへ送った操縦目標**（API 上の `instructedVector`）とは概念上分離する。

実測が管制メモの内容に**許容誤差以内**で一致したら、当該メモを消すか、色・強調を変えて「遵守済み」と分かるようにする。教育・研究用シミュレータとして、ストリップ／FDPS に近い「管制側が保持するクリアランス」と「レポート（実際）」の併記に近い状況認識を提供する。

---

## Horus における役割とデータの定義（設計の前提）

本プロジェクトでは **Controller と Operator を別クライアント**とみなし、次の用語を仕様・実装・README で揃える。

| 用語（日本語） | 役割 | 主な UI / データ |
|----------------|------|-------------------|
| **管制クリアランス（ATC メモ）** | 管制官が無線で与えた内容を、管制官側が記録した値 | `InstructionMemo`（現状はブラウザローカルのみ）。2-6 ではラベル隣表示のソース。**将来・複数端末ではサーバ永続化**して Operator 画面のラベルとも共有する。 |
| **パイロット操縦目標** | 管制官の指示を聞いたパイロットが、機体に入力した目標（高度・針路・速度） | Operator の `ControlAircraft` → `POST /api/aircraft/control/{callsign}` → バックエンドが機体のナビ目標に反映。**API / DTO / `Aircraft` のフィールド名は従来どおり `instructedVector`**（英語名は歴史的だが、意味は「パイロット適用目標」と定義する）。 |
| **実測（レポート）** | シミュレーション上の実際の位置・ベクトル | `position` / `vector`（および API の同等フィールド）。 |

**重要**: `instructedVector` を「管制官がサーバに書き込んだクリアランス」と読まない。管制官のメモは **別データ**（本 spec では **clearance memo** または `atcClearance` 等の名前で API 化を想定）として設計する。

---

## 背景・課題

### 現状

| 項目 | 状態 |
|------|------|
| **管制メモ** | Controller の `InstructionMemo` のみ。記録は**ローカル state**、バックエンド未送信。ラーダー上には**出ない**。 |
| **パイロット目標** | Operator が `POST .../control/{callsign}` で送信。バックエンドが `instructedVector` として保持し、機動計算に利用。 |
| **ラベル・高度行** | `drawAircraftLabel` で `instructedVector`（パイロット目標）と `position.altitude`（実測）を比較し、`目標FL ↑/↓ 現在FL` 形式で併記。これは **パイロット目標 vs 実測** の表示（2-6 の「管制メモ行」とは別レイヤー）。 |
| **針路・速度** | パイロット目標と実測の差はラベル上では高度以外ほぼ未表現。 |

### 課題（Problem Statement）

- 管制官がメモした**クリアランス**がレーダー上に無く、**パネルだけ**に依存する
- 2 人体制では、Operator 側のラベルでも「管制が何を言ったか」を共有するには **メモのサーバ同期**が必要
- 既存の高度行は有用だが、用語上 **管制クリアランス** と混同しやすい → ドキュメントと将来ラベル copy で区別する

### なぜ今か（Motivation）

- Phase 2 の 2-6、Issue #54 の正式化
- 役割分離（Controller は API で機体を動かさない）を維持したまま、**管制側の状況認識**をレーダーに載せる

---

## 実運用における表示（調査メモ）

公開資料に基づく要約。**クリアランス系**と**レポート（監視）系**を併記するのが一般的。

### 実運用の用語と Horus の対応（更新）

| 概念 | 意味（典型） | Horus での対応 |
|------|----------------|----------------|
| **レポート** | Mode C 等に基づく実際の高度・位置のイメージ | `position` / `vector` |
| **クリアランス（管制が与えた指示）** | ストリップ／FDPS に保持 | **管制クリアランスメモ**（InstructionMemo → 2-6 で API 化した値） |
| **パイロットが従う目標** | FCU 等に設定 | **`instructedVector`**（Operator 入力・サーバ保持） |

### 公開資料・出典

（変更なし）

1. **FAA FOA Ch.8 §3** — [8-3-2 DATA DISPLAY FOR BLOCK ALTITUDE FLIGHTS](https://www.faa.gov/air_traffic/publications/atpubs/foa_html/chap8_section_3.html)
2. **Mode C** — レポート側のイメージ
3. **選択高度とクリアランス** — [Bolt Flight / Mode S selected altitude](https://boltflight.com/understanding-how-air-traffic-controllers-use-selected-altitude-data-from-mode-s/)（参考）
4. **ストリップ / FDPS** — クリアランスと実測の別フィールド管理

---

## 方針

### 決定方針（Decision）

1. **ラベル隣の「指示メモ」行（2-6 の主成果物）**  
   - **データソース**: **管制クリアランスメモ**（InstructionMemo と同義の数値セット: 高度・針路・地速など）。  
   - **現状の実装ステップ**: メモがローカルのみのため、**Phase 1** でバックエンドに **機体単位の管制クリアランス DTO** を持ち、`POST` で Controller（または将来の専用 API）が更新、`GET /api/aircraft/location/all`（または同等）で **全クライアントが同じ値を描画**できるようにする。  
   - **表示内容**: 実測と異なる軸について、ラベルの**右または下**に短いテキスト（例は実装で確定。管制メモであることが分かる接頭辞や色を検討）。

2. **既存の高度併記行（`instructedVector` vs 実測）**  
   - **意味**: **パイロット操縦目標 vs 実測**。2-6 ではこれを「管制メモ行」と混ぜない。  
   - **ドキュメント**: README・本 spec・型の JSDoc で上記の意味を固定。必要なら UI ラベル（開発者向けコメントではなくユーザー向け表記があれば）で「目標」がパイロット入力由来であることを明示。

3. **メモ行のクリア（遵守済み）条件**  
   - **主条件**: **実測**が**管制クリアランスメモ**の当該軸に**許容誤差以内**で一致。  
   - **オプション（後続）**: メモと `instructedVector` の不一致を別色で示し「読み違い／未入力」を訓練表示する（必須ではない）。

4. **Controller の位置ポーリング方針**  
   - `updateAircraftLocationInfo` で `instructedVector` を毎回上書きしない現状は、**パイロット目標を Controller 画面で勝手に上書きしない**という意図と整合。管制メモは **API の clearance フィールド**から読む（実装後）。

### 検討した他案（Alternatives Considered）

- **案 A**: ラベルメモを `instructedVector` だけから生成する。却下: **管制官のメモとパイロット入力は別**というドメインに反する。  
- **案 B**: メモを永続化せず Controller のみ Canvas に描画。却下: Operator 画面と教員視点で**共有できない**。  
- **案 C**: 全文自然言語メモ。却下: 占有面積。短縮記号＋ツールチップは Phase 3 可。

### トレードオフ（Trade-offs）

- **メリット**: 役割分離を保ったまま、管制側クリアランスのレーダー上可視化と多画面一貫性  
- **デメリット**: バックエンド＋API の追加が必須 → **Phase 1 で最小 DTO と 1～2 本の API** に限定して段階導入  
- **混雑**: データブロック設定（2-3）と **「管制メモ行」ON/OFF** を完了条件に含める

---

## データモデルと API（案）

実装時に名前は調整してよいが、概念は以下とする。

| 項目 | 案 |
|------|-----|
| **サーバ保持** | 各 `callsign` に対し、最後に登録された **管制クリアランス**（高度・heading・groundSpeed 等） |
| **更新** | `POST /api/aircraft/{callsign}/atc-clearance`（仮）— Body は `ControlAircraftDto` と同型でも可（意味は管制記録）。Controller 専用 BFF からのみ呼ぶ制約は後続で検討。 |
| **配信** | `GET .../location/all` の各要素に `atcClearance`（仮）を追加、または別 GET でバッチ取得 |
| **フロント** | `InstructionMemo` の「記録」で上記 POST を呼ぶ。ラベルは `atcClearance` vs 実測でメモ行を生成 |

既存の `POST .../control/{callsign}` は **Operator 専用・パイロット目標**のまま変更しない（エンドポイント名の rename は別タスク）。

---

## 表示仕様（ラベル）

| 領域 | 内容 | データ |
|------|------|--------|
| **既存・高度中心** | パイロット目標と実測の関係（現行ロジックを踏襲・文言整理のみ可） | `instructedVector` vs `position` / `vector` |
| **2-6 追加行** | **管制クリアランス**と実測の差（未充足軸を短縮表示） | `atcClearance`（仮）vs 実測 |
| **凡例 / 設定** | 管制メモ行の ON/OFF、色 | `DataBlockDisplaySetting` 拡張を検討 |

---

## 完了条件（Success Criteria）

- [ ] **管制クリアランス**がサーバ経由で取得でき、**全クライアント**で同一内容をラベル（メモ行）に描画できる
- [ ] メモ行は **InstructionMemo の記録操作**と連動（記録＝POST、または同等の一貫性）
- [ ] 実測が管制メモの各軸に**許容内**で一致したとき、当該表示が消える／状態が変わる（テスト可能）
- [ ] **誤差閾値**がコードまたは設定で明示されている
- [ ] README・本 spec・主要 DTO / `Aircraft` で **`instructedVector` = パイロット操縦目標**、管制メモ = 別フィールドと説明されている
- [ ] `npm run lint`（Frontend）および関連テストが通る

---

## 影響範囲

- **Backend**: 管制クリアランスの保持、`ScenarioService` または `Aircraft` 集約へのフィールド追加、API 1～2 本
- **Frontend**: `InstructionMemo`（POST 連携）、`location` DTO / `mapDtoToAircraft`、`drawAircraftLabel`、任意で `DataBlockDisplaySetting`
- **Proto / OpenAPI**: プロジェクト方針に従い位置情報レスポンスを拡張
- **ドキュメント**: `Frontend/README.md`（役割・用語・エンドポイント）

---

## 実装計画

### Phase 1 — 管制クリアランスの永続化と配信

- バックエンドに `atcClearance`（仮）を保持し、更新 API と location レスポンスへの埋め込み
- `InstructionMemo` から更新 API を呼ぶ（記録ボタンまたは自動デバウンスは UX で決定）

### Phase 2 — ラベル描画と閾値

- `drawAircraftLabel` に **管制メモ行**を追加（パイロット目標行と視覚的に区別）
- 閾値定数・色・消去条件の単体テスト

### Phase 3 — 設定・拡張（任意）

- データブロック設定での ON/OFF、ツールチップ
- 管制メモ vs `instructedVector` の不一致ハイライト（訓練用）
- 用語のリネーム（コード上 `instructedVector` → `pilotTarget` 等）は大きな破壊的変更のため別 spec

---

## 検証

- [ ] Controller のみ / Operator のみ / 両方オープンで、同一機の管制メモが一致すること
- [ ] Operator がパイロット目標を変えても、管制メモ行が**勝手に**パイロット値に置き換わらないこと
- [ ] 実測がクリアランスに収束したときの表示変化の自動テスト

---

## 未解決事項（Unresolved Questions）

- 管制クリアランス POST を **Controller セッション限定**にする認証・BFF 方針（単一ユーザ開発では後回し可）
- メモの**具体表記**（略号 vs 日本語）
- 複数クリアランスの履歴（最後の 1 件のみで十分か）

---

## 関連ドキュメント

- [spec/20260318-data-block-display-items/spec.md](../20260318-data-block-display-items/spec.md)
- [Issue #54](https://github.com/Futty93/Horus/issues/54)
- [FAA FOA Chapter 8 Section 3](https://www.faa.gov/air_traffic/publications/atpubs/foa_html/chap8_section_3.html)
