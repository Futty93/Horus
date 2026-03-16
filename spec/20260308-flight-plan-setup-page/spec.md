# フライトプラン設定ページ — シナリオ作成・保存・ロード

## メタデータ

- **Status**: In Progress
- **Date**: 2026-03-08
- **Updated**: 2026-03-08 — ATS 経路提案統合、Route Preview 改善、Load & Suggest Routes 追加

## 概要

**フライトプラン作成はシミュレーションの前に実施する**。専用ページで初期表示データ（航空機＋フライトプラン＋初期位置）をオフライン編集し、JSONとして保存。シミュレーション開始時に、JSONのアップロードまたは「これで始める」により空域情報へ反映する。

## 背景・課題

### ワークフロー（正しい認識）

1. **シミュレーション前**: シナリオ作成フェーズ
   - フライトプラン設定画面で、航空機の初期位置・フライトプラン（ルート）を編集
   - バックエンドに依存せず、ローカルでデータを構築
   - JSON ファイルとして保存可能

2. **シミュレーション開始時**: 次のいずれかで空域を初期化
   - **A**: JSON ファイルをアップロードして空域に反映
   - **B**: 作成画面から「これで始める」を選択し、編集中のシナリオでシミュレーションを開始

### 現状

- **既存実装**: `create-haneda-samples` や `spawn-with-flightplan` は**シミュレーション稼働中**に航空機を追加するAPI
- **設計書**: [flight-plan-implementation.md](../../Backend/docs/design/flight-plan-implementation.md) に `POST /api/scenario/load` が記載されているが未実装
- **UranusAPI**: `/startGame` に `areaInfo` で初期空域を渡す仕様あり（外部システム向け）

### 課題（Problem Statement）

- 現在のフライトプラン設定ページは「稼働中シミュレーションへの割り当て」を前提としており、シナリオ事前作成の用途と合わない
- シナリオを JSON で保存・再利用する機能がない
- シミュレーション開始時に事前定義シナリオを反映する経路がない

### なぜ今か（Motivation）

シミュレーション前にシナリオを用意し、ファイルで共有・再利用できるようにすることで、教育・研究利用の柔軟性を高める。

---

## 方針

### 決定方針（Decision）

`/flight-plan-setup` を**シミュレーション前のシナリオ作成・管理画面**として再定義する。

#### 1. シナリオ編集（オフライン）

- **テンプレート読み込み**: 空の状態から開始、または「Haneda Samples テンプレート」を読み込み（28機分の初期位置・O/D をローカルに展開。バックエンド非依存）
- **Load & Suggest Routes**: テンプレート読み込みと同時に、各 O/D ペアに対して A* 経路提案 API を呼び出し、ルートを自動補完（推奨ワークフロー）
- **O/D グループ化**: 航空機を `originIcao → destinationIcao` でグループ化し、ルートを一括定義
- **経路選択**:
  - **Suggest route**: A* アルゴリズムで空港間の最短経路を自動提案し、グループに適用（1クリック）
  - **ATS ルート検索**: atsLowerRoutes / rnavRoutes から waypoint 候補を検索・採用
  - 手動入力: Waypoint をカンマ区切りで編集
- **テーブル編集**: Callsign | Origin | Dest | Route | Initial Position | 編集

#### 2. JSON 保存・読み込み

- **エクスポート**: 編集中のシナリオを JSON ファイルとしてダウンロード
- **インポート**: JSON ファイルをアップロードしてシナリオを読み込み・編集継続

#### 3. シミュレーション開始

- **これで始める**: 編集中のシナリオをバックエンドに送信し、空域を初期化したうえでシミュレーションを開始。完了後 Controller / Operator へ遷移
- **JSON アップロードで開始**: シミュレーション開始画面（または別エントリ）で JSON をアップロードし、同様に空域初期化 → シミュレーション開始

#### 4.  backward compatibility（既存フロー）

- 「既存機に割り当て」「Haneda Samples 作成」など、シミュレーション稼働中向けの操作は、当面はオプションとして残す（段階的廃止を検討）

### シナリオ JSON 形式

`SpawnWithFlightPlan` の配列をラップする形式とする。

