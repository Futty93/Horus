# フロントエンド BFF（Backend for Frontend）導入 実装計画

## 概要

Next.js を BFF として導入し、クライアントが Java バックエンドに直接アクセスする現状の構成を、Next.js API Routes 経由のプロキシ構成に変更する。**本 spec は今回の PR ではスコープ外**とする。

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
| `/aircraft/location/all` | GET | `location.ts` | 航空機位置一覧取得 |
| `/api/aircraft/control/{callsign}` | POST | `controlAircraft.tsx`, `inputInfoArea.tsx` | 航空機制御指示 |
| `/api/aircraft/{callsign}/flightplan` | GET | `flightPlan.ts` | フライトプラン取得 |
| `/api/aircraft/{callsign}/direct-to` | POST | `flightPlan.ts`, `selectFixMode.tsx` | 直行指示 |
| `/api/aircraft/{callsign}/resume-navigation` | POST | `flightPlan.ts` | ナビゲーション再開 |
| `/simulation/start` | POST | `simulationControlButtons.tsx`, `simulation.ts` | シミュレーション開始 |
| `/simulation/pause` | POST | `simulationControlButtons.tsx`, `simulation.ts` | シミュレーション一時停止 |
| `/ats/route/all` | GET | `atsRoutesLoader.ts` | ATS 経路データ取得 |

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
│   │       └── route.ts         # POST → /simulation/{start|pause}
│   └── ats/
│       └── route/
│           └── all/
│               └── route.ts     # GET → /ats/route/all
```

各 Route Handler はリクエストを Java バックエンドへ転送し、レスポンスをそのまま返す（パス・ボディ・ヘッダを適宜マッピング）。

#### 1.3 プロキシ実装の共通パターン

**新規**: `Frontend/utility/api/backendProxy.ts`（サーバー専用）

```typescript
// サーバー側のみで使用。getBackendBaseUrl() で Java の URL を取得
export async function proxyToBackend(
  path: string,
  init?: RequestInit
): Promise<Response> {
  const base = getBackendBaseUrl();
  const url = `${base}${path}`;
  return fetch(url, init);
}
```

- クライアントから渡された `Request` の body / headers を必要な範囲で転送
- Java のレスポンスをそのまま返す（status, body, headers）

#### 1.4 フロントエンド API クライアントの変更

| ファイル | 変更内容 |
|----------|----------|
| `utility/api/location.ts` | `fetch('/api/aircraft/location/all', ...)` に変更（相対 URL） |
| `utility/api/flightPlan.ts` | `baseUrl()` を削除し `/api/aircraft/${callsign}/...` を使用 |
| `utility/api/controlAircraft.ts` | （未使用の可能性あり。`controlAircraft.tsx` 等で直接 fetch しているため要確認） |
| `utility/api/simulation.ts` | `/api/simulation/${action}` に変更 |
| `utility/AtsRouteManager/atsRoutesLoader.ts` | `/api/ats/route/all` に変更 |
| `components/controlAircraft.tsx` | `/api/aircraft/control/${callsign}` に変更 |
| `components/inputInfoArea.tsx` | 同上 |
| `components/selectFixMode.tsx` | `flightPlan.ts` の `directToFix` を使用するか、`/api/aircraft/${callsign}/direct-to` へ統一 |
| `components/simulationControlButtons.tsx` | `/api/simulation/start`, `/api/simulation/pause` に変更 |

- すべての API 呼び出しで **相対 URL**（`/api/...`）を使用
- `NEXT_PUBLIC_SERVER_IP` / `NEXT_PUBLIC_SERVER_PORT` の参照を削除

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
| 11 | `utility/AtsRouteManager/atsRoutesLoader.ts` | 変更 |
| 12 | `components/controlAircraft.tsx` | 変更 |
| 13 | `components/inputInfoArea.tsx` | 変更 |
| 14 | `components/selectFixMode.tsx` | 変更（`flightPlan.ts` 使用に統一する場合は簡略化） |
| 15 | `components/simulationControlButtons.tsx` | 変更 |
| 16 | `.env.sample` | `BACKEND_SERVER_IP`, `BACKEND_SERVER_PORT` を追加 |
| 17 | `Frontend/README.md` | 環境変数・アーキテクチャの説明を更新 |

---

## 検証

1. **Operator 画面**: 航空機選択・制御・Direct to Fix・シミュレーション Start/Pause が動作する
2. **Controller 画面**: 同上、加えてフライトプラン取得・Resume が動作する
3. **ネットワーク**: ブラウザの DevTools で Java 直アクセスが発生していないことを確認
4. **ビルド**: `npm run build` が通る

---

## リスク・注意点

- Next.js の Route Handler はサーバーサイドで実行されるため、`process.env.BACKEND_SERVER_*` はクライアントに公開されない
- Java バックエンドの CORS 設定は、同一オリジン経由のため BFF 導入後は簡略化できる可能性がある
- 本 spec は今回の PR のスコープ外であり、別 PR で実装する

---

## 関連ドキュメント

- [frontend-refactor-selected-aircraft](../frontend-refactor-selected-aircraft/spec.md) — 選択航空機 Context 化
- [Backend README](../../Backend/README.md) — Java バックエンド API 仕様
