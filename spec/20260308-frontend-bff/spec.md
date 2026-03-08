# フロントエンド BFF（Backend for Frontend）導入 実装計画

## 概要

Next.js を BFF として導入し、クライアントが Java バックエンドに直接アクセスする現状の構成を、Next.js API Routes 経由のプロキシ構成に変更する。

## 背景・課題

### 現状のアーキテクチャ

```
[Browser] ──fetch()──> [Java Backend :8080]
     │
     └──> [Next.js :3333]  (HTML/JS/CSS 配信のみ、API Routes なし)
```

- フロントエンド（Next.js）は HTML/JS の配信のみ
- ブラウザが `NEXT_PUBLIC_SERVER_IP:PORT`（例: localhost:8080）へ直接 `fetch` で API 呼び出し
- `app/api/` および `pages/api/` は未定義

### 課題

| 課題 | 影響 |
|------|------|
| CORS の考慮 | Java バックエンドで CORS を正しく設定する必要がある |
| バックエンド URL の露出 | クライアントに Java の URL が渡る |
| API 呼び出しの分散 | 各コンポーネント・ユーティリティで `serverIp`/`serverPort` を個別に参照 |
| 認証・共通処理の欠如 | 認証ヘッダ、エラーハンドリング、リトライなどを集約するレイヤーがない |

---

## 方針

**Next.js API Routes を BFF として導入**し、クライアントは同一オリジン（`/api/*`）のみを呼び出す構成に変更する。

### 目標アーキテクチャ

```
[Browser] ──fetch('/api/...')──> [Next.js :3333]
                                      │
                                      └──proxy──> [Java Backend :8080]
```

- クライアントは `http://localhost:3333/api/...` を呼び出す（同一オリジン）
- Next.js Route Handler が Java バックエンドへプロキシ
- `NEXT_PUBLIC_*` のバックエンド URL はクライアントから不要になる（サーバー側の `SERVER_IP`/`SERVER_PORT` のみ使用）

---

## 現行 API 一覧（Java バックエンド）

| エンドポイント | メソッド | 呼び出し元 | 用途 |
|----------------|----------|------------|------|
| `/aircraft/location/all` | GET | `location.ts`（`fetchAircraftLocation`） | 航空機位置一覧取得 |
| `/api/aircraft/control/{callsign}` | POST | `controlAircraft.ts`（`controlAircraft.tsx`, `inputInfoArea.tsx` が利用） | 航空機制御指示 |
| `/api/aircraft/{callsign}/flightplan` | GET | `flightPlan.ts`（`fetchFlightPlan`） | フライトプラン取得 |
| `/api/aircraft/{callsign}/direct-to` | POST | `flightPlan.ts`（`directToFix`）, `selectFixMode.tsx`（直接 fetch） | 直行指示 |
| `/api/aircraft/{callsign}/resume-navigation` | POST | `flightPlan.ts`（`resumeNavigation`、`flightPlanControl.tsx` が利用） | ナビゲーション再開 |
| `/simulation/start` | POST | `simulationControlButtons.tsx`（直接 fetch） | シミュレーション開始 |
| `/simulation/pause` | POST | `simulationControlButtons.tsx`（直接 fetch） | シミュレーション一時停止 |
| `/ats/route/all` | GET | `atsRoutesLoader.ts`（`loadAtsRoutes`） | ATS 経路データ取得 |

※ `simulation.ts` の `SimulationManager` は DOM ベースの legacy 実装で未使用。`simulationControlButtons.tsx` のみがシミュレーション API を呼ぶ。

---

## TDD アプローチ

テスト駆動開発（TDD）に従い、**Red → Green → Refactor** のサイクルで実装を進める。

### 基本方針

1. **テストファースト**: 失敗するテストを先に書き、その後に実装する
2. **小さなステップ**: 1 エンドポイント単位でプロキシ → クライアント変更を完結させる
3. **二重化を避ける**: プロキシは「Java への透過的転送」であるため、E2E または統合テストで検証し、単体テストは必要最小限に

### テスト戦略

| 対象 | テスト種別 | 方針 |
|------|------------|------|
| `backendProxy.ts` | 単体 | `getBackendBaseUrl` の環境変数反映、`proxyToBackend` の URL 組み立てを検証。`fetch` はモック |
| Route Handler 各ファイル | 単体 | リクエストを `proxyToBackend` に正しいパスで渡すことを検証。`proxyToBackend` はモック |
| API クライアント（location, flightPlan, controlAircraft 等） | 既存テスト | 相対 URL `/api/...` を呼ぶように変更後、モック `fetch` で URL が正しいことを検証 |
| 動作確認 | 手動 | Java バックエンド起動下で Operator/Controller 画面の一連操作を確認 |