```json
{
  "scenarioName": "Tokyo Approach Training",
  "description": "羽田空港周辺の到着機シナリオ",
  "createdAt": "2026-03-08",
  "aircraft": [
    {
      "flightPlan": {
        "callsign": "JAL101",
        "aircraftType": "B777",
        "departureAirport": "RJBB",
        "arrivalAirport": "RJAA",
        "cruiseAltitude": 35000,
        "cruiseSpeed": 450,
        "route": [
          { "fix": "ABENO", "action": "CONTINUE" },
          { "fix": "SAMON", "action": "CONTINUE" }
        ]
      },
      "initialPosition": {
        "latitude": 34.48,
        "longitude": 136.61,
        "altitude": 31000,
        "heading": 90,
        "groundSpeed": 450,
        "verticalSpeed": 0
      }
    }
  ]
}
```

### 検討した他案（Alternatives Considered）

- **案 A（シミュレーション中のみ）**: 現状の「稼働中に割り当て」のみ。採用しなかった理由: ユーザー要望と異なる
- **案 B（JSON 手書きのみ）**: UI なしで JSON 編集のみ。採用しなかった理由: 非開発者にハードルが高い

### トレードオフ（Trade-offs）

- **メリット**: シナリオの事前作成・保存・共有が可能。シミュレーション開始を明確に分離できる
- **デメリット / 受容する制約**: バックエンドに `POST /api/scenario/load` または `POST /simulation/start-with-scenario` の実装が必要

---

## 完了条件（Success Criteria）

### Must-have（シナリオ事前作成フロー）

- [ ] シナリオをオフラインで編集できる（テンプレート読み込み or 空から開始）
- [ ] シナリオを JSON ファイルとしてエクスポート（ダウンロード）できる
- [ ] JSON ファイルをインポートしてシナリオを読み込み・編集できる
- [ ] 「これで始める」で編集中のシナリオを空域に反映し、シミュレーション開始後 Controller へ遷移できる
- [ ] O/D グループ化、ATS ルート検索、テーブル編集が可能

### Should-have（シミュレーション開始時の JSON アップロード）

- [ ] シミュレーション開始前・開始画面で JSON をアップロードし、空域初期化 → シミュレーション開始できる

### Optional（既存フローとの互換）

- [ ] シミュレーション稼働中に「既存機に割り当て」を実行できる（現行機能の維持）

---

## 影響範囲

- **Frontend**
  - `app/flight-plan-setup/`: シナリオ編集、JSON インポート/エクスポート、「これで始める」UI、経路自動提案（Suggest route / Load & Suggest Routes）、Route Preview（空港座標・動的表示範囲・欠落 fix 表示）
  - `utility/api/ats.ts`: `suggestRoute(origin, destination)` — A* 経路提案 API 呼び出し
  - `app/api/ats/airports/route.ts`: 空港座標取得 BFF（Route Preview 用）
  - `app/page.tsx` またはシミュレーション開始画面: JSON アップロードでの開始オプション
  - `utility/api/`: シナリオロード API、シナリオ形式の型定義
  - Haneda テンプレート: 28機分の `SpawnWithFlightPlan` 相当データを静的または API 取得で保持
- **Backend**
  - 新規: `GET /ats/airports` — 空港座標一覧（Route Preview の Origin/Dest 表示用）
  - 新規: `POST /api/scenario/load` または `POST /simulation/start-with-scenario` — シナリオ JSON を受け取り、空域を初期化してシミュレーション開始
  - 既存の `spawn-with-flightplan` をバッチで呼び出すか、一括ロード用の新エンドポイントを検討
- **Frontend/README.md**: 新ワークフローの説明を追記

---

## 実装計画

### Phase 1: シナリオ編集基盤（Must-have）

| # | タスク | 備考 |
|---|--------|------|
| 1.1 | シナリオデータ構造の定義 | `ScenarioJson` 型、`SpawnWithFlightPlan` 相当の型定義 |
| 1.2 | Haneda テンプレート | 28機分の初期データをフロントエンドに静的保有（create-haneda-samples 相当の座標・O/D） |
| 1.3 | 「テンプレート読み込み」ボタン | テンプレートをローカル状態に展開し、編集可能にする |
| 1.4 | 空から開始 | 航空機0の状態から1機ずつ追加するUI（簡易フォーム） |

### Phase 2: JSON 保存・読み込み（Must-have）

