# Phase 3 — スクオーク割当・表示（3-1）

## メタデータ

- **Status**: Draft
- **Date**: 2026-05-09

## 概要

航空機にスクオークコードを割り当て、Backend API から Frontend レーダーのデータブロックへ実値表示する。現在 `---` で表示されている項目を運用可能な状態にし、Phase 2-3（データブロック拡張）を完結させる。

## 背景・課題

### 現状

- Frontend は `Frontend/utility/aircraft/drawAircraft.ts` で squawk 行を描画しているが、値が未連携のため `---` 固定表示になっている。
- データブロック表示の ON/OFF は `dataBlockDisplaySettingContext.tsx` と `dataBlockDisplaySetting.tsx` で実装済み。
- Backend 側には squawk 専用の公開DTO項目や割当ロジックが未整備で、`/aircraft/location/all` 経由で値が渡っていない。

### 課題（Problem Statement）

- 表示項目は存在するのに実値が無いため、訓練UIとしての整合性が低い。
- 将来の識別・管制指示（Phase 3 系）に必要な識別子基盤が不足している。

### なぜ今か（Motivation）

2-5（セクター境界線）を後回しにしたため、次は依存が少なく効果が高い 3-1 を先行する。3-1 を完了すると 2-3 の未完了要素（squawk実値）が解消できる。

---

## 方針

### 決定方針（Decision）

1. **Backendドメイン**: 航空機状態に squawk を保持するが、生成時は **未割当（null）をデフォルト** とし、自動採番は行わない。
2. **Backend API**: 位置取得DTO（`/aircraft/location/all` 系）へ squawk 項目を追加し、既存レスポンスへ後方互換的に拡張する。
3. **Frontend変換**: `location.ts` の DTO→`Aircraft` 変換で squawk を受け取り、`drawAircraft.ts` で `---` フォールバックを維持しつつ実値優先表示にする。
4. **表示制御**: 既存のデータブロック表示トグルはそのまま利用し、新規UIは追加しない。
5. **拡張方針（次スライス）**: 特定 squawk の強調表示（完全一致フィルタ、STCA強調より低優先）を別タスクで追加する。

### 検討した他案（Alternatives Considered）

- **案 A**: Frontend 側で疑似 squawk を生成。採用しなかった理由: Backend と不整合になり、将来の指示機能で破綻する。
- **案 B**: 既存レスポンスを変えず専用APIを追加。採用しなかった理由: ポーリング本数が増え、同一データの重複取得になる。
- **案 C**: 生成時に自動で squawk を採番。採用しなかった理由: 「明示的に割り当てる」運用意図とずれ、意図しない値が常に表示される。

### トレードオフ（Trade-offs）

- **メリット**: 既存ポーリング経路で実値表示でき、追加API不要で実装がシンプル。
- **デメリット / 受容する制約**: 初期割当ロジックは簡易実装となり、実運用相当の運用ルールは後続対応になる。

---

## 完了条件（Success Criteria）

- [ ] `controller` / `operator` で squawk 未割当は `---`、割当済みは実値が表示される。
- [ ] Backend の位置取得レスポンスに squawk 項目が含まれる。
- [ ] squawk 未設定時は従来どおり `---` を表示し、画面崩れがない。
- [ ] 自動採番が行われないことをテストで担保する。
- [ ] `npm run lint` / `npm test` / `npm run build`（Frontend）および Backend テストが通る。

---

## 影響範囲

- **Backend/domain + application + dto**: 航空機状態・DTO・位置取得ユースケース
- **Frontend/utility/api/location.ts**: DTO型と変換
- **Frontend/utility/aircraft/drawAircraft.ts**: squawk 描画の実値化
- **Frontend/README.md / Backend/README.md**: squawk 表示仕様の追記

---

## 実装計画

### Phase 1

1. Backend に squawk フィールドを追加（デフォルトは未割当）。
2. 位置取得DTOとAPIレスポンスへ squawk を追加。
3. Frontend の型・変換・描画を更新。
4. README と spec を更新し、回帰テストを実施。

---

## 検証

- [ ] テストが通る
- [ ] ビルドが通る
- [ ] 手動動作確認

---

## 未解決事項（Unresolved Questions）

- 外部入力（シナリオJSON）で squawk を上書き可能にするか。
- 特定 squawk 強調表示を `3-1` に含めるか、`3-1b` として分離するか（推奨: 分離）。

---

## 関連ドキュメント

- [spec/spec.md](../spec.md)
- `Frontend/utility/aircraft/drawAircraft.ts`
- `Frontend/context/dataBlockDisplaySettingContext.tsx`