### 実装順序（TDD サイクル）

1. **backendProxy.ts** … テスト作成 → 実装 → 環境変数・URL 検証
2. **location** … Route Handler テスト → 実装 → location.ts 変更 → 既存テスト修正
3. **control** … 同様
4. **flightplan / direct-to / resume-navigation** … 同様
5. **simulation** … 同様
6. **ats/route** … 同様
7. **selectFixMode.tsx** … directToFix への差し替え、既存テストで検証
8. **統合** … 手動動作確認、ビルド・全テスト通過

---

## 実装計画

### Phase 1: API プロキシの導入

#### 1.1 環境変数の整理

| 変数 | スコープ | 用途 |
|------|----------|------|
| `BACKEND_SERVER_IP` | サーバー側のみ | Java バックエンドのホスト（`.env.local` で `localhost` 等） |
| `BACKEND_SERVER_PORT` | サーバー側のみ | Java バックエンドのポート（`8080`） |
| `NEXT_PUBLIC_SERVER_IP` | 廃止予定 | Phase 1 完了後は不要 |
| `NEXT_PUBLIC_SERVER_PORT` | 廃止予定 | Phase 1 完了後は不要 |

※ クライアントは同一オリジンのため、`/api/*` 呼び出しに URL 指定不要。

#### 1.2 Next.js API Routes 構成

**新規**: `Frontend/app/api/` 配下に Route Handler を配置

```
app/
├── api/
│   ├── aircraft/
│   │   ├── control/
│   │   │   └── [callsign]/
│   │   │       └── route.ts     # POST → /api/aircraft/control/{callsign}
│   │   ├── location/
│   │   │   └── all/
│   │   │       └── route.ts     # GET → /aircraft/location/all
│   │   └── [callsign]/
│   │       ├── flightplan/
│   │       │   └── route.ts     # GET → /api/aircraft/{callsign}/flightplan
│   │       ├── direct-to/
│   │       │   └── route.ts     # POST → /api/aircraft/{callsign}/direct-to
│   │       └── resume-navigation/
│   │           └── route.ts    # POST → /api/aircraft/{callsign}/resume-navigation
│   ├── simulation/
│   │   └── [action]/
│   │       └── route.ts         # POST → /simulation/{start|pause}（action を start|pause に制限）
│   └── ats/
│       └── route/
│           └── all/
│               └── route.ts     # GET → /ats/route/all
```

各 Route Handler はリクエストを Java バックエンドへ転送し、レスポンスをそのまま返す（パス・ボディ・ヘッダを適宜マッピング）。

#### 1.2.1 BFF パス ↔ Java バックエンド パスマッピング

| BFF（クライアントが呼ぶパス） | Java バックエンド（プロキシ先） |
|------------------------------|----------------------------------|
| `GET /api/aircraft/location/all` | `GET /aircraft/location/all` |
| `POST /api/aircraft/control/[callsign]` | `POST /api/aircraft/control/{callsign}` |
| `GET /api/aircraft/[callsign]/flightplan` | `GET /api/aircraft/{callsign}/flightplan` |
| `POST /api/aircraft/[callsign]/direct-to` | `POST /api/aircraft/{callsign}/direct-to` |
| `POST /api/aircraft/[callsign]/resume-navigation` | `POST /api/aircraft/{callsign}/resume-navigation` |
| `POST /api/simulation/start` | `POST /simulation/start` |
| `POST /api/simulation/pause` | `POST /simulation/pause` |
| `GET /api/ats/route/all` | `GET /ats/route/all` |

#### 1.3 プロキシ実装の共通パターン

**新規**: `Frontend/utility/api/backendProxy.ts`（サーバー専用）

```typescript
// サーバー側のみで使用。BACKEND_SERVER_IP / BACKEND_SERVER_PORT から URL を構築
export function getBackendBaseUrl(): string {
  const ip = process.env.BACKEND_SERVER_IP ?? "localhost";
  const port = process.env.BACKEND_SERVER_PORT ?? "8080";
  return `http://${ip}:${port}`;
}

