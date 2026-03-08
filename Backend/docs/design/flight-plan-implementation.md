# フライトプラン機能 実装設計書

## 概要

本ドキュメントは、Horusシミュレーターにフライトプラン機能を追加するための詳細な実装方針を定めるものである。

### 背景と目的

現在のHorusシミュレーターでは、航空機の初期位置（緯度・経度・高度）と初期ベクトル（速度・方位・垂直速度）のみを指定して航空機を生成している。しかし、実際の航空管制では航空機はフライトプランに従って飛行し、管制官はそのフライトプランを基に管制指示を出す。

本機能の実装により、以下が可能となる：
- 航空機が自動的にウェイポイントを順次通過する飛行
- 管制官による介入（ヘディング指示等）とフライトプランへの復帰
- より現実的な管制シミュレーション

### 対象スコープ

- **対象**: エンルート管制、ターミナルレーダー管制
- **対象外**: タワー管制（航空機はタワー移管時点で表示から削除）

---

## 設計方針

### 1. ナビゲーションモードの設計

#### 選定理由

実際の航空管制では、航空機は以下の3つの状態のいずれかで飛行する：
1. フライトプランに従った自動飛行
2. 管制官からのヘディング指示に従った飛行（レーダーベクター）
3. 特定のFixへの直行（ダイレクト）

これらを明確に区別することで、管制官の指示と航空機の自動飛行を適切に制御できる。

#### NavigationMode 定義

```java
public enum NavigationMode {
    /**
     * フライトプランに従って自動飛行
     * - ウェイポイントを順次通過
     * - 高度・速度制約を自動適用
     */
    FLIGHT_PLAN,

    /**
     * 管制官からのヘディング指示に従う
     * - フライトプランから離脱した状態
     * - 次の管制指示を待つ
     */
    HEADING,

    /**
     * 指定されたFixへ直行
     * - フライトプラン上のFixなら、到達後フライトプラン再開
     * - フライトプラン外のFixなら、到達後HEADING状態へ
     */
    DIRECT_TO
}
```

#### 状態遷移図

```
                    ┌─────────────────┐
                    │   FLIGHT_PLAN   │
                    │  (自動飛行中)    │
                    └────────┬────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
            ▼                ▼                ▼
    ヘディング指示      Direct To指示    最終WP通過
            │                │                │
            ▼                ▼                ▼
    ┌───────────┐    ┌───────────┐    ┌───────────┐
    │  HEADING  │    │ DIRECT_TO │    │  削除/    │
    │(指示待ち) │    │ (直行中)  │    │  非表示   │
    └─────┬─────┘    └─────┬─────┘    └───────────┘
          │                │
          │    Resume      │  Fix到達
          │    Navigation  │
          ▼                ▼
    ┌─────────────────────────┐
    │      FLIGHT_PLAN        │
    │   (フライトプラン再開)   │
    └─────────────────────────┘
```

---

### 2. ウェイポイント通過判定

#### 選定理由：距離ベース方式

2つの方式を検討した結果、**距離ベース方式**を採用する。

| 評価項目 | 距離ベース | セクター通過 |
|----------|-----------|-------------|
| 実装の複雑さ | ◎ シンプル | △ 複雑 |
| 計算コスト | ◎ 低い | △ 高い |
| 実際のFMSとの類似性 | ◎ 近い | △ 異なる |
| 旋回中の判定精度 | ○ 良好 | △ 問題あり |
| 調整の容易さ | ◎ しきい値のみ | △ 複数パラメータ |

**採用理由**:
1. 実際の航空機のFMS（Flight Management System）も距離ベースで次のウェイポイントへの切り替えを行う
2. 実装がシンプルで、バグが発生しにくい
3. パフォーマンスへの影響が小さい（大量の航空機をシミュレートする際に重要）

#### 通過判定アルゴリズム

```java
/**
 * ウェイポイント通過判定
 *
 * 条件1: ウェイポイントまでの距離がしきい値以下
 * 条件2: 前回の距離より現在の距離が大きい（通り過ぎた）
 *
 * 両方の条件を満たした場合に「通過」と判定
 */
public boolean hasPassedWaypoint(FixPosition waypoint) {
    double currentDistance = calculateDistanceTo(waypoint);
    double threshold = calculateDynamicThreshold();

    boolean withinThreshold = currentDistance < threshold;
    boolean movingAway = currentDistance > previousDistanceToWaypoint;

    previousDistanceToWaypoint = currentDistance;

    return withinThreshold && movingAway;
}

/**
 * 動的しきい値の計算
 *
 * 高速飛行時は早めに次のウェイポイントへ切り替える必要がある
 * （旋回に時間がかかるため）
 *
 * 基準: 対地速度 × 5秒 の距離（約1-2NM）
 * 最小値: 0.5NM（低速時でも最低限の余裕を確保）
 * 最大値: 3.0NM（高速時でも過度に早い切り替えを防止）
 */
private double calculateDynamicThreshold() {
    double groundSpeedKnots = aircraftVector.groundSpeed.toDouble();
    double groundSpeedNmPerSec = groundSpeedKnots / 3600.0;
    double threshold = groundSpeedNmPerSec * 5.0; // 5秒分の距離

    return Math.max(0.5, Math.min(threshold, 3.0)); // 0.5NM ~ 3.0NM
}
```

