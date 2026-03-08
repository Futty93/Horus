# ATS 経路提案 API — 空港間ルートの A* 計算

## メタデータ

- **Status**: Completed
- **Date**: 2026-03-08
- **Updated**: 2026-03-08 — Phase 1–3 完了、フロント連携・GET /ats/airports 追加

## 概要

フライトプラン設定画面で O/D（出発・到着空港）を指定した際に、**デフォルトルートをバックエンドで計算して返す API** を新設する。既存の ATS 経路データ（atsLowerRoutes / rnavRoutes）を「fix 同士を線で結んだ道」とみなし、空港の緯度経度から最も近い fix を起点・終点として **A* アルゴリズム**で経路を導出する。フロントエンドには経路データを保存せず、都度バックエンドから取得する。

## 背景・課題

### 現状

- **ATS 経路データ**（バックエンド）:
  - `waypoints.json`, `radio_navigation_aids.json`: fix 名と緯度経度
  - `ats_lower_routes.json`, `rnav_routes.json`: 各 Route は `points[]` の順序で fix を繋いだ経路
- **フロントエンド**: 上記データを `/api/ats/route/all` 経由で取得し、fix 同士を線で描画
- **デフォルトルート**: Haneda テンプレートでは全機に `KOITO → BOKJO → AOIKU` をハードコード（O/D 非依存）
- **Airport 型**: `Airport.java` に `icaoCode`, `latitude`, `longitude` 等が定義済み。ただし `airports.json` や読み込み処理は未実装

### 課題（Problem Statement）

- フロントエンドにルート情報を保持するのは責務として不適切
- O/D ごとの適切なデフォルトルートを生成する手段がない
- 既存の ATS 経路（fix を線で結んだグラフ）はバックエンドにあり、これを活用していない

### なぜ今か（Motivation）

フライトプラン設定ページの O/D グループで「ルートを一括定義」する際、ユーザーが手動で ATS ルートを検索・選択する手間を減らし、空港指定だけで候補ルートを得られるようにする。

---

## 方針

### 決定方針（Decision）

1. **グラフ構築**: atsLowerRoutes + rnavRoutes から、隣接する fix 対を「辺」とする**無向グラフ**を構築する
2. **空港データ**: `airports.json` を新規作成し、主要空港（ICAO, 緯度, 経度）を定義する
3. **最近傍 fix**: 出発空港・到着空港それぞれについて、緯度経度が最も近い fix を算出する
4. **A* 経路検索**: 起点 fix から終点 fix まで A* で最短経路を探索し、fix 名の配列を返す
5. **API**: `GET /api/ats/route/suggest?origin=RJBB&destination=RJAA` を新設

### グラフの定義

| 要素 | 定義 |
|------|------|
| **ノード** | waypoints + radioNavigationAids に存在する fix 名（緯度経度は waypoints / radioNavAids から取得） |
| **辺** | 各 Route の `points[i]` と `points[i+1]` が隣接関係にある fix 対。同一 fix 対は 1 本にまとめる |

### A* のコスト設計

| 項目 | 案 |
|------|-----|
| **移動コスト g(n)** | 2 点間の緯度経度による実距離（Haversine）またはユークリッド近似 |
| **ヒューリスティック h(n)** | ゴールまでの直線距離（許容的） |

### 検討した他案（Alternatives Considered）

- **案 A（フロントでハードコード）**: O/D ごとにルートを静的に定義。採用しなかった理由: フロントにデータを保持せず、拡張性が低い
- **案 B（既存 ATS ルートの名前マッチ）**: description の "from X to Y" で O/D 相当を推測。採用しなかった理由: 空港 ICAO と fix 名の対応が曖昧で精度が低い

### トレードオフ（Trade-offs）

- **メリット**: 既存データを活用、フロントは API 呼び出しのみ、経路変更時はバックエンドの JSON 更新で対応可能
- **デメリット**: 空港データの新規作成が必要、経路が存在しない O/D では空の応答になる可能性

---

## データ仕様

### airports.json（新規）

```json
{
  "airports": [
    {
      "icaoCode": "RJTT",
      "name": "Tokyo Haneda",
      "iataCode": "HND",
      "latitude": 35.5494,
      "longitude": 139.7798
    },
    {
      "icaoCode": "RJAA",
      "name": "Narita",
      "iataCode": "NRT",
      "latitude": 35.7720,
      "longitude": 140.3929
    },
    {
      "icaoCode": "RJBB",
      "name": "Kansai",
      "iataCode": "KIX",
      "latitude": 34.4347,
      "longitude": 135.2441
    },
    {
      "icaoCode": "RJOO",
      "name": "Osaka Itami",
      "iataCode": "ITM",
      "latitude": 34.7855,
      "longitude": 135.4382
    }
  ]
}
```