export async function proxyToBackend(
  path: string,
  init?: RequestInit
): Promise<Response> {
  const base = getBackendBaseUrl();
  const url = `${base}${path}`;
  return fetch(url, init);
}
```

- `path` は Java バックエンドのパス（例: `/aircraft/location/all`）。先頭の `/` を含む
- クライアントから渡された `Request` の body / headers を必要な範囲で転送
- Java のレスポンスをそのまま返す（status, body, headers）

#### 1.4 フロントエンド API クライアントの変更

| ファイル | 変更内容 |
|----------|----------|
| `utility/api/location.ts` | `fetch('/api/aircraft/location/all', ...)` に変更（相対 URL） |
| `utility/api/flightPlan.ts` | `baseUrl()` を削除し `/api/aircraft/${callsign}/...` を使用 |
| `utility/api/controlAircraft.ts` | `fetch('/api/aircraft/control/${callsign}', ...)` に変更。`controlAircraft.tsx`・`inputInfoArea.tsx` は本 utility を利用するため変更不要 |
| `utility/AtsRouteManager/atsRoutesLoader.ts` | `fetch('/api/ats/route/all', ...)` に変更 |
| `components/selectFixMode.tsx` | 直接 fetch をやめ、`flightPlan.ts` の `directToFix` に統一 |
| `components/simulationControlButtons.tsx` | インライン fetch を `fetch('/api/simulation/${action}', ...)` に変更 |

- すべての API 呼び出しで **相対 URL**（`/api/...`）を使用
- `NEXT_PUBLIC_SERVER_IP` / `NEXT_PUBLIC_SERVER_PORT` の参照を全ファイルから削除

---

### Phase 2（将来）: 共通 API クライアントの整理

- `utility/api/client.ts` を新規作成
- `baseUrl = ''`（同一オリジン）で `fetch` をラップ
- エラーハンドリング、リトライ、ロギングを共通化

---

### Phase 3（将来）: 認証・拡張

- BFF で認証トークンの付与・検証
- レートリミット、リクエストログの集約
- 必要に応じて WebSocket のプロキシ検討

---

## ファイル変更一覧（Phase 1）

| # | ファイル | 種別 |
|---|----------|------|
| 1 | `app/api/aircraft/location/all/route.ts` | 新規 |
| 2 | `app/api/aircraft/control/[callsign]/route.ts` | 新規 |
| 3 | `app/api/aircraft/[callsign]/flightplan/route.ts` | 新規 |
| 4 | `app/api/aircraft/[callsign]/direct-to/route.ts` | 新規 |
| 5 | `app/api/aircraft/[callsign]/resume-navigation/route.ts` | 新規 |
| 6 | `app/api/simulation/[action]/route.ts` | 新規 |
| 7 | `app/api/ats/route/all/route.ts` | 新規 |
| 8 | `utility/api/backendProxy.ts` | 新規（サーバー専用） |
| 9 | `utility/api/location.ts` | 変更 |
| 10 | `utility/api/flightPlan.ts` | 変更 |
| 11 | `utility/api/controlAircraft.ts` | 変更 |
| 12 | `utility/AtsRouteManager/atsRoutesLoader.ts` | 変更 |
| 13 | `components/selectFixMode.tsx` | 変更（`directToFix` 使用に統一） |
| 14 | `components/simulationControlButtons.tsx` | 変更 |
| 15 | `.env.sample` | `BACKEND_SERVER_IP`, `BACKEND_SERVER_PORT` を追加 |
| 16 | `Frontend/README.md` | 環境変数・アーキテクチャの説明を更新 |

---

## 検証（受け入れ基準）

### 自動テスト

- [ ] `npm run test` が全スイート通過
- [ ] `npm run build` が成功

### 手動動作確認

- [ ] **Operator 画面**: 航空機選択・制御（高度/速度/針路）・Direct to Fix・シミュレーション Start/Pause が期待どおり動作
- [ ] **Controller 画面**: 同上、加えてフライトプラン表示・Resume Navigation が動作
- [ ] **ネットワーク**: ブラウザ DevTools の Network タブで、`localhost:8080` へのリクエストが一切発生していないこと（すべて `localhost:3333/api/...` であること）

### チェックリスト（各 API）

| API | 確認方法 |
|-----|----------|
| location/all | 航空機がレーダー上に表示され、シミュレーション中に移動する |
| control | 航空機選択 → 高度/速度/針路変更 → 反映される |
| flightplan | Controller で航空機選択 → フライトプラン表示 |
| direct-to | Fix 選択 → CONFIRM → フライトプランに直行反映 |
| resume-navigation | Resume ボタン → フライトプラン再開 |
| simulation start/pause | START/Pause で航空機の移動が開始/停止 |
| ats/route/all | ATS 経路・Fix が地図上に表示される |

---

## リスク・注意点

- Next.js の Route Handler はサーバーサイドで実行されるため、`process.env.BACKEND_SERVER_*` はクライアントに公開されない
- Java バックエンドの CORS 設定は、同一オリジン経由のため BFF 導入後は簡略化できる可能性がある

---

## 関連ドキュメント

- [frontend-refactor-selected-aircraft](../frontend-refactor-selected-aircraft/spec.md) — 選択航空機 Context 化
- [Backend README](../../Backend/README.md) — Java バックエンド API 仕様
