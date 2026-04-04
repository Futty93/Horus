# オモテノス (Omotenus): ATCレーダーシミュレーションシステム フロントエンド

## 概要

オモテノスは、ATCレーダーシミュレーションシステム「Horus」のフロントエンドコンポーネントです。Next.js 14.2.15とTypeScriptを使用して実装されており、航空管制レーダーとして洗練された UI とリアルタイムな航空機情報の表示を提供します。

### 想定ワークフロー（訓練シミュレーション）

| 画面                  | 役割         | 操作                                                                                                                        |
| --------------------- | ------------ | --------------------------------------------------------------------------------------------------------------------------- |
| **Controller**        | 管制官役     | レーダーを見ながら音声で指示を出す。発出した指示をメモとして入力・反映する。航空機の直接操作（API 送信）は行わない。        |
| **Operator**          | パイロット役 | 管制官の音声指示を復唱しながら入力し、バックエンドへ送信。航空機の実際の操作（高度・速度・針路、Direct To、Resume）を行う。 |
| **Flight Plan Setup** | シナリオ準備 | テンプレート読み込み、経路自動提案（Load & Suggest）、O/D ごとのルート定義、ATS ルート検索、一括フライトプラン割り当て。    |

フライトプラン作成・航空機 spawn は **Flight Plan Setup** ページ（`/flight-plan-setup`）で一括割り当てできる。Swagger（`http://localhost:8080/docs.html`）でも可能。

## 技術スタック

- **フレームワーク**: Next.js 14.2.15
- **言語**: TypeScript
- **スタイリング**: Tailwind CSS with ATC Theme（Industrial / Utilitarian）
- **状態管理**: React Context API
- **ビルドツール**: esbuild

## 🎨 デザインシステム

### ATC テーマ（Industrial / Utilitarian）

航空管制レーダーとして洗練された見た目を目指し、ネオンやグローを廃止。本物の ATC コンソールに近い控えめなパレットを採用しています。

#### カラーパレット（atc-\*）

- **背景**: `atc-bg` #0d1117, `atc-surface` #161b22, `atc-surface-elevated` #21262d
- **ボーダー**: `atc-border` #30363d
- **テキスト**: `atc-text` #c9d1d9, `atc-text-muted` #8b949e
- **アクセント**: `atc-accent` #238636, `atc-accent-hover` #2ea043
- **警告・危険**: `atc-warning` #9e6a03, `atc-danger` #cf2222

### コンポーネント設計原則

1. **統一されたテーマ**: 全コンポーネントで一貫した ATC テーマ（atc-\* パレット）を使用
2. **インタラクティブフィードバック**: ホバー・フォーカス時は `transition-colors` による控えめな反応
3. **アクセシビリティ**: キーボードナビゲーションとスクリーンリーダー対応
4. **パフォーマンス**: CSS-in-JSではなくTailwindによる最適化されたスタイリング

## 環境構築

### 前提条件

- Node.js 18.0.0以上
- npm 9.0.0以上

### インストール手順

1. リポジトリのクローン

```bash
git clone https://github.com/your-username/horus.git
cd horus/Frontend
```

2. 依存関係のインストール

```bash
npm install
```

3. 環境変数の設定

```bash
cp .env.sample .env.local
```

`.env.local`ファイルを開き、以下の環境変数を設定してください：

```
BACKEND_SERVER_IP=localhost   # Java バックエンドのホスト（サーバー側のみ、クライアントに公開されない）
BACKEND_SERVER_PORT=8080      # Java バックエンドのポート
```

フロントエンドは同一オリジン（`/api/*`）経由で API を呼び出し、Next.js BFF が Java バックエンドへプロキシします。`NEXT_PUBLIC_SERVER_*` は廃止済みです。

4. 開発サーバーの起動

```bash
npm run dev
```

5. ブラウザでアクセス

```
http://localhost:3333
```

6. テストの実行

```bash
npm test
```

## プロジェクト構造

```
Frontend/
├── app/                    # Next.jsのアプリケーションルート
│   ├── api/               # BFF Route Handlers（Java バックエンドへのプロキシ）
│   ├── controller/        # コントローラー画面
│   ├── operator/          # オペレーター画面
│   ├── flight-plan-setup/ # フライトプラン一括設定画面
│   └── layout.tsx         # 共通レイアウト
├── components/            # 再利用可能なコンポーネント
│   ├── radarCanvas.tsx    # レーダー表示キャンバス
│   ├── controlAircraft.tsx # 航空機制御パネル（Operator 専用）
│   ├── instructionMemo.tsx # 指示メモ（Controller 専用）
│   ├── selectFixMode.tsx  # Fix選択モード
│   ├── routeInfoDisplaySetting.tsx # 経路情報表示設定（カスタムチェックボックス）
│   ├── flight-plan-setup/ # フライトプラン設定用（OdGroupSection, AtsRouteSearch）
│   ├── sectorSelector.tsx # セクター選択
│   ├── displayRangeSetting.tsx # 表示範囲設定（ビジュアルレンジバー）
│   └── ...
├── context/              # React Context定義
├── utility/             # ユーティリティ関数
├── tailwind.config.ts   # ATC テーマ設定
└── public/             # 静的ファイル
```

