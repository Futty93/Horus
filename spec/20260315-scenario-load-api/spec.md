# POST /api/scenario/load バックエンド実装（1-1）

## メタデータ

- **Status**: Done
- **Date**: 2026-03-15
- **関連 Issue**: [#44](https://github.com/Futty93/Horus/issues/44)
- **親 spec**: [spec/spec.md Phase 1-1](../../spec/spec.md)、[20260308-flight-plan-setup-page Phase 3.1](../20260308-flight-plan-setup-page/spec.md)

## 概要

複数航空機を一括でシナリオとして登録・スポーンする `POST /api/scenario/load` のバックエンド実装を完成させる。sim spec では「未着手」とあるが、**実装は既に存在し動作している**。本 spec では詳細調査に基づき、実装の完成度向上（テスト・ドキュメント・検証）および設計書との整合を図る方針を定める。

---

## 詳細調査結果

### 現状の実装

| 項目 | 状態 | 詳細 |
|------|------|------|
| **ScenarioController** | 実装済み | `Backend/.../interfaces/api/ScenarioController.java` |
| **loadScenario メソッド** | 実装済み | `POST /api/scenario/load`、`ScenarioLoadDto` 受付 |
| **DTO 構造** | 整合 | `ScenarioLoadDto` → `SpawnWithFlightPlanDto[]`。フロント `ScenarioJson` と互換 |
| **フロント連携** | 動作中 | `loadScenarioAndStart()` → Next.js BFF `/api/scenario/load` → Backend。flight-plan-setup の「これで始める」で利用 |
| **UranosAPI.yml** | 未記載 | OpenAPI に `/api/scenario/load` が存在しない |

### 処理フロー（現状）

```
1. aircraftRepository.clear() で空域クリア
2. GlobalVariables.isSimulationRunning = false（一時停止）
3. 各 aircraft をループ:
   - initialPosition が null ならスキップ（warn ログ）
   - FlightPlanFromDtoConverter で FlightPlan に変換
   - createAircraftFromSpawn で CommercialAircraft 生成（CreateAircraftDto 経由）
   - scenarioService.spawnAircraft(aircraft)
4. GlobalVariables.isSimulationRunning = true（再開）
5. { success, scenarioName, aircraftCount, message } を返却
```

### 設計書との差分

| 項目 | flight-plan-implementation.md | 現状実装 |
|------|-------------------------------|----------|
| シナリオ形式 | `aircraft[]` に `spawnTime` + `flightPlan` | `aircraft[]` に `flightPlan` + `initialPosition` |
| spawnTime | 遅延スポーン（秒）を想定 | 非対応。全機即時スポーン |
| initialPosition | 設計書の古い形式では記載なし | 必須。フロント flight-plan-setup と一致 |

**結論**: フロントエンド（flight-plan-setup-page）は `flightPlan` + `initialPosition` 形式を使用。設計書の `spawnTime` 形式は現行実装・フロントと異なり、**現状の形式を正とする**。設計書は将来拡張として `spawnTime` を追記可能。

### 不足している点

| 不足 | 影響度 | 備考 |
|------|--------|------|
| 単体・統合テストなし | 高 | リグレッション検知ができない |
| UranosAPI.yml に未記載 | 中 | Swagger/OpenAPI から利用者が発見しにくい |
| コールサイン重複チェックなし | 中 | 同一シナリオ内で重複コールサインがある場合、後勝ちで上書き。spawn-with-flightplan は `AircraftConflictException` を投げるが load では未検証 |
| Route 内 Fix 不存在時の扱い | 中 | `FlightPlanFromDtoConverter` が `findFixPositionByName` で `IllegalArgumentException` を投げる。Controller はキャッチせず 500 になる |
| ETA の扱い | 低 | 現状 `DEFAULT_SIMULATION_ETA` 固定。フロントは ETA を送らない。設計上許容 |

### フロント・バック契約

- **リクエスト**: `ScenarioLoadDto` = `{ scenarioName?, description?, createdAt?, aircraft: SpawnWithFlightPlanDto[] }`
- **SpawnWithFlightPlanDto**: `{ flightPlan: FlightPlanDto, initialPosition: InitialPositionDto, spawnTime?: number }`（spawnTime は将来の遅延スポーン用。現状は省略可）
- **FlightPlanDto**: callsign, aircraftType?, departureAirport, arrivalAirport, cruiseAltitude, cruiseSpeed, route?
- **InitialPositionDto**: latitude, longitude, altitude, heading, groundSpeed, verticalSpeed
- **レスポンス**: `{ success: true, scenarioName, aircraftCount, message }`

---

## 方針

### 決定方針（Decision）

**既存実装をベースに完成度を高める**。ゼロから実装するのではなく、以下で仕上げる。

1. **テスト追加**: `ScenarioController.loadScenario` の統合テストを追加し、正常系・異常系（空配列、initialPosition 欠損、Fix 不存在）をカバーする
2. **UranosAPI.yml 更新**: `POST /api/scenario/load` を OpenAPI に追記し、リクエスト/レスポンススキーマを明示する
3. **ドキュメント整備**: Backend README、本 spec、20260308-flight-plan 4.9 を「実装済み」に更新する
4. **コールサイン重複**: 400 を返す。同一シナリオ内での重複は入力ミスとみなし、spawn-with-flightplan との一貫性も保つ
5. **Fix 不存在時**: 400 で `{ "success": false, "message": "Fix not found: XXX" }` を返す。5xx はサーバー障害向けであり、ルート typo はクライアントエラー
6. **spawnTime 設計許容性**: `SpawnWithFlightPlanDto` にオプショナルな `spawnTime` (Integer) を追加。null/0 = 即時スポーン。将来 `spawnTime > 0` で遅延スポーン対応可能に

### なぜこの方針か

- **既存実装が動作している**: フロントの「これで始める」から呼ばれ、Controller 遷移まで一連のフローが成立している
- **エラーハンドリング強化**: コールサイン重複・Fix 不存在時に 400 を返すことで、ユーザーが原因を把握しやすく、spawn-with-flightplan との一貫性も保つ
- **spawnTime 拡張性**: DTO にフィールドを追加するだけで将来対応可能。現状は読み捨てで即時スポーンを維持

### 検討した他案（Alternatives Considered）

- **案 A: コールサイン重複・Fix 不在を現状のまま（後勝ち・500）**  
  採用しなかった理由: ユーザーが不正 JSON を送った場合の UX が悪く、デバッグしづらい

- **案 B: spawnTime を DTO に追加しない**  
  採用しなかった理由: 設計書に spawnTime 形式があり、将来サポート時の DTO 変更を避けたい

### トレードオフ（Trade-offs）

- **メリット**: エラー時の UX 向上、spawn-with-flightplan との一貫性、spawnTime の将来拡張が容易
- **デメリット / 受容する制約**: spawnTime > 0 の遅延スポーン実装は本 spec のスコープ外。現状はフィールド追加のみ

---

## 完了条件（Success Criteria）

- [x] `ScenarioController.loadScenario` の統合テストが存在し、以下をカバーする
  - 正常系: 1 機以上のシナリオでロード成功、aircraftCount が一致、シミュレーション再開
  - 異常系: aircraft 空配列で 400、initialPosition 欠損の 1 機はスキップされ残りは成功、コールサイン重複で 400、Fix 不存在で 400
- [x] コールサイン重複時に 400 を返す
- [x] Fix 不存在時に 400 で明示的エラーメッセージを返す
- [x] `SpawnWithFlightPlanDto` に `spawnTime` (Integer, オプショナル) を追加
- [x] UranosAPI.yml に `POST /api/scenario/load` のパスと `ScenarioLoadDto` 相当のスキーマが記載されている（`spawnTime` 含む）
- [x] spec/20260308-flight-plan の Phase 4 テーブルで 4.9 が「完了」に更新されている
- [x] Backend README に ScenarioController は README の改善項目セクションに言及あり。OpenAPI で仕様公開済み

---

## 影響範囲

- **Backend**
  - 新規: `ScenarioControllerIntegrationTest` または既存統合テストへの `loadScenario` テスト追加
  - 更新: `UranosAPI.yml` に `/api/scenario/load` 追記
  - 更新: `Backend/README.md`（該当箇所の確認・追記）
- **spec**
  - 更新: `spec/20260308-flight-plan/spec.md` 4.9 を「完了」に変更
  - 更新: `spec/20260308-flight-plan-setup-page/spec.md` の Phase 3.1 を「完了」に変更（該当する場合）

---

## 実装計画

### Phase 1: 統合テスト追加（Must-have）

1. 既存 `FlightPlanApiIntegrationTest` に `loadScenario` ケースを追加するか、新規 `ScenarioControllerIntegrationTest` を作成
2. テストケース:
   - `loadScenario_success_withMultipleAircraft`: 2 機以上のシナリオを POST、200、aircraftCount 一致、リポジトリに機が存在
   - `loadScenario_returns400_whenAircraftEmpty`: aircraft が `[]` なら 400
   - `loadScenario_skipsAircraftWithoutInitialPosition`: initialPosition が null の 1 機を含むシナリオで、他機はスポーンされスキップ数が count に反映されないことの確認（スキップ時は count に含めない現状の仕様を検証）

### Phase 2: OpenAPI 更新（Must-have）

1. `UranosAPI.yml` の `paths` に `/api/scenario/load` を追加
2. リクエストボディに `ScenarioLoadDto`（`ScenarioLoad` スキーマ名で）を定義。`SpawnWithFlightPlan` は既存スキーマを参照
3. レスポンス 200 の例を記載

### Phase 3: ドキュメント更新（Must-have）

1. `spec/20260308-flight-plan/spec.md` の 4.9 を「完了」に更新
2. `spec/20260308-flight-plan-setup-page/spec.md` の Phase 3.1 および未解決事項の該当記述を更新
3. Backend README のシナリオ API 説明を確認し、不足があれば追記

---

## 検証

- [x] Backend の全テストが通る（`./gradlew test`）
- [ ] Backend のビルドが通る（`./gradlew build`）
- [ ] 手動: フライトプラン設定ページで「Haneda テンプレート読み込み」→「これで始める」→ Controller 遷移後、航空機が表示・飛行することを確認

---

## 未解決事項（Unresolved Questions）

- `spawnTime > 0` の遅延スポーン実装は将来タスクとする

---

## 関連ドキュメント

- [spec/spec.md Phase 1](../../spec/spec.md)
- [spec/20260308-flight-plan-setup-page](../20260308-flight-plan-setup-page/spec.md)
- [spec/20260308-flight-plan](../20260308-flight-plan/spec.md)
- [Backend docs: flight-plan-implementation](../../Backend/docs/design/flight-plan-implementation.md)
- [Backend ScenarioController](../../Backend/src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/interfaces/api/ScenarioController.java)
