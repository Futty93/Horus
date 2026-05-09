# Phase 1 — フライトプラン設定まわり spec / Issue 整合

## メタデータ

- **Status**: Accepted
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
4. 親 [spec/spec.md](../spec.md) Phase 1 表の「次の具体タスク」列を、整合後の事実と矛盾しないよう **1 回だけ追随更新**する（本ブランチ内で実施してよい）。

### 検討した他案（Alternatives Considered）

- **案 A: 実装を spec に合わせて巻き戻す**  
  採用しなかった理由: 実装は既に利用可能で価値が高い。巻き戻しは損失のみ。
- **案 B: spec を更新せず Issue だけ Close**  
  採用しなかった理由: 子 spec に古い「課題」が残ると、再び計画が食い違う。

### トレードオフ（Trade-offs）

- **メリット**: チーム全体で同じ優先度認識を持てる。次タスク（Conflict UI、T-3 等）に素直に進める。
- **デメリット / 受容する制約**: コードの細部まで読み直すため、**初回は 0.5〜1 日程度**の調査・編集が必要。自動テストは「ドキュメントのみ」変更なら必須ではないが、記載内容と実装の突合用に **手動で画面を一度通す**ことを推奨。

---

## 完了条件（Success Criteria）

- [ ] 以下 3 子 spec を読み、**メタデータの Status** と本文の「現状」記述が **2026-05-09 時点のコード**と矛盾しない:
  - [20260308-json-export-import](../20260308-json-export-import/spec.md)（1-2）
  - [20260315-start-with-this-button](../20260315-start-with-this-button/spec.md)（1-3）
  - [20260315-aircraft-table-edit](../20260315-aircraft-table-edit/spec.md)（1-4）
- [ ] 各子 spec に **「残タスク」** セクションまたはチェックリストがあり、**Must-have / Should-have / 別 Issue 化**のいずれかに分類されている（残り無しなら「残タスクなし」と明記）。
- [ ] [spec/spec.md](../spec.md) の Phase 1 行（1-2〜1-4）と **推奨着手順序 1** が、上記更新内容と **矛盾しない**（本 spec へのリンクを 1 行追加してよい）。
- [ ] 関連 GitHub Issue（#45–#47）の本文またはコメントで、**spec 更新を参照**するか、Issue 状態を **Open/Close/Done の理由**が分かるように更新する（権限・運用に応じてコメントのみでも可）。

---

## 影響範囲

- **ドキュメント**: `spec/20260308-json-export-import/spec.md`、`spec/20260315-start-with-this-button/spec.md`、`spec/20260315-aircraft-table-edit/spec.md`、`spec/spec.md`
- **コード**: 原則 **変更しない**。実装と spec の差が **バグ**であると判断した場合のみ、**別 PR** で扱う（本 spec の完了条件からは外す）。

---

## 実装計画

### Phase A: コード突合（読むだけ）

| 順 | 確認対象 | 目的 |
|----|-----------|------|
| A.1 | `Frontend/app/flight-plan-setup/page.tsx` | state 遷移、Import/Export、Suggest、開始フロー、追加・削除・初期位置更新 |
| A.2 | `Frontend/utility/api/scenario.ts` | `parseScenarioJson` / `exportScenario` / `loadScenarioAndStart`（エラー `message` 抽出） |
| A.3 | `Frontend/components/flight-plan-setup/*` | `AircraftTable`、`AddAircraftForm`、`InitialPositionEditor`、`FlightPlanSetupActionBar` |
| A.4 | `Backend/.../ScenarioController.java` + `FlightPlanApiIntegrationTest` | load API の契約（必要なら子 spec の API 節だけ更新） |

### Phase B: 子 spec 更新

| 順 | ドキュメント | 主な更新内容 |
|----|----------------|----------------|
| B.1 | 20260308-json-export-import | 実装済み範囲の明示、残り（バリデーション強化等）の列挙 |
| B.2 | 20260315-start-with-this-button | フロー実装済みの確定、Must-have（エラー UX）の達成状況の照合 |
| B.3 | 20260315-aircraft-table-edit | 「表示のみ」から **追加・削除・初期位置編集実装済み**へ更新し、未実装（例: テーブル内での FP 直接編集）を列挙 |

### Phase C: 親 spec・Issue

| 順 | 作業 |
|----|------|
| C.1 | [spec/spec.md](../spec.md) の Phase 1 表・着手順 1 に本 spec へのリンクと一行サマリ |
| C.2 | #45–#47 の整合（コメント or 本文） |

---

## 検証

- [ ] 上記 **完了条件**のチェックボックスをすべて満たす
- [ ] `Frontend`: 必要なら `npm run lint`（ドキュメントのみならスキップ可）
- [ ] 手動: `/flight-plan-setup` で Template / Import / Export / Add / Delete / Initial position / 「これで始める」を **1 通り**確認し、子 spec の「現状」欄と齟齬がないこと

---

## 未解決事項（Unresolved Questions）

- Issue を Close する権限がローカル作業者にない場合: **コメントで spec パスと残タスクを書く**運用で代替するか、メンテナに依頼する。
- 「これで始める」後のシミュレーション開始タイミング（Operator の START）に関する記述が、バックエンドの `isSimulationRunning` 挙動と一致するかは、**B.2 でコードを再確認**すること。

---

## 関連ドキュメント

- [spec/spec.md](../spec.md)
- [20260315-scenario-load-api](../20260315-scenario-load-api/spec.md)（1-1、バックエンド側 Done の参照）
- [20260308-flight-plan/spec.md](../20260308-flight-plan/spec.md)（フライトプラン機能全体）
- [Backend/docs/test-data/MANUAL_TEST.md](../../Backend/docs/test-data/MANUAL_TEST.md)（手動確認の補助）
