# Swagger テスト用サンプルデータ

バックエンド起動後、`http://localhost:8080/docs.html` の Swagger UI で各 API を「Try it out」でテストする際に使用できるサンプルです。

**詳細なマニュアルテスト手順**: [MANUAL_TEST.md](./MANUAL_TEST.md) を参照してください。

## 使い方

1. バックエンドを起動: `./gradlew bootRun`（Backend ディレクトリで）
2. ブラウザで `http://localhost:8080/docs.html` を開く
3. **シミュレーション開始**: `POST /simulation/start` を実行
4. 対象 API の「Try it out」をクリック
5. 以下の JSON を Request body にコピー＆ペースト（該当する場合）
6. 「Execute」をクリック

## 推奨テスト順序

### パターン A: 1 機ずつ spawn する場合

1. `POST /simulation/start` — シミュレーション開始
2. `POST /api/aircraft/spawn-with-flightplan` — `spawn-with-flightplan-sample.json` または `spawn-with-flightplan-minimal.json` を使用
3. `GET /api/aircraft/{callsign}/flightplan` — フライトプラン状態確認
4. `POST /api/aircraft/{callsign}/direct-to` — `direct-to-sample.json` を使用（Direct To テスト）
5. `POST /api/aircraft/{callsign}/resume-navigation` — フライトプラン再開

### パターン B: シナリオ一括ロードから始める場合

1. `POST /api/scenario/load` — Request body に **`scenario-load-minimal.json`**（1 機）をコピペ。空域がクリアされ、機体がスポーンする。**シミュレーションは自動開始されない**（`GET /simulation/status` で `isSimulationRunning: false` を確認可）
2. `POST /simulation/start` — シミュレーション開始
3. 以降は `GET /api/aircraft/{callsign}/flightplan` など（上記パターン A の 3 以降）

## サンプルファイル一覧

| ファイル | 用途 |
|----------|------|
| scenario-load-minimal.json | `POST /api/scenario/load` 用。1 機・空の route（Swagger / 手動の最小例）。コールサイン `SCLOAD01` |
| spawn-with-flightplan-sample.json | T09 セクター（伊豆・駿河湾付近）。KOITO→BOKJO→AOIKU。spawn は KOITO 東約 5km |
| spawn-with-flightplan-minimal.json | T09 最短テスト（KOITO→UNAGI）。spawn は KOITO 東約 5km、約 1 分で KOITO 通過 |
| assign-flightplan-sample.json | `POST /api/aircraft/{callsign}/flightplan` 用。Body の callsign は path と一致させる（サーバが path を優先） |
| direct-to-sample.json | Direct To 指示（fixName: UNAGI, resumeFlightPlan: true） |

## 注意

- route 内の fix は waypoints.json に存在する名称を使用すること
- T09 デフォルトセクター付近: KOITO, UNAGI, BOKJO, AOIKU 等
