# マニュアルテスト手順（フライトプラン機能）

テストデータは **T09 セクター**（伊豆・駿河湾付近、デフォルト表示）に配置しているため、フロントのレーダー中心で航空機が表示される。

## 前提条件

- Java 21 または 22、Gradle 8.8 以上
- バックエンド: `./gradlew bootRun` で起動済み
- フロントエンド: `npm run dev` で起動済み（任意。レーダー表示で確認する場合）

---

## 手順 1: 事前準備

### 1.1 バックエンド起動

```bash
cd Backend
./gradlew bootRun
```

**確認**: コンソールに `Started AtcSimulatorApplication` と表示され、ポート 8080 で待ち受け開始。

### 1.2 Swagger UI を開く

ブラウザで以下を開く:

```
http://localhost:8080/docs.html
```

### 1.3 フロントエンド（任意）

レーダー上で航空機の動きを確認する場合:

```bash
cd Frontend
npm run dev
```

ブラウザで `http://localhost:3333/controller` または `http://localhost:3333/operator` を開く。

---

## 手順 2: シミュレーション開始

### 2.1 POST /simulation/start

1. Swagger UI で `POST /simulation/start` を展開
2. **Execute** をクリック
3. レスポンス **200** を確認

**確認**: バックエンドログに `Simulation started` が出る。

---

## 手順 3: 航空機 spawn（最小テスト）

### 3.1 POST /api/aircraft/spawn-with-flightplan（minimal）

1. `POST /api/aircraft/spawn-with-flightplan` を展開
2. **Try it out** をクリック
3. Request body に以下を設定（または `spawn-with-flightplan-minimal.json` をコピペ）:

```json
{
  "flightPlan": {
    "callsign": "TEST01",
    "aircraftType": "B738",
    "departureAirport": "RJTT",
    "arrivalAirport": "RJAA",
    "cruiseAltitude": 10000,
    "cruiseSpeed": 250,
    "route": [
      { "fix": "KOITO", "action": "CONTINUE" },
      { "fix": "UNAGI", "action": "CONTINUE" }
    ]
  },
  "initialPosition": {
    "latitude": 34.405,
    "longitude": 138.60,
    "altitude": 5000,
    "heading": 270,
    "groundSpeed": 250,
    "verticalSpeed": 0
  }
}
```

※ T09 セクター、KOITO (138.54°E) の東約 5km (138.60°E) に spawn。西向き飛行で KOITO を通過。

4. **Execute** をクリック
5. レスポンス **200**、`"success": true`, `"callsign": "TEST01"` を確認

**確認**: バックエンドログに `Aircraft spawned with flight plan: TEST01` が出る。

---

## 手順 4: フライトプラン状態確認

### 4.1 GET /api/aircraft/{callsign}/flightplan

1. `GET /api/aircraft/{callsign}/flightplan` を展開
2. **Try it out** をクリック
3. **callsign** に `TEST01` を入力
4. **Execute** をクリック
5. レスポンスで以下を確認:
   - `currentWaypointIndex`: 0（MAYAH が次）
   - `remainingWaypoints`: `["KOITO", "UNAGI"]` または同様の内容
   - `navigationMode`: `"ROUTE"` など

---

## 手順 5: 航空機位置・経路進行の確認

### 5.1 GET /aircraft/location/all

1. `GET /aircraft/location/all` を実行
2. レスポンスの JSON 配列に `TEST01` が含まれることを確認
3. `position.latitude`, `position.longitude` が 34.405, 138.60 付近であることを確認

### 5.2 経路通過の確認（約 1 分待機）

シミュレーション 1 秒 = 実時間 1 秒。spawn 位置は KOITO 東約 5km。250kt で約 1 分で KOITO 通過。

**手順**:

1. 約 60 秒待つ（またはフロントのレーダーで航空機が西へ移動するのを確認）
2. `GET /api/aircraft/TEST01/flightplan` を再実行
3. **期待**: `currentWaypointIndex` が 1 に進み、`remainingWaypoints` が `["UNAGI"]` になる

---

## 手順 6: Direct To テスト

### 6.1 Direct To の実行

1. `POST /api/aircraft/{callsign}/direct-to` を展開
2. **Try it out** をクリック
3. **callsign** に `TEST01` を入力
4. Request body に以下を設定（または `direct-to-sample.json` をコピペ）:

```json
{
  "fixName": "UNAGI",
  "resumeFlightPlan": true
}
```

5. **Execute** をクリック
6. レスポンス **200** を確認

**確認**: `GET /api/aircraft/TEST01/flightplan` で `navigationMode` が `"DIRECT_TO"` 相当に変わり、目標 fix が UNAGI になっていること。

---

## 手順 7: Resume Navigation テスト

### 7.1 POST /api/aircraft/{callsign}/resume-navigation

1. `POST /api/aircraft/{callsign}/resume-navigation` を展開
2. **Try it out** をクリック
3. **callsign** に `TEST01` を入力
4. **Execute** をクリック
5. レスポンス **200** を確認