---

### 3. 管制指示の優先度

#### 設計原則

**管制指示は常にフライトプランより優先される。**

これは実際の航空管制の原則に基づく：
- 管制官は安全確保のために航空機に指示を出す
- パイロットは管制指示に従う義務がある
- フライトプランはあくまで「計画」であり、管制指示により変更される

#### 指示と動作の対応

| 管制指示 | NavigationMode | 動作 |
|----------|---------------|------|
| ヘディング指示 | → HEADING | フライトプランから離脱、指定方位へ旋回 |
| 高度指示 | (変更なし) | 指示高度へ上昇/降下（モードに関係なく適用） |
| 速度指示 | (変更なし) | 指示速度へ加減速（モードに関係なく適用） |
| "Direct to [FIX]" | → DIRECT_TO | 指定Fixへ直行 |
| "Resume own navigation" | → FLIGHT_PLAN | フライトプラン再開（次のWPへ） |
| "Direct to [FIX], resume" | → DIRECT_TO | 指定Fix到達後、フライトプラン再開 |

---

### 4. SID/STAR・タワー移管の扱い

#### 設計方針

本シミュレーターはエンルート管制・ターミナルレーダー管制を対象としているため、SID/STARは以下のようにシンプルに扱う：

1. **SID（標準計器出発方式）**: フライトプランの最初のウェイポイント列として定義
2. **STAR（標準計器到着方式）**: フライトプランの最後のウェイポイント列として定義
3. **タワー移管**: 最終ウェイポイント通過で航空機を削除

#### WaypointAction 定義

```java
public enum WaypointAction {
    /**
     * 通常のウェイポイント
     * 通過後、次のウェイポイントへ
     */
    CONTINUE,

    /**
     * 航空機を削除（タワー移管を想定）
     * このウェイポイント通過後、シミュレーションから削除
     */
    REMOVE_AIRCRAFT,

    /**
     * 管制移管ポイント（将来の拡張用）
     * セクター間の移管を表現
     */
    HANDOFF
}
```

#### 運用例

```
羽田 → 伊丹 のフライトプラン例:

[SID部分]
RJTT (羽田) → SPENS → KAIHO →
[エンルート部分]
MAIKO → KOHWA → AWAJI →
[STAR部分]
MAYAH → IKOMA → RJOO (伊丹, action: REMOVE_AIRCRAFT)
```

---

## ドメインモデル設計

### クラス図

```
┌─────────────────────────────────────────────────────────┐
│                      FlightPlan                         │
├─────────────────────────────────────────────────────────┤
│ - callsign: Callsign                                    │
│ - departureAirport: String (ICAO)                       │
│ - arrivalAirport: String (ICAO)                         │
│ - waypoints: List<FlightPlanWaypoint>                   │
│ - cruiseAltitude: Altitude                              │
│ - cruiseSpeed: GroundSpeed                              │
├─────────────────────────────────────────────────────────┤
│ + getNextWaypoint(currentIndex): FlightPlanWaypoint     │
│ + getWaypointByName(name): Optional<FlightPlanWaypoint> │
│ + findWaypointIndex(name): int                          │
│ + isLastWaypoint(index): boolean                        │
└─────────────────────────────────────────────────────────┘
                           │
                           │ 1:N
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  FlightPlanWaypoint                     │
├─────────────────────────────────────────────────────────┤
│ - fixName: String                                       │
│ - position: FixPosition                                 │
│ - targetAltitude: Altitude (nullable)                   │
│ - targetSpeed: GroundSpeed (nullable)                   │
│ - altitudeConstraint: AltitudeConstraint                │
│ - action: WaypointAction                                │
├─────────────────────────────────────────────────────────┤
│ + hasAltitudeConstraint(): boolean                      │
│ + hasSpeedConstraint(): boolean                         │
│ + shouldRemoveAircraft(): boolean                       │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                  AltitudeConstraint                     │
├─────────────────────────────────────────────────────────┤
│ AT              // 指定高度ちょうど                      │
│ AT_OR_ABOVE     // 指定高度以上                         │
│ AT_OR_BELOW     // 指定高度以下                         │
│ BETWEEN         // 指定範囲内（将来拡張用）              │
│ NONE            // 制約なし                             │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                    WaypointAction                       │
├─────────────────────────────────────────────────────────┤
│ CONTINUE          // 次のウェイポイントへ継続           │
│ REMOVE_AIRCRAFT   // 航空機を削除                       │
│ HANDOFF           // 管制移管（将来拡張用）             │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   NavigationMode                        │
├─────────────────────────────────────────────────────────┤
│ FLIGHT_PLAN   // フライトプランに従う                   │
│ HEADING       // ヘディング指示（離脱状態）             │
│ DIRECT_TO     // 指定Fixへ直行                          │
└─────────────────────────────────────────────────────────┘
```

