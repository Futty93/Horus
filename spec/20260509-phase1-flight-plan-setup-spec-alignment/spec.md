# Phase 1 — フライトプラン設定まわり spec / Issue 整合

## メタデータ

- **Status**: Done（2026-05-09: 子 spec・親 spec 更新完了）
- **Date**: 2026-05-09
- **親ロードマップ**: [spec/spec.md](../spec.md)（推奨着手順序 1 番）
- **対象 Issue**: [#45](https://github.com/Futty93/Horus/issues/45), [#46](https://github.com/Futty93/Horus/issues/46), [#47](https://github.com/Futty93/Horus/issues/47)（必要に応じて #44 参照）

## 概要

[spec/spec.md](../spec.md) Phase 1 のうち、**フライトプラン設定ページ（`/flight-plan-setup`）周辺**について、**実装が先行している子 spec・Issue の記述をコードベースに合わせて更新**し、**残タスクだけを明示**する。コード変更を主目的とせず、認識のズレを解消して次の実装（Phase 4 や T-3）に進めるための**最初の着手単位**とする。

## 背景・課題

### 現状

- バックエンド: `POST /api/scenario/load`（`ScenarioController`）、フライトプラン系 API は実装済み。統合テスト（`FlightPlanApiIntegrationTest` 等）あり。
- フロントエンド: `Frontend/app/flight-plan-setup/page.tsx` を中心に、テンプレ読込・ATS Suggest・JSON Import/Export・「これで始める」・**機体追加（`AddAircraftForm`）・削除（`AircraftTable`）・初期位置編集（`InitialPositionEditor`）**が実装されている。
- 一方、子 spec には「表示のみ」「未着手」に近い記述が残っているものがあり、[spec/spec.md](../spec.md) の Phase 1 状態と **Issue の説明が実装より古い**可能性がある。

### 課題（Problem Statement）

- ロードマップ・Issue・子 spec の **Status / 完了条件（DoD）** が実装と一致しないと、優先度判断やスプリント計画がブレる。
- 「まだやるべきこと」と「もう済んでいること」が混在し、**レビュー・オンボーディングのコスト**が上がる。

### なぜ今か（Motivation）

- [spec/spec.md](../spec.md) の推奨着手順序で **本項目を 1 番**に置いており、**機能追加の前にドキュメントと計画の単一の真実（single source of truth）を揃える**のが最も費用対効果が高い。
- コード変更なしで完了できる範囲が大きく、**リスクが低い**。

---

## 方針

### 決定方針（Decision）

1. **実装を正**とする。子 spec の「詳細調査結果」「現状の実装」表を、リポジトリ内の実ファイル名・振る舞いに合わせて更新する。
2. 各子 spec の **Status** を更新する（例: `In Progress` → `Done` の条件を満たすなら `Done`、残作業のみなら `In Progress` と **残タスク一覧**を明記）。
3. **新規のプロダクト要件は扱わない**。本 spec のスコープは「整合」。別機能（トースト UI 全面導入など）は、残タスクとして **1〜2 行で指摘**に留め、必要なら **別 spec / Issue** に切り出す。
4. 親 [spec/spec.md](../spec.md) Phase 1 表の「次の具体タスク」列を、整合後の事実と矛盾しないよう **追随更新**する。

### 検討した他案（Alternatives Considered）

- **案 A: 実装を spec に合わせて巻き戻す**
  採用しなかった理由: 実装は既に利用可能で価値が高い。巻き戻しは損失のみ。
- **案 B: spec を更新せず Issue だけ Close**
  採用しなかった理由: 子 spec に古い「課題」が残ると、再び計画が食い違う。

### トレードオフ（Trade-offs）

- **メリット**: チーム全体で同じ優先度認識を持てる。次タスク（Conflict UI、T-3 等）に素直に進める。
- **デメリット / 受容する制約**: GitHub Issue 本文の更新は権限が必要な場合がある → 下記 **Issue コメントテンプレ**を利用。

---

## 完了条件（Success Criteria）

- [x] 以下 3 子 spec の **Status** と「現状」記述を **2026-05-09 時点のコード**に整合:
  - [20260308-json-export-import](../20260308-json-export-import/spec.md)（1-2）
  - [20260315-start-with-this-button](../20260315-start-with-this-button/spec.md)（1-3）
  - [20260315-aircraft-table-edit](../20260315-aircraft-table-edit/spec.md)（1-4）
- [x] 各子 spec に **「残タスク」** 節を追加（Optional / 手動 E2E / 親 spec メンテ）
- [x] [spec/spec.md](../spec.md) Phase 1 行（1-2〜1-4）を更新
- [x] 関連 GitHub Issue（#45–#47）に `gh issue comment` で更新済み（リポジトリ: `HorusATC/Horus`）。いずれも **既に Closed** のため close コマンドは不要だった

---

## GitHub Issue コメント用テンプレ（#45 / #46 / #47）

コピーして各 Issue に投稿してください。

```text
spec 整合（2026-05-09）: docs/spec-phase1-setup-alignment ブランチ
・親: spec/20260509-phase1-flight-plan-setup-spec-alignment/spec.md
・#45 → spec/20260308-json-export-import/spec.md（Status Done、残タスクは Optional のみ）
・#46 → spec/20260315-start-with-this-button/spec.md（Status Done、手動 E2E・Optional 文言のみ）
・#47 → spec/20260315-aircraft-table-edit/spec.md（Status Done、手動 E2E・行内編集は Optional）
コア実装は既に main 系に存在。Issue を Close してよいかご判断ください。
```

（#45 のみ投稿する場合は該当行だけに読み替え）

---

## 影響範囲

- **ドキュメント**: `spec/20260308-json-export-import/spec.md`、`spec/20260315-start-with-this-button/spec.md`、`spec/20260315-aircraft-table-edit/spec.md`、`spec/spec.md`
- **コード**: 変更なし（本タスク範囲）

---

## 実装計画（実行結果）

| Phase | 内容 | 結果 |
|-------|------|------|
| A | `page.tsx`、`scenario.ts`、`ScenarioController`、テストの突合 | ✅ |
| B | 子 spec 3 件の本文更新・残タスク節 | ✅ |
| C | `spec/spec.md` Phase 1 更新 | ✅ |
| C2 | Issue コメント | ✅ \`gh issue comment 45 46 47\`（HorusATC/Horus） |

---

## 検証

- [x] 子 spec の記述と `ScenarioController`（`isSimulationRunning` が load 後も false のまま）、`scenario.test.ts` の存在が一致
- [ ] 手動: `/flight-plan-setup` の smoke（任意・リリース前推奨）

---

## 未解決事項（Unresolved Questions）

- なし（ドキュメントタスク完了）。

---

## 関連ドキュメント

- [spec/spec.md](../spec.md)
- [20260315-scenario-load-api](../20260315-scenario-load-api/spec.md)
- [20260308-flight-plan/spec.md](../20260308-flight-plan/spec.md)
- [Backend/docs/test-data/MANUAL_TEST.md](../../Backend/docs/test-data/MANUAL_TEST.md)

---

## 変更履歴

| 日付 | 内容 |
|------|------|
| 2026-05-09 | Phase B/C 実施。本 spec を Done。Issue テンプレ追加。 |