- **初期範囲**: Haneda テンプレートで使用する空港（RJTT, RJAA, RJBB, RJOO, RJFK, RJSM, RJOM, RJSA, RJFF, RJGG, RJFM, RJOA, RJBE, ROAH, RJEC, RORA, ROMD, RJSN 等）を段階的に追加（粟国空港は RORA）

### API レスポンス

**成功時（200）**

```json
{
  "waypoints": ["KOITO", "BOKJO", "AOIKU", "UNAGI"]
}
```

**経路なし / 空港不明（200・空配列）**

```json
{
  "waypoints": [],
  "reason": "NO_AIRPORT"
}
```

`reason`: `NO_AIRPORT` | `NO_NEAREST_FIX` | `REJECT`（空配列時のみ付与）。グラフ非連結時は `[startFix, goalFix]` の 2 点を返す（FALLBACK）。

**パラメータ不正（400）**

- `origin` または `destination` が欠落・空文字の場合

---

## 完了条件（Success Criteria）

- [ ] `GET /api/ats/route/suggest?origin=RJTT&destination=RJAA` が fix 配列を返す（経路が存在する O/D の場合）
- [ ] `airports.json` が読み込まれ、指定 ICAO の空港が検索できる
- [ ] 経路が存在しない、または空港が未知の場合は空配列を返す
- [ ] フロントエンドはこの API を呼び出してデフォルトルート候補を取得できる（呼び出し実装は別 spec または本 spec の Phase 2）

---

## 影響範囲

| 対象 | 変更内容 |
|------|----------|
| **Backend** | `airports.json` 新規、空港読み込み、A* 経路検索ロジック、`AtsRouteService` に新エンドポイント追加 |
| **Frontend** | `/api/ats/route/suggest` の BFF プロキシ、フロントは既存 `loadAtsRoutes` とは別に本 API を呼ぶ（O/D グループのデフォルトルート取得時） |
| **既存 API** | `/api/ats/route/all` は変更なし |

---

## 実装計画

### Phase 1: データとインフラ（Must-have）

1. `fix/airports.json` を新規作成（RJTT, RJAA, RJBB, RJOO 等、Haneda テンプレートで必要な空港）
2. `AirportRepository` または `AtsRouteFixPositionRepository` の拡張で空港データを読み込む
3. waypoints + radioNavAids + atsLowerRoutes + rnavRoutes から「fix グラフ」を構築するユーティリティ

### Phase 2: A* と API（Must-have）

4. 緯度経度から最近傍 fix を求めるメソッド
5. A* アルゴリズムで起点 fix → 終点 fix の経路を計算
6. `AtsRouteService` に `GET /ats/route/suggest?origin=&destination=` を追加
7. Frontend BFF: `app/api/ats/route/suggest/route.ts` を追加

### Phase 3: フロント連携（Should-have）— **完了**

8. フロントエンドの O/D グループで「Suggest route」ボタン + 「Load & Suggest Routes」を実装
9. 取得した waypoints をルート入力欄に反映し、自動 Apply
10. `GET /ats/airports` で空港座標を取得、Route Preview の Origin/Dest 表示に使用

### Phase 4: テスト・最適化（Could-have）

10. ユニットテスト（グラフ構築、A*、最近傍）— **完了**: `RouteSuggestionServiceTest` に簡易グラフで Mock したテストを追加
11. 代表的な O/D での統合テスト — **完了**: `BackendRedesignIntegrationTest` に API テスト追加

---

## 検証

- [ ] Backend のビルドが通る（`./gradlew build`）
- [ ] `GET /ats/route/suggest?origin=RJTT&destination=RJAA` が期待通りの JSON を返す
- [ ] 未知の ICAO を指定した場合に空配列が返る
- [ ] Frontend のビルドが通る（BFF 追加後）

---

## 未解決事項（Unresolved Questions）

- 空港と fix が地理的に離れている場合の「最近傍」の許容距離（例: 500 km 以上は無効とするか）
- 複数経路が存在する場合の選定基準（最短のみでよいか、代替経路も返すか）
- Runway 等の `Airport` 既存フィールドを airports.json に含めるか、経路計算では icao + lat/lon のみで十分か

---

## 関連ドキュメント

- [spec/20260308-flight-plan-setup-page](../20260308-flight-plan-setup-page/spec.md)
- [Backend: AtsRouteFixPositionRepository](../../Backend/src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/infrastructure/fix/AtsRouteFixPositionRepository.java)
- [Backend: Airport 型](../../Backend/src/main/java/jp/ac/tohoku/qse/takahashi/AtcSimulator/domain/model/entity/airport/Airport.java)