### AircraftBase への追加フィールド

```java
public abstract class AircraftBase implements Aircraft {
    // 既存フィールド
    protected final Callsign callsign;
    protected AircraftPosition aircraftPosition;
    protected AircraftVector aircraftVector;
    protected InstructedVector instructedVector;
    protected final AircraftType aircraftType;
    protected final FlightBehavior flightBehavior;
    protected final AircraftCharacteristics characteristics;

    // 新規追加フィールド
    protected FlightPlan flightPlan;                    // フライトプラン（nullable）
    protected int currentWaypointIndex;                 // 現在目指しているWPのインデックス
    protected NavigationMode navigationMode;            // ナビゲーションモード
    protected double previousDistanceToWaypoint;        // 通過判定用の前回距離
    protected FixPosition directToTarget;               // DIRECT_TO時の目標Fix
    protected boolean resumeFlightPlanAfterDirectTo;    // DIRECT_TO後にフライトプラン再開するか
}
```

---

## ファイル形式設計

### フライトプランJSON形式

```json
{
  "callsign": "JAL512",
  "aircraftType": "B738",
  "departure": {
    "icao": "RJTT",
    "runway": "34R"
  },
  "arrival": {
    "icao": "RJOO",
    "runway": "32L"
  },
  "cruiseAltitude": 35000,
  "cruiseSpeed": 450,
  "route": [
    {
      "fix": "SPENS",
      "altitude": 10000,
      "constraint": "AT_OR_ABOVE",
      "action": "CONTINUE"
    },
    {
      "fix": "KAIHO",
      "altitude": 24000,
      "constraint": "AT",
      "action": "CONTINUE"
    },
    {
      "fix": "MAIKO",
      "action": "CONTINUE"
    },
    {
      "fix": "KOHWA",
      "altitude": 35000,
      "constraint": "AT",
      "action": "CONTINUE"
    },
    {
      "fix": "AWAJI",
      "action": "CONTINUE"
    },
    {
      "fix": "MAYAH",
      "altitude": 12000,
      "constraint": "AT_OR_BELOW",
      "action": "CONTINUE"
    },
    {
      "fix": "IKOMA",
      "altitude": 6000,
      "constraint": "AT",
      "speed": 250,
      "action": "CONTINUE"
    },
    {
      "fix": "RJOO",
      "action": "REMOVE_AIRCRAFT"
    }
  ],
  "initialPosition": {
    "latitude": 35.5533,
    "longitude": 139.7811,
    "altitude": 3000,
    "heading": 270,
    "groundSpeed": 250,
    "verticalSpeed": 2000
  }
}
```

### シナリオファイル形式（複数航空機）

```json
{
  "scenarioName": "Tokyo Approach Training",
  "description": "羽田空港への到着機シナリオ",
  "createdAt": "2025-12-21",
  "aircraft": [
    {
      "spawnTime": 0,
      "flightPlan": { /* 上記のフライトプラン形式 */ }
    },
    {
      "spawnTime": 120,
      "flightPlan": { /* 別の航空機のフライトプラン */ }
    }
  ]
}
```

---

## API設計

### 新規エンドポイント

#### 1. フライトプラン付き航空機生成

```
POST /api/aircraft/spawn-with-flightplan
Content-Type: application/json

Request Body: FlightPlanファイルの内容

Response:
{
  "success": true,
  "callsign": "JAL512",
  "message": "Aircraft spawned with flight plan"
}
```

#### 2. 既存航空機へのフライトプラン割り当て

```
POST /api/aircraft/{callsign}/flightplan
Content-Type: application/json

Request Body: FlightPlanファイルの内容（initialPosition除く）

Response:
{
  "success": true,
  "callsign": "JAL512",
  "message": "Flight plan assigned"
}
```

#### 3. Direct To 指示

```
POST /api/aircraft/{callsign}/direct-to
Content-Type: application/json

Request Body:
{
  "fixName": "MAIKO",
  "resumeFlightPlan": true
}

Response:
{
  "success": true,
  "callsign": "JAL512",
  "targetFix": "MAIKO",
  "navigationMode": "DIRECT_TO"
}
```

