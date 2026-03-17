# ウラノス (Uranus): ATCレーダーシミュレーションシステム バックエンド

## 概要

ウラノスは、ATCレーダーシミュレーションシステム「Horus」のバックエンドコンポーネントです。クリーンアーキテクチャに基づいて設計されており、Java 21とSpring Boot 3.3.5を使用して実装されています。航空機の位置情報管理、管制指示の処理、シナリオファイルの読み込みと実行、**コンフリクトアラート機能**などの機能を提供します。

## 技術スタック

- **言語**: Java 21
- **フレームワーク**: Spring Boot 3.3.5
- **API仕様**: OpenAPI 3.0.2
- **ビルドツール**: Gradle 8.8
- **アーキテクチャ**: クリーンアーキテクチャ + Strategy パターン + Composition

## 新機能: コンフリクトアラートシステム

### 概要
航空機間の衝突リスクを評価し、管制官にアラートを発する高精度なコンフリクト検出システムを実装しました。CPA（Closest Point of Approach）分析を基盤とした数学的計算により、最大200機の同時処理が可能です。

### 主な特徴

#### アラートレベル
- **安全** (危険度0-29): 管制間隔が確保されている状態
- **白コンフリクト** (危険度30-69): 5分以内に管制間隔欠如の可能性
- **赤コンフリクト** (危険度70-100): 1分以内に管制間隔欠如の可能性

#### 管制間隔基準
- **垂直間隔**: 1,000フィート
- **水平間隔**: 5海里（レーダー管制下）
- **予測時間**: 最大5分先まで

#### パフォーマンス特性
- **処理能力**: 200機同時処理（通常 < 100ms）
- **メモリ使用量**: < 50MB
- **精度**: CPA分析による高精度予測
- **並列処理**: スレッドセーフな実装
- **最適化**: 事前フィルタリングによる高速化

### APIエンドポイント

#### コンフリクト検出API

```http
# 全てのコンフリクトアラートを取得
GET /api/conflict/all

# 指定レベル以上のコンフリクトを取得
GET /api/conflict/filtered?level={SAFE|WHITE_CONFLICT|RED_CONFLICT}

# 緊急度の高いコンフリクト（赤コンフリクト）を取得
GET /api/conflict/critical

# 管制間隔欠如が予測されるコンフリクトを取得
GET /api/conflict/violations

# 特定航空機に関連するコンフリクトを取得
GET /api/conflict/aircraft/{callsign}

# コンフリクトアラートの統計情報を取得
GET /api/conflict/statistics

# システムヘルスチェック
GET /api/conflict/health
```

#### 使用例

```bash
# 全コンフリクトの取得
curl http://localhost:8080/api/conflict/all

# 白コンフリクト以上の取得
curl http://localhost:8080/api/conflict/filtered?level=WHITE_CONFLICT

# 緊急アラートの取得
curl http://localhost:8080/api/conflict/critical

# 統計情報の取得
curl http://localhost:8080/api/conflict/statistics
```

#### レスポンス例

```json
{
  "JAL512-ANA456": {
    "riskLevel": 75.2,
    "timeToClosest": 45.5,
    "closestHorizontalDistance": 3.2,
    "closestVerticalDistance": 500.0,
    "conflictPredicted": true,
    "alertLevel": "RED_CONFLICT"
  }
}
```

---

## 環境構築

### 前提条件

> [!CAUTION]
> Java 24では互換性の問題が発生するため、必ずJava 21または22を使用してください。

- Java Development Kit (JDK) 21または22
- Gradle 8.8以上（8.8推奨）

### インストール手順

1. リポジトリのクローン
```bash
git clone https://github.com/your-username/horus.git
cd horus/Backend
```

2. アプリケーションのビルド
```bash
./gradlew build
```

> [!NOTE]
> Java 24がシステムのデフォルトになっている場合は、以下のように環境変数を設定してください：
> ```bash
> JAVA_HOME=/path/to/java21 ./gradlew build
> ```
> Macの場合は通常 `/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home` にインストールされます。