## 主要コンポーネント

### 0. トップページ (Home)

初回訪問ユーザー向けのオンボーディングハブ。システムの概要、Controller（管制官役）/ Operator（パイロット役）/ Flight Plan Setup の役割説明を提示。

**デザイン特徴**:

- HORUS ロゴ＋1文のシステム説明
- Operator / Controller / Flight Plan Setup の3カード（役割説明付きリンク）
- ATC テーマに統一したカード型レイアウト
- staggered フェードインアニメーション

### 1. レーダー表示画面 (RadarCanvas)

航空機の位置情報をリアルタイムで表示するキャンバスコンポーネント。

**主な機能**:

- 航空機の位置表示
- 経路情報の表示
- マウスインタラクション
- ズーム/パン機能

**データブロック（ラベル）の ETA**: バックエンドの `eta` は ISO 8601（UTC インスタント）。ラベル上では **UTC（Zulu）の `HH:mm`** で表示し、ブラウザのローカルタイムゾーンには依存しない（詳細は [spec/20260318-data-block-display-items/spec.md](../spec/20260318-data-block-display-items/spec.md)）。**UTC / JST の切替**はデータブロック表示設定への項目追加で対応予定（[Issue #91](https://github.com/Futty93/Horus/issues/91)、`good first issue`）。

### 2. 航空機制御パネル (ControlAircraft)

Operator 専用。管制官の音声指示を復唱した内容を**パイロット操縦目標**（高度・速度・針路）としてバックエンドへ送信し、シミュレーション上の機体がその目標に向かう。API / DTO 上のフィールド名は `instructedVector` だが、意味は**パイロットが適用した目標**であり、管制官がストリップに書くクリアランス記録とは別データである（後者は [spec/20260326-instruction-memo-radar-label/spec.md](../spec/20260326-instruction-memo-radar-label/spec.md)）。

**デザイン特徴**:

- `atc-surface` 背景と `atc-border` ボーダー
- 単位表示付きの入力フィールド（Altitude / Speed / Heading）
- ホバー・フォーカス時のスケーリングとグローエフェクト
- `button-gradient` による実行ボタン

### 3. 指示メモ (InstructionMemo)

Controller 専用。レーダーで航空機を選択すると、入力欄に当該機の**実測**（`position.altitude` / `vector` の地速・針路、`GET /api/aircraft/location/all`）を自動で入れる（あとから「Refresh inputs from current」で取り直し可）。管制官が発出した**クリアランス**を数値で編集し、**RECORD** で `POST /api/aircraft/{callsign}/atc-clearance`（BFF 経由）へ送信する。バックエンドが `atcClearance` として保持し、`GET /api/aircraft/location/all` で全クライアントに配信。**レーダー上の「管制メモ行」**（`atcClearance` vs 実測の差、略号・スラッシュ表記）は **Controller 画面（`/controller`）のレーダーのみ**に描画する。Controller では **管制クリアランスに高度が記録されているとき**、データブロックの **2 行目（↑/↓ 付き高度行）** はパイロット目標ではなく **クリアランス高度 vs 実測** を表示する（パイロット目標の未更新で矢印が狂うのを防ぐ）。その場合、水色の管制メモ行から **高度ペアは省略**し、針路・地速の差分のみを出す。Operator 画面（`/operator`）ではパイロット目標行（`instructedVector` vs 実測）のみとし、管制メモ行は表示しない（役割分担と主ブロックとの見かけ上の重複を避ける）。管制メモ行の ON/OFF は Controller のデータブロック設定のみ。`POST .../control` は呼ばず、機体のパイロット目標（`instructedVector`）は変更しない。

### 4. Fix選択モード (SelectFixMode)

Operator 専用。航空機の直行指示（Direct To）をレーダー上の Fix 選択で行う。

### 5. フライトプラン設定ページ (Flight Plan Setup)

`/flight-plan-setup`。テンプレート読み込み（28機）、**Load & Suggest Routes**（テンプレート読み込みと同時に A\* 経路提案でルートを自動補完）、O/D ペアごとのグループ化、**Suggest route**（空港間の最短経路を1クリックで適用）、ATS ルート検索、ルート定義、一括フライトプラン割り当てを提供。右側にルートプレビューマップを表示。

### 6. フライトプラン制御 (FlightPlanControl)

Operator 専用。Direct To（Fix 名入力）、Resume Navigation をバックエンドへ送信。

### 7. 経路情報表示設定 (RouteInfoDisplaySetting)

カスタムデザインのチェックボックスを使用した設定パネル。経路表示のオン/オフを切替。

### 8. セクター選択 (SectorSelector)

カスタムドロップダウンデザインによるセクター選択。

**デザイン特徴**:

- カスタム矢印アイコン
- ホバー時のネオンエフェクト
- フォーカス時のボーダー強調

### 9. 表示範囲設定 (DisplayRangeSetting)

ビジュアルインジケーター付きの数値入力。

**デザイン特徴**:

- プログレスバースタイルの視覚化
- 現在値の動的表示
- スピンボタンの非表示

## 状態管理

React Context APIを使用して、以下の状態を管理しています：

1. **中心座標** (CenterCoordinateContext)
2. **表示範囲** (DisplayRangeContext)
3. **経路情報表示設定** (RouteInfoDisplaySettingContext)
4. **データブロック表示設定** (DataBlockDisplaySettingContext) — スクオーク・機種・ETA・**管制クリアランスメモ行**の表示 ON/OFF（**管制メモ行のトグルは Controller 画面のみ**。Operator では該当チェックボックスを出さない）
5. **Fix選択モード** (SelectFixModeContext)
6. **選択航空機** (SelectedAircraftContext) — callsign と `instructedVector`（パイロット操縦目標: altitude, groundSpeed, heading）

## API通信

クライアントは同一オリジン（`/api/*`）を呼び出し、Next.js BFF が Java バックエンドへプロキシします。主なエンドポイント：

- `GET /api/aircraft/location/all` - 航空機位置一覧取得（各要素に `atcClearance` が含まれる場合あり）
- `POST /api/aircraft/create-haneda-samples` - Haneda Samples（約28機）作成
- `POST /api/aircraft/spawn-with-flightplan` - フライトプラン付き航空機スポーン
- `POST /api/scenario/load` - シナリオ一括ロード（空域クリア＋複数機スポーン＋シミュレーション開始）
- `POST /api/aircraft/control/{callsign}` - パイロット操縦目標の送信（`instructedVector` 更新。管制クリアランスメモとは別）
- `POST /api/aircraft/{callsign}/atc-clearance` - **管制クリアランスメモ**の記録（Body は control と同型。`atcClearance` として保持）
- `GET /api/aircraft/{callsign}/flightplan` - フライトプラン取得
- `POST /api/aircraft/{callsign}/flightplan` - フライトプラン割り当て
- `POST /api/aircraft/{callsign}/direct-to` - 直行指示
- `POST /api/aircraft/{callsign}/resume-navigation` - ナビゲーション再開
- `POST /api/simulation/start` - シミュレーション開始
- `POST /api/simulation/pause` - シミュレーション一時停止
- `GET /api/ats/route/all` - ATS 経路・Fix・日本海岸線（japanOutline）取得
- `GET /api/ats/route/suggest?origin=&destination=` - A\* による空港間経路提案

## 開発ガイドライン

### デザインシステムの使用

1. **カラー**: `tailwind.config.ts` の atc パレットを使用
2. **アニメーション**: 最小限（fade-in のみ）。glow / scan は使用しない

### スタイリング規則

1. **一貫性**: 全コンポーネントで統一されたクラス命名とスタイリング
2. **レスポンシブ**: モバイルファーストアプローチ
3. **パフォーマンス**: 必要最小限のCSSクラスの使用
4. **アクセシビリティ**: 適切なコントラスト比とキーボードナビゲーション

## デプロイメント

### ビルド

```bash
npm run build
```

### 本番環境での実行

```bash
npm start
```

## トラブルシューティング

### よくある問題と解決方法

1. **Tailwindクラスが適用されない**
   - `tailwind.config.ts`の設定を確認
   - CSSのビルドとキャッシュクリア

2. **アニメーションが動作しない**
   - ブラウザの設定で動画を有効にする
   - CSSアニメーションの対応確認

## 実装計画・改善項目

実装計画（フェーズ・技術的負債）の一覧は [spec/spec.md](../spec/spec.md) を参照してください。

## 貢献ガイドライン

1. デザインシステムの一貫性を保つ
2. 新しいコンポーネントは既存のテーマに従う
3. アニメーションは控えめで意味のあるものにする
4. テストとドキュメントの更新

## ライセンス

このプロジェクトはオープンソースとして公開されており、研究・教育目的で自由に利用できます。