#### 4. フライトプラン再開指示

```
POST /api/aircraft/{callsign}/resume-navigation

Response:
{
  "success": true,
  "callsign": "JAL512",
  "navigationMode": "FLIGHT_PLAN",
  "nextWaypoint": "KOHWA"
}
```

#### 5. 航空機のフライトプラン取得

```
GET /api/aircraft/{callsign}/flightplan

Response:
{
  "callsign": "JAL512",
  "navigationMode": "FLIGHT_PLAN",
  "currentWaypointIndex": 3,
  "currentWaypoint": "KOHWA",
  "remainingWaypoints": ["KOHWA", "AWAJI", "MAYAH", "IKOMA", "RJOO"],
  "flightPlan": { /* フライトプラン全体 */ }
}
```

#### 6. シナリオファイルのロード

```
POST /api/scenario/load
Content-Type: application/json

Request Body: シナリオファイルの内容

Response:
{
  "success": true,
  "scenarioName": "Tokyo Approach Training",
  "aircraftCount": 5,
  "message": "Scenario loaded successfully"
}
```

---

## 実装計画

### Phase 1: ドメインモデル実装（基盤）

**目的**: フライトプラン機能の基盤となるドメインモデルを実装

**実装内容**:
1. `NavigationMode` enum
2. `AltitudeConstraint` enum
3. `WaypointAction` enum
4. `FlightPlanWaypoint` クラス
5. `FlightPlan` クラス

**ファイル配置**:
```
Backend/src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/
└── domain/model/
    └── entity/
        └── flightplan/
            ├── FlightPlan.java
            ├── FlightPlanWaypoint.java
            ├── NavigationMode.java
            ├── AltitudeConstraint.java
            └── WaypointAction.java
```

### Phase 2: 航空機へのナビゲーション機能追加

**目的**: 航空機がフライトプランに従って飛行できるようにする

**実装内容**:
1. `AircraftBase` へのフィールド追加
2. ウェイポイント通過判定ロジック
3. 自動ナビゲーション（`calculateNextAircraftVector` の拡張）
4. 高度・速度制約の自動適用

**変更ファイル**:
- `AircraftBase.java`
- `CommercialAircraft.java`
- `Aircraft.java`（インターフェース）

### Phase 3: 管制指示の拡張

**目的**: 管制官がフライトプランに関連する指示を出せるようにする

**実装内容**:
1. `ScenarioService` への新規メソッド追加
   - `directToFix(callsign, fixName, resumeFlightPlan)`
   - `resumeNavigation(callsign)`
2. 既存の `instructAircraft` の拡張（ヘディング指示時にモード変更）

**変更ファイル**:
- `ScenarioService.java`
- `ScenarioServiceImpl.java`

### Phase 4: API・DTO実装

**目的**: フロントエンドからフライトプラン機能を利用可能にする

**実装内容**:
1. `FlightPlanDto` クラス
2. `CreateAircraftWithFlightPlanDto` クラス
3. REST APIエンドポイント実装
4. OpenAPI仕様の更新

**ファイル配置**:
```
Backend/src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/
└── interfaces/
    ├── dto/
    │   ├── FlightPlanDto.java
    │   ├── FlightPlanWaypointDto.java
    │   └── CreateAircraftWithFlightPlanDto.java
    └── api/
        └── FlightPlanService.java
```

### Phase 5: フロントエンド対応

**目的**: UIからフライトプラン機能を操作可能にする

**実装内容**:
1. フライトプラン表示コンポーネント
2. Direct To指示UI
3. Resume Navigation指示UI
4. シナリオファイルアップロード機能

### Phase 6: テスト・ドキュメント

**目的**: 品質保証とドキュメント整備

**実装内容**:
1. 単体テスト（ドメインモデル）
2. 統合テスト（API）
3. サンプルシナリオファイル作成
4. ユーザーマニュアル更新

---

## 将来の拡張可能性

本設計は以下の将来拡張を考慮している：

1. **ホールディングパターン**: `WaypointAction.HOLD` の追加
2. **セクター間移管**: `WaypointAction.HANDOFF` の実装
3. **RNAV/RNP経路**: 曲線経路のサポート
4. **燃料計算**: フライトプランに基づく燃料消費シミュレーション
5. **複数セクター同時シミュレーション**: 管制移管の完全実装

---

## 参考資料

- ICAO Doc 4444: 航空交通管理手順
- JCAB AIP Japan: 日本の航空路誌
- 既存のHorusコードベース（特に `AircraftBase.java`, `ScenarioServiceImpl.java`）

---

## 変更履歴

| 日付 | バージョン | 変更内容 | 作成者 |
|------|-----------|----------|--------|
| 2025-12-21 | 1.0 | 初版作成 | - |