3. アプリケーションの実行
```bash
./gradlew bootRun
```

4. APIドキュメントへのアクセス
```
http://localhost:8080/docs.html
```

5. コンフリクトアラート機能のテスト
```bash
# 基本テストの実行
./gradlew test --tests "*ConflictDetectorTest"

# サンプル実行
./gradlew run --args="jp.ac.tohoku.qse.takahashi.AtcSimulator.example.ConflictDetectionExample"
```

6. 最適化されたリポジトリのテスト
```bash
# パフォーマンステストを含む全テストの実行
./gradlew test --tests "*AircraftRepositoryInMemoryTest"
```

## 動作確認

### バックエンドの実行

```bash
cd Backend
./gradlew bootRun
```

- 起動後、API ドキュメント: http://localhost:8080/docs.html
- デフォルトポート: **8080**

### フロントエンドの実行

別ターミナルで以下を実行：

```bash
cd Frontend
npm install
cp .env.sample .env.local   # 初回のみ。BACKEND_SERVER_IP / PORT を編集
npm run dev
```

- ブラウザ: http://localhost:3333
- フロントエンドは Next.js BFF 経由で API を呼び出す。BFF が `BACKEND_SERVER_IP` / `BACKEND_SERVER_PORT` で本バックエンドに接続する（`.env.local` で変更可）

### 連携確認の手順

1. バックエンドを起動（`./gradlew bootRun`）
2. フロントエンドを起動（`npm run dev`）
3. ブラウザで http://localhost:3333 を開く
4. シミュレーション開始 → 航空機作成 → レーダー表示・管制指示が動作することを確認

### フライトプラン機能のマニュアルテスト

Swagger を用いた詳細な手順は [docs/test-data/MANUAL_TEST.md](docs/test-data/MANUAL_TEST.md) を参照してください。

## プロジェクト構造

プロジェクトはクリーンアーキテクチャに基づき、以下のレイヤーで構成されています：

```
jp.ac.tohoku.qse.takahashi.AtcSimulator/
├── application/                # アプリケーションレイヤー
│   ├── AircraftFactory.java         # DTO→ドメイン変換
│   ├── AircraftRadarService.java
│   ├── ConflictAlertService.java    # ★ コンフリクトアラートサービス
│   ├── GetAllAircraftLocationsWithRiskUseCase.java  # 位置情報 JSON 取得ユースケース
│   ├── ScenarioService.java         # シナリオ実行インターフェース
│   ├── ScenarioServiceImpl.java    # シナリオ実行実装
│   └── aircraft/
│       └── AircraftRadarServiceImpl.java
├── config/
│   ├── globals/
│   ├── AtcSimulatorApplicationConfig.java
│   ├── AtcSimulatorDomainConfig.java  # ドメイン Bean・スケジューラ設定
│   ├── ConflictDetectionConfig.java
│   └── WebConfig.java
├── domain/model/
│   ├── aggregate/airspace/          # AirspaceManagement（シナリオ駆動）
│   ├── entity/
│   │   ├── aircraft/
│   │   └── fix/
│   │       └── FixPositionRepository.java  # Fix 位置取得インターフェース
│   ├── service/conflict/
│   │   └── ConflictDetector.java
│   └── valueObject/
├── infrastructure/
│   ├── fix/
│   │   └── AtsRouteFixPositionRepository.java  # Fix・ATS 経路データ
│   └── persistence/inMemory/
│       └── AircraftRepositoryInMemory.java
├── interfaces/
│   ├── api/
│   │   ├── AtsRouteService.java
│   │   ├── ConflictAlertController.java
│   │   ├── ControlAircraftService.java
│   │   ├── CreateAircraftService.java
│   │   ├── LocationService.java
│   │   ├── ScenarioController.java      # シナリオ一括ロード API ✅
│   │   └── SimulationService.java
│   └── dto/
│       ├── AircraftLocationDto.java  # 位置情報 JSON レスポンス用
│       ├── ControlAircraftDto.java
│       └── CreateAircraftDto.java
├── shared/
│   ├── constants/AtcSimulatorConstants.java
│   └── utility/GeodeticUtils.java
└── example/
    └── ConflictDetectionExample.java
```

