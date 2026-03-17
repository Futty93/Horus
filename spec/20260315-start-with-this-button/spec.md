# フライトプラン設定ページ — 「これで始める」ボタン（1-3）

## メタデータ

- **Status**: In Progress
- **Date**: 2026-03-15
- **関連 Issue**: [#46](https://github.com/Futty93/Horus/issues/46)
- **親 spec**: [spec/spec.md Phase 1-3](../../spec/spec.md)、[20260308-flight-plan-setup-page Phase 3](../20260308-flight-plan-setup-page/spec.md)

## 概要

フライトプラン設定ページ（`/flight-plan-setup`）の「これで始める」ボタンにより、編集中のシナリオをバックエンドに送信し、シミュレーションを開始したうえで Operator 画面へ遷移する一連のフローを対象とする。空域設定は Operator が行う場面が多く、遷移先は Operator としている。調査の結果、**基本実装は既に存在し動作している**。本 spec では現状の整理、修正・改善箇所の特定、および実装方針を定義する。

---

## 詳細調査結果

### フロー定義（期待される動作）

| ステップ | 内容 |
|---------|------|
| 1 | シナリオ送信: 編集中の `ScenarioJson` を `POST /api/scenario/load` に送信 |
| 2 | シミュレーション開始: バックエンドが空域クリア・航空機スポーン後、`GlobalVariables.isSimulationRunning = true` に設定 |
| 3 | Operator 遷移: 成功時に `/operator` へルーティングし、レーダー画面で航空機を確認 |

### 現状の実装

| 項目 | 状態 | 場所・詳細 |
|------|------|------------|
| **「これで始める」ボタン** | 実装済み | `FlightPlanSetupActionBar.tsx` 行 87-95。`hasAircraft && !starting` のとき有効 |
| **handleStartWithThis** | 実装済み | `flight-plan-setup/page.tsx` 行 185-201。`loadScenarioAndStart(scenario)` → 成功時 `router.push("/operator")` |
| **loadScenarioAndStart** | 実装済み | `utility/api/scenario.ts` 行 59-76。`fetch("/api/scenario/load")` で POST |
| **BFF プロキシ** | 実装済み | `app/api/scenario/load/route.ts` → `proxyToBackend("/api/scenario/load")` |
| **バックエンド loadScenario** | 実装済み | `ScenarioController.java`。空域クリア→スポーン→`isSimulationRunning=true` |
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
        → 空配列チェック、コールサイン重複チェック、Fix 存在チェック（400 返却）
        → aircraftRepository.clear(), GlobalVariables.isSimulationRunning = false
        → 各機スポーン、GlobalVariables.isSimulationRunning = true
        → 200 { success, scenarioName, aircraftCount, message }
    [フロント] 200 時: setStatus("Scenario loaded. Redirecting..."), router.push("/operator")
    [フロント] 非 200 時: setStatus(`Error: ${result.message}`)
    → setStarting(false)
```

### 修正・改善が必要な箇所

| # | 箇所 | 現状 | 問題 | 重要度 |
|---|------|------|------|--------|
| 1 | **エラー表示** | `loadScenarioAndStart` が `!response.ok` 時に `response.text()` をそのまま `message` に格納 | バックエンドは 400 で `{"success":false,"message":"Duplicate callsign: XXX"}` などの JSON を返す。生の JSON 文字列が status に表示され、ユーザーに分かりにくい | 🔴 Must-have |
| 2 | **単体テスト** | `loadScenarioAndStart` にテストなし | リグレッション検知ができない | 🟡 Should-have |
| 3 | **手動検証** | spec 上「テンプレート読み込み → これで始める → Operator で航空機表示」の確認が未実施 | エンドツーエンド動作保証がない | 🟡 Should-have |
| 4 | **ネットワークエラー** | `fetch` 失敗時に `String(e)`（例: "TypeError: Failed to fetch"）を表示 | 英語の技術的メッセージでユーザーに伝わりにくい場合あり | 🟢 Optional |

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
- [ ] 手動: テンプレート読み込み → 「これで始める」→ Operator 遷移後、航空機がレーダーに表示・飛行する
- [ ] 手動: 不正シナリオ（例: 重複コールサイン）で「これで始める」→ status に `message` の内容（生 JSON でない）が表示される

---

## 未解決事項（Unresolved Questions）

- 1-5（シミュレーション開始画面での JSON アップロード起動）との連携・UI 一貫性は別 spec で検討する

---

## 関連ドキュメント

- [spec/spec.md Phase 1](../../spec/spec.md)
- [spec/20260308-flight-plan-setup-page](../20260308-flight-plan-setup-page/spec.md)
- [spec/20260315-scenario-load-api](../20260315-scenario-load-api/spec.md)
- [Frontend utility/api/scenario.ts](../../Frontend/utility/api/scenario.ts)
- [Backend ScenarioController](../../Backend/src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/interfaces/api/ScenarioController.java)
