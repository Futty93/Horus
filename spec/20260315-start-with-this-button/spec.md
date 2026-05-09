# フライトプラン設定ページ — 「これで始める」ボタン（1-3）

## メタデータ

- **Status**: Done（Must-have / Should-have コード・テスト充足。手動 E2E・Optional のみ残り）
- **Date**: 2026-03-15
- **整合更新**: 2026-05-09（[20260509-phase1-flight-plan-setup-spec-alignment](../20260509-phase1-flight-plan-setup-spec-alignment/spec.md)）
- **関連 Issue**: [#46](https://github.com/Futty93/Horus/issues/46)
- **親 spec**: [spec/spec.md Phase 1-3](../../spec/spec.md)、[20260308-flight-plan-setup-page Phase 3](../20260308-flight-plan-setup-page/spec.md)

## 概要

フライトプラン設定ページ（`/flight-plan-setup`）の「これで始める」ボタンにより、編集中のシナリオをバックエンドに送信し、空域に反映したうえで Operator 画面へ遷移する一連のフローを対象とする。シミュレーション開始は Operator 画面で START SIMULATION ボタンを押すまで行われない。空域設定は Operator が行う場面が多く、遷移先は Operator としている。調査の結果、**基本実装は既に存在し動作している**。本 spec では現状の整理、修正・改善箇所の特定、および実装方針を定義する。

---

## 詳細調査結果

### フロー定義（期待される動作）

| ステップ | 内容 |
|---------|------|
| 1 | シナリオ送信: 編集中の `ScenarioJson` を `POST /api/scenario/load` に送信 |
| 2 | 空域反映: バックエンドが `GlobalVariables.isSimulationRunning = false` のまま空域クリア・スポーン（**自動ではシミュレーションは走らない**。Operator で START SIMULATION するまで待機） |
| 3 | Operator 遷移: 成功時に `/operator` へルーティングし、レーダーで航空機を確認 |

### 現状の実装

| 項目 | 状態 | 場所・詳細 |
|------|------|------------|
| **「これで始める」ボタン** | 実装済み | `FlightPlanSetupActionBar.tsx`。`hasAircraft && !starting` のとき有効 |
| **handleStartWithThis** | 実装済み | `flight-plan-setup/page.tsx`。`loadScenarioAndStart(scenario)` → 成功時 `router.push("/operator")` |
| **loadScenarioAndStart** | 実装済み | `utility/api/scenario.ts`。`fetch("/api/scenario/load")`、非 OK 時は `parseJsonMessage` で JSON の `message` を抽出 |
| **BFF プロキシ** | 実装済み | `app/api/scenario/load/route.ts` → `proxyToBackend("/api/scenario/load")` |
| **バックエンド loadScenario** | 実装済み | `ScenarioController.java`。load 開始時に `isSimulationRunning=false`、クリア→スポーン後も **true に戻さない**（`FlightPlanApiIntegrationTest` の「load 後もシミュレーション未開始」参照） |
| **Operator ページ** | 存在 | `app/operator/page.tsx`。RadarCanvas + 管制指示入力パネル |

### 処理フロー（現状）

```
[フロント] ユーザーが「これで始める」クリック
    → scenario.aircraft.length === 0 なら "Error: No aircraft to load" 表示
    → setStarting(true), setStatus("Loading scenario...")
    → loadScenarioAndStart(scenario)
        → fetch POST /api/scenario/load (Next.js BFF)
        → BFF が Backend へプロキシ
    [バックエンド] ScenarioController.loadScenario
        → 空配列・重複コールサイン・Fix 解決エラー時は 400 + JSON（message）
        → GlobalVariables.isSimulationRunning = false, aircraftRepository.clear()
        → 各機スポーン（この処理ブロック内で isSimulationRunning を true にしない）
        → 200 { success, scenarioName, aircraftCount, message }
    [フロント] 200 時: setStatus("Scenario loaded. Redirecting..."), router.push("/operator")
    [フロント] 非 200 時: setStatus(`Error: ${result.message}`)
    → setStarting(false)
```

### 修正・改善が必要な箇所（2026-05-09 更新）

| # | 箇所 | 状態 | 備考 |
|---|------|------|------|
| 1 | **エラー表示（JSON message 抽出）** | ✅ 実装済 | `scenario.ts` の `parseJsonMessage`。400 時は `message` を status に表示可能 |
| 2 | **単体テスト** | ✅ | `Frontend/utility/api/scenario.test.ts` の `loadScenarioAndStart` describe |
| 3 | **手動 E2E** | ⬜ 推奨 | リリース前にテンプレート → これで始める → Operator を実施（チェックリストは下節「検証」） |
| 4 | **ネットワークエラー文言** | ⬜ Optional | 日本語の汎用メッセージへの置換は未実装 |

### 既存 spec との整合

| ドキュメント | 記載 | 現状 |
|-------------|------|------|
| 20260308-flight-plan-setup-page Phase 3.2 | シナリオを API に送信 → 成功時は Operator へ遷移 | ✅ 実装済み |
| 20260308-flight-plan-setup-page Phase 3.3 | エラーハンドリング: バックエンドエラー、ネットワークエラーの表示 | ⚠️ エラー内容の可読性に改善余地 |
| 20260315-scenario-load-api | POST /api/scenario/load のバックエンド仕様 | ✅ 実装完了。1-3 はフロント連携部分 |

---

## 方針

### 決定方針（Decision）

**既存実装をベースに、エラー UX の改善とテスト追加で完了とする**。フロー自体は実装済みのため、以下の修正・追加のみ行う。

1. **エラー表示の改善（Must-have）**: `loadScenarioAndStart` で 4xx/5xx 時、レスポンスが JSON かつ `message` プロパティを持つ場合はそれを抽出して返す。そうでなければ `response.text()` を返す（従来どおり）。
2. **単体テスト（Should-have）**: `loadScenarioAndStart` のユニットテストを追加。`fetch` をモックし、200 時・400 時・ネットワークエラー時の挙動を検証する。
3. **手動検証（Should-have）**: 本 spec の検証チェックリストに「テンプレート読み込み → これで始める → Operator で航空機が表示・飛行する」を明記し、リリース前に確認する。
4. **ネットワークエラーメッセージ（Optional）**: `Failed to fetch` 等の場合は「サーバーに接続できません。バックエンドが起動しているか確認してください。」のような日本語メッセージに変換する。実装コストが低ければ実施。

### なぜこの方針か

- **フローは完成している**: 1-1（POST /api/scenario/load）が完了しており、フロントの「これで始める」から Operator 遷移まで一連の流れが成立している。
- **UX 改善の優先度**: バックエンドが 400 で返す `message`（例: "Duplicate callsign: JAL101", "Fix not found: XXX"）はユーザーにとって有用。生 JSON 表示は避けるべき。
- **テストによる堅牢化**: `loadScenarioAndStart` は外部 API 呼び出しを伴うため、モックによる単体テストで契約変更やリグレッションを検知できる。

### 検討した他案（Alternatives Considered）

- **案 A: エラー表示を現状のまま**  
  採用しなかった理由: バックエンドが 1-1 で 400 時に `message` を返すように改善済み。フロントでそれを活かさないと UX が損なわれる。

- **案 B: Controller / Operator の遷移先をユーザー選択**  
  採用しなかった理由: 空域設定は Operator が行う場面が多いため、「これで始める」の遷移先は Operator に統一。両方へのリンク（FlightPlanSetupNav）は既にある。

### トレードオフ（Trade-offs）

- **メリット**: エラー時のユーザー体験向上、テストによる品質担保
- **デメリット / 受容する制約**: ネットワークエラーの文言改善は Optional。他画面（例: 1-5 の JSON アップロード起動）との一貫性は別 spec で検討する。

---

## 完了条件（Success Criteria）

### Must-have

- [x] `loadScenarioAndStart` が 4xx/5xx 時にレスポンス JSON の `message` を抽出して返却する（存在する場合）
- [x] バックエンドが 400 で `{ message: "..." }` を返したとき、status に生 JSON ではなく `message` の内容が表示される

### Should-have

- [x] `loadScenarioAndStart` のユニットテストが存在し、以下をカバーする
  - 200 時: `{ ok: true }` を返す
  - 400 時（JSON body に `message` あり）: `{ ok: false, message: "..." }` で `message` が抽出される
  - 400 時（JSON body に `message` なし）: `{ ok: false, message: responseText }`
  - ネットワークエラー（fetch throw）: `{ ok: false, message: String(e) }`
- [x] 検証チェックリストに「これで始める」→ Operator 遷移の手動確認が含まれる

### Optional

- [ ] ネットワークエラー時にユーザーフレンドリーな日本語メッセージを表示する

---

## 影響範囲

- **Frontend**
  - `utility/api/scenario.ts`: `loadScenarioAndStart` のエラーレスポンス処理を修正
  - `utility/api/scenario.test.ts`（または新規）: `loadScenarioAndStart` のユニットテスト追加
- **spec**
  - 本 spec: 完了条件のチェック
  - 20260308-flight-plan-setup-page: Phase 3.2, 3.3 の完了を記録（本 spec 完了後）

---

## 実装計画

### Phase 1: エラー表示の改善（Must-have）

1. `loadScenarioAndStart` 内で `!response.ok` 時:
   - `Content-Type: application/json` かつ body が JSON パース可能で `message` プロパティがあれば、それを `message` に使用
   - それ以外は `response.text()` をそのまま使用
2. 既存の `catch` は維持（ネットワークエラー時は `String(e)`）

### Phase 2: 単体テスト（Should-have）

1. `scenario.test.ts` に `loadScenarioAndStart` のテストを追加
2. `global.fetch` をモック（または `jest.spyOn`）
3. 上記完了条件の各ケースを検証

### Phase 3: 検証・ドキュメント（Should-have）

1. 本 spec の検証チェックリストを実行
2. 20260308-flight-plan-setup-page の Phase 3 該当タスクを「完了」に更新（明示的指示がある場合のみ。ルールに従い通常は未指定時は触らない）

---

## 検証

- [x] フロントエンドのビルドが通る（`npm run build`）
- [x] Lint が通る（`npm run lint`）
- [x] 単体テストが通る（`npm test`）
- [ ] 手動: テンプレート読み込み → 「これで始める」→ Operator 遷移後、航空機がレーダーに表示される。START SIMULATION を押すまで飛行しない
- [ ] 手動: 不正シナリオ（例: 重複コールサイン）で「これで始める」→ status に `message` の内容（生 JSON でない）が表示される

---

## 残タスク（2026-05-09 時点）

| 分類 | 内容 |
|------|------|
| **運用** | 手動検証（検証節の未チェック 2 項目）をリリース前に実施 |
| **Optional** | ネットワークエラー時の日本語メッセージ |
| **別 spec** | 1-5（トップからの JSON 起動）との UI 一貫性 |

---

## 未解決事項（Unresolved Questions）

- 1-5（シミュレーション開始画面での JSON アップロード起動）との連携・UI 一貫性は別 spec で検討する

---

## 変更履歴

| 日付 | 内容 |
|------|------|
| 2026-05-09 | Status Done。`ScenarioController` の `isSimulationRunning` 挙動をコードに合わせ修正。改善項目表を実装済みに更新。 |

---

## 関連ドキュメント

- [spec/spec.md Phase 1](../../spec/spec.md)
- [spec/20260308-flight-plan-setup-page](../20260308-flight-plan-setup-page/spec.md)
- [spec/20260315-scenario-load-api](../20260315-scenario-load-api/spec.md)
- [Frontend utility/api/scenario.ts](../../Frontend/utility/api/scenario.ts)
- [Backend ScenarioController](../../Backend/src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/interfaces/api/ScenarioController.java)