### ドキュメント構成

- **設計ドキュメント** (`docs/design/`): 機能の仕様・アーキテクチャを定義
- **実装計画** (`spec/`): 設計に基づくタスク分解と進捗管理（各計画は `spec/{feature}/spec.md`）

**完了済み**: [backend-redesign](../spec/backend-redesign/spec.md)（クリーンアーキテクチャ再設計、Phase 1〜7 完了）。新機能の実装時は [spec/](../spec/) を参照。

## 主要コンポーネント

### 1. ドメインモデル

#### 航空機エンティティ

航空機の状態と振る舞いを表現します。

**主要クラス**:
- `Aircraft.java` - 航空機インターフェース
- `AircraftBase.java` - 基本的な航空機の実装
- `CommercialAircraft.java` - 商用航空機の実装

**主な機能**:
- 位置情報の更新
- 速度ベクトルの計算
- 管制指示の反映
- 航跡の計算

#### 値オブジェクト

不変の値を表現します。

**主要クラス**:
- `AircraftPosition.java` - 航空機の位置
- `AircraftVector.java` - 航空機の速度ベクトル
- `InstructedVector.java` - 指示された速度ベクトル
- `Callsign.java` - 航空機のコールサイン
- **★ `RiskAssessment.java` - コンフリクトリスク評価結果**
- **★ `AlertLevel.java` - アラートレベル列挙型**

### 2. アプリケーションサービス

ドメインモデルを操作するためのサービスを提供します。

**主要クラス**:
- `AircraftRadarService.java` - 航空機の位置情報取得サービス
- `ScenarioService.java` / `ScenarioServiceImpl.java` - シナリオ実行サービス（`application` 層に配置）
- `AircraftFactory.java` - DTO からドメインオブジェクトへの変換
- `GetAllAircraftLocationsWithRiskUseCase.java` - レーダー表示用の位置情報（JSON）取得ユースケース
- **★ `ConflictAlertService.java` - コンフリクトアラート管理サービス**

### 3. ドメインサービス

- **★ `ConflictDetector.java` - コンフリクト検出メインロジック** (`domain/model/service/conflict/`)

**共有ユーティリティ** (`shared/utility/`):
- **★ `GeodeticUtils.java` - 測地計算ユーティリティ**

### 4. インフラストラクチャ

データの永続化とシステムリソースへのアクセスを提供します。

**主要クラス**:
- **★ `AircraftRepositoryInMemory.java`** - インメモリ航空機リポジトリ (`infrastructure/persistence/inMemory/`)
- **`AtsRouteFixPositionRepository.java`** - Fix 位置・ATS 経路データ (`infrastructure/fix/`)
  - 読み込み元: `fix/waypoints.json`, `fix/radio_navigation_aids.json`, `fix/ats_lower_routes.json`, `fix/rnav_routes.json`, `fix/japan-outline.json`

### 5. インターフェース

外部とのインターフェースを提供します。

**主要クラス**:
- `ControlAircraftService.java` - 航空機制御APIエンドポイント
- `CreateAircraftService.java` - 航空機作成APIエンドポイント
- `LocationService.java` - 位置情報取得APIエンドポイント
- `SimulationService.java` - シミュレーション制御APIエンドポイント
- **★ `ConflictAlertController.java` - コンフリクトアラートAPIエンドポイント**

## API仕様

RESTful APIを提供しており、詳細なAPI仕様は`UranosAPI.yml`ファイルに記載されています。

主なエンドポイント:

1. **航空機制御**
   - `POST /api/aircraft/control/{callsign}` - 特定の航空機に管制指示を与える
   - `POST /api/aircraft/control/{callsign}/direct/{fixName}` - 特定の航空機を特定のFIXに直行させる

2. **位置情報取得**（JSON 形式）
   - `GET /aircraft/location/all` - 全航空機の現在位置を取得
   - `GET /aircraft/location?callsign={callsign}` - 特定航空機の位置を取得

3. **シミュレーション・シナリオ**
   - `POST /api/scenario/load` - シナリオ一括ロード（空域クリア＋複数機スポーン）。シミュレーション開始は `POST /simulation/start` で行う ✅
   - `POST /simulation/start` - シミュレーションを開始
   - `POST /simulation/pause` - シミュレーションを一時停止
   - `GET /simulation/status` - シミュレーションの状態を取得

4. **★ コンフリクトアラート（新規）**
   - `GET /api/conflict/all` - 全コンフリクトアラートを取得
   - `GET /api/conflict/filtered` - フィルタされたコンフリクトを取得
   - `GET /api/conflict/critical` - 緊急アラートを取得
   - `GET /api/conflict/violations` - 管制間隔欠如予測を取得
   - `GET /api/conflict/aircraft/{callsign}` - 特定航空機のコンフリクトを取得
   - `GET /api/conflict/statistics` - 統計情報を取得

## 開発ガイドライン

### コード規約

1. **クリーンアーキテクチャの原則を遵守**
   - 依存関係は外側から内側に向かうようにする
   - ドメインレイヤーはフレームワークに依存しない

2. **DDD (ドメイン駆動設計) の原則を適用**
   - エンティティ、値オブジェクト、集約、リポジトリの区別を明確にする
   - ユビキタス言語を使用する

3. **SOLIDの原則を遵守**
   - 単一責任の原則
   - オープン・クローズドの原則
   - リスコフの置換原則
   - インターフェース分離の原則
   - 依存性逆転の原則

### コードフォーマット (Spotless)

- **Spotless** で未使用 import の削除、import 順序、末尾空白・改行を統一
- `./gradlew spotlessApply` … 自動整形
- `./gradlew spotlessCheck` … CI で実行（違反があれば fail）
- **pre-commit フック**: ルートで `npm install` 実行後、`.java` を commit すると自動で `spotlessApply` が走る

### テスト

1. ユニットテスト
   - ドメインモデルのテスト
   - アプリケーションサービスのテスト
   - リポジトリのテスト
   - **★ コンフリクト検出機能のテスト**

2. 統合テスト
   - APIエンドポイントのテスト
   - シナリオのテスト

### ★ コンフリクト検出機能のテスト実行

```bash
# 全テストの実行
./gradlew test

# コンフリクト検出のみ
./gradlew test --tests "*ConflictDetectorTest"

# パフォーマンステスト
./gradlew test --tests "*ConflictDetectorTest.testPerformanceWith200Aircraft"

# サンプル実行
java -cp build/libs/atc-simulator.jar jp.ac.tohoku.qse.takahashi.AtcSimulator.example.ConflictDetectionExample
```

## 将来の拡張

1. **永続化層の追加**
   - インメモリからデータベースへの移行
   - キャッシュの実装

2. **シナリオ機能の拡張**
   - 複雑なフライトシナリオの追加
   - シナリオエディタの開発

3. **パフォーマンス最適化**
   - 大規模シミュレーションのサポート
   - 分散処理の実装

4. **★ コンフリクト検出機能の拡張**
   - 複数航路での同時検出
   - 予測精度の向上
   - 機械学習による危険度評価の改善
   - リアルタイム通知機能

## ライセンス

このプロジェクトはオープンソースとして公開されており、研究・教育目的で自由に利用できます。

## 実装計画・改善項目

実装計画（フェーズ・技術的負債）の一覧は [spec/spec.md](../spec/spec.md) を参照してください。
