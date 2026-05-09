# Phase 5 — シミュレーション速度変更（5-1）

## メタデータ

- **Status**: Draft
- **Date**: 2026-05-09
- **関連ロードマップ**: [spec/spec.md](../spec.md) フェーズ 5-1、Issue [#64](https://github.com/Futty93/Horus/issues/64)

## 概要

バックエンドのシミュレーション刻み（**実時間**＝現実世界で進む時間に対して、シミュレーション内時間がどれだけ進むか）を変更できるようにする。訓練・デモで「早送り」「スロー」を切り替えられるようにし、現状固定の 1Hz ティックと物理積分の前提を、**実行時に安全に切り替え可能**な形へ拡張する。

## 背景・課題

### 現状

- `AtcSimulatorConstants.REFRESH_RATE`（既定 `1`）と `TICK_INTERVAL_MS`（`1000 / REFRESH_RATE`）が **コンパイル時定数**。
- `AtcSimulatorDomainConfig.SimulationScheduler` が `@Scheduled(fixedRate = TICK_INTERVAL_MS)` で `airspaceManagement.nextStep()` を駆動。Spring の `fixedRate` は Bean 定義時に固定され、**実行中の変更に不向き**。
- `AircraftBase` / `CommercialAircraft` / `FixedWingFlightBehavior` 等が `REFRESH_RATE`（またはそれに相当する値）を **シミュレーション1ステップあたりの経過時間（秒）** として位置・垂直速度などに利用している。
- `GlobalVariables` には `isSimulationRunning` のみ。速度・刻みのランタイム状態がない。
- Frontend の航空機位置ポーリング間隔（`GLOBAL_CONSTANTS.LOCATION_UPDATE_INTERVAL` 等）はバックエンド刻みと**独立**しており、早送り時に UI が追従しすぎ／遅れすぎしないよう別途整理の余地がある。

### 課題（Problem Statement）

- 長時間シナリオの訓練・検証で、**実時間に対する進行速度**を変えられず、待ち時間が冗長。
- 速度を変える場合、**実時間ベースのスケジューラ間隔**と**1ティックあたりのシミュレ時間（dt）** の整合を誤ると、機体移動・旋回・高度変化が非物理的になるか、コンフリクト検出の時間解像度と齟齬が出る。

### なぜ今か（Motivation）

`spec/spec.md` で 5-1 が 🔴 かつ `REFRESH_RATE` 動的化が明記されている。ホールディング・コンフリクト UI など操作・安全表示の土台が揃いつつあるため、**訓練体験**を伸ばす次の自然なステップとして優先度が高い。

---

## 方針

### 決定方針（Decision）

1. **第1版のユーザー向け意味**は **時間倍率（multiplier）** とする。設定は **離散プリセットのみ**（任意の実数は受け付けない）。API / UI とも次のいずれかだけを許可する: **`0.25` / `0.5` / `1` / `2` / `4` / `10`**。  
   - `1` = 現状相当（**実時間で 1 秒**経つ間に、シミュレーション時間が約 1 秒進む）。
   - `2` / `4` / `10` = 早送り、`0.25` / `0.5` = スロー。  
   - 上限 **`10`** については下記「負荷見積り」を参照（機数・CPU によっては `4` 以下が無難な場合あり）。

2. **1 ティックあたりのシミュレーション経過時間（`simDeltaSeconds`）** は第1版では **固定 1.0 秒**を維持する。  
   - 早送り・スローは **実時間でのティック間隔**（`tickIntervalWallMs = 1000 / multiplier`、近似的）で表現する。  
   - 物理・フライト計算は毎ティック「1 シミュ秒分」進むため、倍率変更で **積分ステップの物理意味がブレない**。

3. **スケジューラ**は `@Scheduled(fixedRate=…)` の単一定数に依存せず、`TaskScheduler` / `ScheduledExecutorService` 等で **再スケジュール可能**な形にする（速度変更時に既存スケジュールをキャンセルし、新間隔で登録し直す）。

4. **ランタイム状態**は `GlobalVariables` への追加、または `@Component` の `SimulationTiming`（`multiplier`, `tickIntervalWallMs` の getter）など **単一の参照元**に集約する。定数 `REFRESH_RATE` は **既定値・フォールバック**として残し、動的値は「毎ティック `nextStep` に渡す」または「スレッドセーフな supplier」で供給する。

5. **API（Backend）**  
   - `GET /simulation/speed`（または `/simulation/timing`）: 現在の倍率・（任意で）ティック間隔 ms を返す DTO。  
   - `PUT /simulation/speed` または `POST /simulation/speed`: リクエストで **上記プリセットのいずれか**のみ受け付ける。それ以外は 400。  
   - 既存 `GET /simulation/status` に `speedMultiplier` を含めてもよい（クライアント1回取得で足りるなら）。

6. **OpenAPI**（`Backend/UranosAPI.yml`）と Frontend BFF（`Frontend/app/api/...`）を **同じ PR または直近 PR** で追随する（プロジェクト方針）。

7. **Frontend（Should / 第2スライス可）**  
   - Controller 等に **離散プリセット**（上記 6 段階）のボタンまたはセレクトのみ。スライダー・自由入力は第1版では採用しない。  
   - 位置ポーリング間隔を極端に速くしすぎないよう、上限・スロットリングを検討する（10x 時は BFF/Backend への GET 頻度に注意）。

### 検討した他案（Alternatives Considered）

- **案 A: REFRESH_RATE（Hz）だけを上げ、dt = 1/Hz にする**  
  **実時間に対する**シミュレ時間の「早送り」にはならず、**解像度が上がるだけ**。訓練用途の主訴に合わないため、第1版の主手段とはしない。将来、細かい軌道表現用オプションとして再検討可。

- **案 B: simDeltaSeconds も倍率と連動して変える**  
  ティック数は減らせるが、1 ティックの変位が大きくなり旋回・高度の見た目が悪化しやすい。第1版は **dt 固定 + 間隔変更**を採用。

- **案 C: application.yml のみで起動時固定**  
  運用・デモで都度切り替えできないため却下。

### トレードオフ（Trade-offs）

- **メリット**: 訓練・検証の待ち時間削減、デモのテンポ調整、既存シナリオを変えずに再現速度だけ変えられる。  
- **デメリット / 受容する制約**: スケジューラ再設定の競合（停止中・変更中のティック）を防ぐため、短い同期または「次ティックから適用」の規約が必要。極端な倍率では CPU・コンフリクト計算負荷が上がるため **離散プリセットと上限**で抑える。

### 負荷見積りと上限（なぜ 10 倍までか）

第1版のモデルでは **1 ティックあたりのシミュレーション経過は 1 秒固定**とし、倍率 `m` のとき **実時間でのティック間隔**をおおよそ `1000 / m` ms にする（`m = 10` → **約 100ms に 1 回** `nextStep`）。

- **1 ティックの仕事量**は現状 1Hz 運用時の 1 ステップと同種（全機更新、コンフリクト評価など）。機数が同じなら **CPU 時間はおおよそ同オーダー**。
- **制約**は「**1 ティックの処理が、次のティック予定時刻より前に終わること**」。間に合わないと遅延が積み上がり、実時間に対する早送り比率が崩れる。
- `Backend/README.md` の目安では、コンフリクト系は **200 機で通常 100ms 未満**。`m = 10` のとき許容ウィンドウは約 100ms のため、**機数が多く最悪に近い負荷のときはギリギリ**になり得る。一方、**数十機程度の訓練シナリオ**では処理時間に余裕があり、**10 倍は十分現実的**と判断できる。
- **結論（第1版）**: プリセットの最大を **`10` とする**（ユーザ想定どおり採用可）。README または本 spec に **「高機数・低速端末では 4 以下推奨」**の一文を残し、負荷テスト後に上限を下げるかはデータで再検討する。
- **フロントのポーリング**はティックと独立のため、10x 時だけ HTTP が増えすぎないよう **間隔の上限**を別途決める（実装 Phase 2）。

---

## 完了条件（Success Criteria）

- [x] 実行中・停止中を問わず API から **時間倍率**を設定でき、設定値が **単一のランタイム状態**から読み取れる。
- [x] 倍率変更後、**実時間でのティック間隔**が倍率に応じて変化し、`isSimulationRunning == true` のときシミュレーション経過が早送り／スローになる（`SimulationSpeedIntegrationTest` で確認）。
- [x] 1 ティックあたりの **シミュレーション経過秒**が仕様どおり一貫しており、位置・垂直速度などの計算が **定数 REFRESH_RATE 直読みに依存しない**（または依存箇所が明示的に置換されている）。
- [x] プリセット以外の倍率は **400** 等で拒否される。
- [x] `Backend` の既存テスト（統合テスト含む）が通り、速度変更に関する **最低1件**のテスト（API またはスケジューラ契約）が追加されている。
- [x] `UranosAPI.yml` が新エンドポイント／DTO と整合している。
- [x] `npm run lint` / `npm test` / `npm run build`（Frontend）が通る（BFF のみ追加。UI プリセットは任意）。

---

## 影響範囲

| 領域 | 内容 |
|------|------|
| `AtcSimulatorConstants` | 既定値・定数の意味の文書化；動的値との役割分担 |
| `AtcSimulatorDomainConfig` / `SimulationScheduler` | `@Scheduled` からの移行、再スケジュール |
| `GlobalVariables` または新コンポーネント | 倍率・（任意）最終適用時刻 |
| `AirspaceManagementImpl` / `AircraftRepository` / `AircraftBase` 連鎖 | `nextStep` 内で参照する dt / Hz の供給 |
| `CommercialAircraft` / `FlightBehavior` / `PositionUtils` | `refreshRate` 引数の意味と呼び出し元の整合 |
| `SimulationService`（`interfaces/api`） | 新 API、ステータス拡張 |
| `UranosAPI.yml` | スキーマ |
| Frontend | BFF ルート、（任意）倍率 UI、ポーリング間隔 |

---

## 実装計画

### Phase 1（Must）

1. **SimulationTiming**（仮名）で倍率・`tickIntervalWallMs` を保持。スレッドセーフ方針を決める。
2. **ティック実行**: シミュレーション停止中はスケジュールを止める、または現状どおり no-op でよいが、**倍率変更時の再スケジュール**を実装。
3. **ドメイン**: `nextStep` から参照する「1 ティックの sim 秒」を Timing から取得し、`AircraftBase` / `CommercialAircraft` 等の静的 `REFRESH_RATE` 参照を置換。
4. **API + DTO + バリデーション** + **統合テスト 1 本以上**。
5. **OpenAPI 更新**。

### Phase 2（Should）

- Frontend: BFF + 簡易 UI（プリセットボタン）。
- 位置ポーリングと倍率の組み合わせメモを `Frontend/README.md` に追記。

---

## 検証

- [x] `./gradlew test`（Backend）が通る
- [ ] 手動: 1x / 2x で同シナリオの経過シミュレ時間がおおよそ 2 倍差になることを確認
- [ ] 手動: 倍率変更直後に異常なティック連発や二重実行がないことを確認

---

## 未解決事項（Unresolved Questions）

- **コンフリクト検出**の内部時間解像度がティックに完全追従するか、別タイマーが必要か（現状は `nextStep` 連動なら一致）。
- ポーズ／再開と倍率変更の **同時操作**の優先順位。
- **10 倍**を大規模シナリオで回したときの実測（最大機数・p95 ティック時間）に基づき、上限を `8` に下げるかどうか。

---

## 関連ドキュメント

- [spec/spec.md](../spec.md)
- [Backend/README.md](../../Backend/README.md)
- [Frontend/README.md](../../Frontend/README.md)