| # | タスク | 備考 |
|---|--------|------|
| 2.1 | エクスポート | 編集中シナリオを JSON でダウンロード（Blob + download 属性） |
| 2.2 | インポート | ファイル選択 → JSON パース → ローカル状態に反映 |
| 2.3 | バリデーション | インポート時の形式チェック、エラー表示 |

### Phase 3: 「これで始める」とバックエンド連携（Must-have）

| # | タスク | 備考 |
|---|--------|------|
| 3.1 | Backend: シナリオロード API | `POST /api/scenario/load` 実装済み。シナリオ JSON を受け取り、空域クリア後各機を spawn-with-flightplan 相当で登録し、シミュレーション開始 |
| 3.2 | Frontend: 「これで始める」ボタン | シナリオを API に送信 → 成功時は Controller へ遷移 |
| 3.3 | エラーハンドリング | バックエンドエラー、ネットワークエラーの表示 |

### Phase 4: シミュレーション開始時の JSON アップロード（Should-have）

| # | タスク | 備考 |
|---|--------|------|
| 4.1 | トップページ or 専用画面 | 「シナリオファイルをアップロードして開始」UI |
| 4.2 | アップロード → シナリオロード API 呼び出し → Controller へ遷移 | Phase 3 の API を再利用 |

### Phase 5: 既存機能の整理（Optional）

| # | タスク | 備考 |
|---|--------|------|
| 5.1 | 「既存機に割り当て」「Haneda Samples 作成」 | シミュレーション稼働中モードとして残すか、別タブ/折りたたみで提供 |
| 5.2 | ドキュメント | Frontend/README.md、本 spec のワークフロー説明を更新 |

---

## 画面構成（ワイヤーフレーム）

```
┌─────────────────────────────────────────────────────────────────────────┐
│ FLIGHT PLAN SETUP — シナリオ作成                                         │
│ Create scenario before simulation. Export/import JSON.                    │
├─────────────────────────────────────────────────────────────────────────┤
│ [Load Template (28)] [Load & Suggest Routes] [Import JSON] [Export] [開始]│
├─────────────────────────────────────────────────────────────────────────┤
│ Grouped by Origin → Destination                                          │
│ ┌─────────────────────────────────────────────────────────────────────┐ │
│ │ RJBB → RJAA (4 aircraft)                                             │ │
│ │   [Suggest route] — or choose from ATS routes: [T09] [T01] ...       │ │
│ │   Route: [_____ABENO SAMON_____] [Alt] [Spd]  [Apply to Group]        │ │
│ └─────────────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────────┤
│ Aircraft Table                                                           │
│ ┌──────────┬──────┬──────┬─────────────────────┬──────────┬──────┬─────┐ │
│ │ Callsign │ Orig │ Dest │ Route               │ Position │ Alt  │ Spd │ │
│ └──────────┴──────┴──────┴─────────────────────┴──────────┴──────┴─────┘ │
├─────────────────────────────────────────────────────────────────────────┤
│ ← Back to Home  |  (シミュレーション稼働中時) [Assign to Existing]       │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 検証

- [ ] フロントエンドのビルドが通る（`npm run build`）
- [ ] Lint が通る（`npm run lint`）
- [ ] テンプレート読み込み → 編集 → エクスポート → インポートの往復が正しく動作する
- [ ] 「Load & Suggest Routes」で各 O/D に経路が自動補完される
- [ ] Route Preview で Origin/Dest/Waypoints がルートに合わせて表示され、欠落時は「Missing: ICAO」を表示する
- [ ] 「これで始める」でシナリオが空域に反映され、Controller で航空機が表示・飛行する

---

## 未解決事項（Unresolved Questions）

- ~~バックエンドのシナリオロード API の詳細仕様~~ → spec 20260315-scenario-load-api で定義済み。空域クリア・複数機一括スポーンを実装
- トップページでの「JSON アップロードして開始」をどこに配置するか（トップのカード、モーダル、専用画面）
- Haneda テンプレートの初期位置データを create-haneda-samples のロジックから取得するか、フロントエンドにハードコードするか

---

## 関連ドキュメント

- [spec/20260308-ats-route-suggest](../20260308-ats-route-suggest/spec.md) — A* 経路提案 API の仕様
- [spec/20260308-flight-plan](../20260308-flight-plan/spec.md)
- [Backend docs: flight-plan-implementation](../../Backend/docs/design/flight-plan-implementation.md)
- [Frontend/README](../../Frontend/README.md)