**確認**: フライトプラン経路に戻り、`navigationMode` が `"ROUTE"` になる。

---

## 手順 8: フルルート spawn（JAL512）

### 8.1 POST /api/aircraft/spawn-with-flightplan（sample）

1. `POST /api/aircraft/spawn-with-flightplan` で **Try it out**
2. Request body に `spawn-with-flightplan-sample.json` の内容を設定:

```json
{
  "flightPlan": {
    "callsign": "JAL512",
    "aircraftType": "B738",
    "departureAirport": "RJTT",
    "arrivalAirport": "RJAA",
    "cruiseAltitude": 10000,
    "cruiseSpeed": 250,
    "route": [
      { "fix": "KOITO", "action": "CONTINUE" },
      { "fix": "BOKJO", "action": "CONTINUE" },
      { "fix": "AOIKU", "action": "CONTINUE" }
    ]
  },
  "initialPosition": {
    "latitude": 34.41,
    "longitude": 138.60,
    "altitude": 3000,
    "heading": 270,
    "groundSpeed": 250,
    "verticalSpeed": 500
  }
}
```

3. **Execute** をクリック
4. レスポンス **200**、`"callsign": "JAL512"` を確認

**確認**: 約 1 分で KOITO 通過、その後 BOKJO → AOIKU へ順に進行。

### 8.2 JAL512 のフライトプラン確認

- `GET /api/aircraft/JAL512/flightplan` で `remainingWaypoints` が `["KOITO", "BOKJO", "AOIKU"]` であることを確認

---

## 手順 9: シミュレーション一時停止

### 9.1 POST /simulation/pause

1. `POST /simulation/pause` を実行
2. レスポンス **200** を確認
3. `GET /aircraft/location/all` の位置が変化しなくなることを確認

---

## チェックリスト（概要）

| # | 項目 | 結果 |
|---|------|------|
| 1 | シミュレーション開始 | OK / NG |
| 2 | spawn-with-flightplan（minimal） | OK / NG |
| 3 | フライトプラン取得 | OK / NG |
| 4 | 航空機位置取得・進行 | OK / NG |
| 5 | KOITO 通過（約 1 分後） | OK / NG |
| 6 | Direct To UNAGI | OK / NG |
| 7 | Resume Navigation | OK / NG |
| 8 | spawn-with-flightplan（JAL512） | OK / NG |
| 9 | シミュレーション一時停止 | OK / NG |

---

## マニュアルテストと Java テストの対応

| # | マニュアル項目 | Java テスト | ファイル |
|---|----------------|-------------|----------|
| 1 | シミュレーション開始 | `simulationApi_start_returns200` | BackendRedesignIntegrationTest |
| 2 | spawn-with-flightplan（minimal） | `spawnWithFlightPlan_createsAircraft` | FlightPlanApiIntegrationTest |
| 3 | フライトプラン取得 | `getFlightPlan_returnsStatus` | FlightPlanApiIntegrationTest |
| 4 | 航空機位置取得 | `locationApi_getAll_returnsJsonArray` | BackendRedesignIntegrationTest |
| 5 | KOITO 通過 | `waypointPass_detectedWhenMovingAwayWithinThreshold`, `afterWaypointPass_instructedVectorPointsToNextWaypoint` | CommercialAircraftFlightPlanRegressionTest |
| 6 | Direct To | `directTo_appliesInstruction`, `directToFix_*`, `directTo_updatesInstructedVectorTowardTarget` | FlightPlanApiIntegrationTest, ScenarioServiceFlightPlanTest, CommercialAircraftFlightPlanRegressionTest |
| 7 | Resume Navigation | `resumeNavigation_appliesInstruction`, `resumeNavigation_setsFlightPlanMode` | FlightPlanApiIntegrationTest, ScenarioServiceFlightPlanTest |
| 8 | spawn-with-flightplan（JAL512） | `spawnWithFlightPlan_createsAircraft`（同 API、別ルート） | FlightPlanApiIntegrationTest |
| 9 | シミュレーション一時停止 | `simulationApi_pause_returns200` | BackendRedesignIntegrationTest |

---

## ウェイポイント通過のデバッグ

ウェイポイント通過判定のログを標準出力に出す場合:

```bash
./gradlew bootRun -PwpDebug
```

出力例: `[WP_DEBUG] TEST01 KOITO dist=0.4500 NM thresh=1.5000 NM within=true movingAway=true passed=true`

---

## トラブルシューティング

### 航空機が動かない

- `POST /simulation/start` が実行されているか確認
- フロントの **START SIMULATION** ボタンを押しているか確認

### 404 / 航空機が見つからない

- callsign の大文字・小文字が一致しているか確認（TEST01, JAL512）
- 先に spawn を実行しているか確認

### 経路を通過しない

- spawn の `latitude`, `longitude`, `heading` が経路方向を向いているか確認
- `heading` 270 は西方向。KOITO (138.54°E) へ向かうには西向きが必要
