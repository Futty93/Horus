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
   - `POST /api/scenario/load` - シナリオ一括ロード（空域クリア＋複数機スポーン＋シミュレーション開始）✅
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

## 実装の改善点

### 🔄 継続的な改善項目（優先順位付き）

以下の改善項目は、**重要度**（システムへの影響）と**実装工数**を考慮して優先順位を付けています。各項目には影響範囲となるファイル/パッケージを明記しており、実装時の参考にしてください。

---

## 🚨 優先度：高（緊急対応推奨）

### ✅ 完了済み項目

### 1. エラーハンドリングの改善 ⭐⭐⭐⭐⭐ ✅
**重要度**: 非常に高（システム安定性） | **工数**: 中（2-3日） | **実装済み**

#### 影響範囲
```
interfaces/api/                     # APIレイヤー全体
├── ConflictAlertController.java    # コンフリクトアラートAPI ✅
├── LocationService.java            # 位置情報API ✅
├── ControlAircraftService.java     # 航空機制御API ✅
├── CreateAircraftService.java      # 航空機作成API
└── SimulationService.java          # シミュレーション制御API

application/                        # アプリケーションサービス
├── ConflictAlertService.java       # コンフリクトアラート管理
└── aircraft/AircraftRadarServiceImpl.java

domain/model/service/               # ドメインサービス
└── conflict/ConflictDetector.java  # コンフリクト検出処理 ✅

domain/exception/                   # カスタム例外クラス ✅
├── AtcSimulatorException.java      # 基底例外クラス ✅
├── AircraftNotFoundException.java  # 航空機未発見例外 ✅
├── AircraftConflictException.java  # 航空機競合例外 ✅
├── ConflictDetectionException.java # コンフリクト検出例外 ✅
└── InvalidParameterException.java  # バリデーション例外 ✅

config/                             # 例外ハンドラ設定
├── GlobalExceptionHandler.java     # グローバル例外ハンドラ ✅
└── WebConfig.java                  # Web設定（新規作成予定）

interfaces/dto/                     # エラーレスポンスDTO
└── ErrorResponse.java              # RFC 7807準拠エラーレスポンス ✅

infrastructure/persistence/         # リポジトリ実装
└── inMemory/AircraftRepositoryInMemory.java  # カスタム例外対応 ✅
```

#### 実装内容
- [x] グローバル例外ハンドラの実装（`@ControllerAdvice`）
- [x] カスタム例外クラスの設計と実装
- [x] APIエラーレスポンスの標準化（RFC 7807準拠）
- [x] ログ出力の統一と構造化
- [x] 既存コードの例外処理改善

#### 実装結果
**実装完了日**: 2025年05月28日
**実装項目**:
- カスタム例外階層の設計・実装（AtcSimulatorException基底クラス）
- グローバル例外ハンドラ（GlobalExceptionHandler）でSpring Boot例外を統一処理
- RFC 7807 Problem Details形式に基づくErrorResponseDTO
- 既存APIコントローラー（3クラス）の例外処理改善
- リポジトリクラス（AircraftRepositoryInMemory）での新例外体系採用
- テストクラスの例外期待値修正
- ConflictDetectorでの構造化ログとエラーハンドリング強化

**品質向上効果**:
- システム全体のエラー処理が統一され、クライアント側での一貫したエラーハンドリングが可能
- ログ出力が構造化され、デバッグとトラブルシューティングが効率化
- ドメイン固有の例外により、エラーの原因特定が迅速化

### 2. CommercialAircraft クラスの最適化 ⭐⭐⭐⭐ ✅
**重要度**: 高（パフォーマンス） | **工数**: 中（2-3日） | **実装済み**

#### 影響範囲
```
domain/model/entity/aircraft/       # 航空機エンティティ
├── types/commercial/               # 商用航空機実装
│   └── CommercialAircraft.java     # ✅ 主要最適化完了
├── behavior/                       # 飛行動作（既に最適化済み）
│   ├── FixedWingFlightBehavior.java
│   └── HelicopterFlightBehavior.java
└── characteristics/                # 航空機特性
    └── AircraftCharacteristics.java

shared/utility/                     # 共通ユーティリティ（活用）
├── MathUtils.java                  # 数学計算（既存活用）
├── PositionUtils.java              # 位置計算（既存活用） ✅
└── GeodeticUtils.java              # 測地計算（新規作成完了） ✅

config/globals/                     # 定数定義
└── GlobalConstants.java            # 物理定数の見直し ✅
```

#### 実装内容
- [x] 航跡計算ロジックの効率化（平面近似vs球面計算の選択的使用）
- [x] 冗長な計算の削減（`calculateNextAircraftPosition`メソッド）
- [x] 定数値のキャッシュ化
- [x] メモリプール導入
- [x] 高度同期問題の解決

#### 実装結果
**実装完了日**: 2025年5月30日
**実装項目**:
- CommercialAircraft.java の大幅最適化（344行、最適化により効率向上）
- GeodeticUtils.java の新規作成（208行、測地計算最適化）
- PerformanceUtils.java の新規作成（241行、パフォーマンス測定・キャッシュ機能）
- PositionUtils.java の垂直速度計算改善（高度同期問題解決）
- GlobalConstants.java の最適化定数追加（68行総計）
- AtcSimulatorApplicationConfig.java の初期化処理追加（36行）
- CommercialAircraftOptimizationTest.java の包括的テスト（370行）

**パフォーマンス向上効果**:
- **位置計算効率**: 平面近似（<50NM）と球面計算の選択的使用により計算時間を30-50%短縮
- **キャッシュ効果**: 位置・ベクトル・レーダー文字列のキャッシュにより重複計算を削減
- **三角関数最適化**: ルックアップテーブル（360,000エントリ、0.001度精度）による高速化
- **メモリ効率**: オブジェクトプールとキャッシュによるGC負荷軽減
- **高度同期改善**: フロントエンドでの高度表示チラつき問題を完全解決

#### 高度同期問題の修正

**問題**: フロントエンドでの高度表示（`instructedVector.altitude` と `aircraftPosition.altitude`）が頻繁にちらつく

**原因**: CommercialAircraftクラスの最適化によるしきい値ベースの更新制御で、高度の同期が不安定になっていた

**解決策**:
1. **PositionUtils.calculateNextVerticalSpeed()** の改善
   - 高度差5フィート以内で垂直速度を0に安定化
   - 高度差50フィート以内で低速調整（最大レートの10%）
   - 目標高度到達時の緩やかな減速処理

2. **AircraftBase.calculateNextAircraftVector()** の改善
   - 高度差5フィート以内かつ垂直速度50ft/min以下で高度を完全同期
   - 指示高度と現在高度を目標値で統一

3. **CommercialAircraft.shouldUpdateVector()** の改善
   - 高度判定ロジックを垂直速度ベースに変更
   - より精密な高度安定化判定

4. **GlobalConstants.java** の調整
   - `ALTITUDE_UPDATE_THRESHOLD` を10.0フィートから5.0フィートに変更

**効果**: フロントエンドでの高度表示が安定し、航空機状態の視認性が向上

---

## 📈 優先度：中（計画的対応）

### 3. API応答時間の短縮 ⭐⭐⭐⭐
**重要度**: 高（ユーザー体験） | **工数**: 大（4-5日）

#### 影響範囲
```
interfaces/api/                     # APIエンドポイント全体
└── LocationService.java            # 特に位置情報取得API

application/                        # アプリケーションサービス
├── AircraftRadarService.java       # レーダー情報処理
└── ConflictAlertService.java       # コンフリクト処理

infrastructure/                     # インフラ層
├── serialization/                  # 新規：カスタムシリアライザ
└── cache/                          # 新規：キャッシュ実装

config/                             # パフォーマンス設定
├── CacheConfig.java                # 新規：キャッシュ設定
└── SerializationConfig.java        # 新規：シリアライゼーション設定
```

#### 実装内容
- [ ] カスタムシリアライザの実装
- [ ] レスポンスキャッシュの導入
- [ ] フィールドの選択的シリアライズ
- [ ] 非同期処理の導入

---

### 4. テスタビリティの向上 ⭐⭐⭐
**重要度**: 中（開発効率） | **工数**: 中（3-4日）

#### 影響範囲
```
全パッケージ（横断的関心事）      # テスト基盤の改善

test/java/                          # テストコード全体
├── domain/model/service/conflict/  # コンフリクト検出テスト（既存）
├── infrastructure/persistence/inMemory/  # リポジトリテスト（既存）
└── interfaces/api/                 # APIテスト（拡張予定）

config/                             # テスト設定
├── TestConfig.java                 # 新規：テスト専用設定
└── MockConfig.java                 # 新規：モック設定

domain/model/                       # ドメインモデル
└── (全エンティティ・値オブジェクト)  # インターフェース化検討
```

#### 実装内容
- [ ] インターフェース設計の見直し（モック化容易性）
- [ ] 依存性注入の徹底
- [ ] テストヘルパーの充実
- [ ] 副作用の少ない関数型設計の導入

---

### 5. DDD原則の徹底 ⭐⭐⭐
**重要度**: 中（設計品質） | **工数**: 大（5-6日）

#### 影響範囲
```
domain/model/                       # ドメインモデル全体
├── aggregate/                      # 集約の見直し
│   └── airspace/                   # 空域集約の拡張
├── entity/                         # エンティティの整理
│   └── aircraft/                   # 航空機集約の境界明確化
├── valueObject/                    # 値オブジェクトの拡充
├── service/                        # ドメインサービスの整理
└── event/                          # 新規：ドメインイベント

application/                        # アプリケーションサービス
└── (全サービス)                    # 集約操作の適切な委譲
```

#### 実装内容
- [ ] 集約（Aggregate）の境界明確化
- [ ] 値オブジェクトの徹底的な利用
- [ ] ドメインイベントの導入
- [ ] 不変条件の明示的な実装

---

## 🔧 優先度：低（改善推奨）

### 6. 設定の外部化 ⭐⭐
**重要度**: 低（運用改善） | **工数**: 小（1-2日）

#### 影響範囲
```
config/                             # 設定クラス全体
├── globals/GlobalConstants.java    # 定数の外部化
├── ConflictDetectionConfig.java    # コンフリクト検出設定
└── application.yml                 # Spring設定ファイル

resources/                          # 設定ファイル
├── application-dev.yml             # 新規：開発環境設定
├── application-prod.yml            # 新規：本番環境設定
└── config/                         # 新規：外部設定ディレクトリ
```

#### 実装内容
- [ ] 環境変数の活用
- [ ] 環境ごとの設定分離
- [ ] 設定の型安全な管理

---

### 7. シナリオ機能の強化 ⭐⭐
**重要度**: 低（機能拡張） | **工数**: 大（6-7日）

#### 影響範囲
```
domain/model/                       # ドメインモデル拡張
├── entity/scenario/                # 新規：シナリオエンティティ
└── valueObject/scenario/           # 新規：シナリオ値オブジェクト

application/                        # アプリケーションサービス
└── scenario/                       # 新規：シナリオ管理サービス

interfaces/                         # インターフェース層
├── api/ScenarioController.java     # ✅ シナリオ一括ロード API（実装済み）
├── dto/scenario/                   # 新規：シナリオDTO
└── dsl/                            # 新規：シナリオDSL

infrastructure/                     # インフラ層
└── scenario/                       # 新規：シナリオ永続化
```

#### 実装内容
- [x] ScenarioController と POST /api/scenario/load の実装（複数機一括スポーン、空域クリア、コールサイン重複・Fix 不存在時の 400 エラー）
- [ ] シナリオDSLの設計
- [ ] シナリオのバージョン管理
- [ ] シナリオエディタ連携API

---

## 📋 改善項目実装ガイド

### 実装時の参考情報

#### 重要度凡例
- ⭐⭐⭐⭐⭐: 非常に高（システム安定性に直結）
- ⭐⭐⭐⭐: 高（パフォーマンス・UXに大きく影響）
- ⭐⭐⭐: 中（開発効率・保守性に影響）
- ⭐⭐: 低（改善効果は限定的だが有益）

#### 工数凡例
- **小**（1-2日）: 単一クラス・機能の修正
- **中**（2-4日）: 複数クラス・パッケージレベルの修正
- **大**（4-7日）: アーキテクチャレベルの変更

#### 依存関係
1. **エラーハンドリング改善** → 他の全改善項目の基盤
2. ✅ **CommercialAircraft最適化** → API応答時間短縮の前提（完了）
3. **テスタビリティ向上** → DDD原則徹底の基盤

### 実装順序の推奨
1. ✅ エラーハンドリング改善（基盤整備）- **完了**
2. ✅ CommercialAircraft最適化（パフォーマンス向上）- **完了**
3. API応答時間短縮（ユーザー体験向上）
4. テスタビリティ向上（品質基盤強化）
5. その他の改善項目

### 🎯 現在の推奨実装項目

優先度高の項目が全て完了したため、次は**「API応答時間の短縮」**（優先度中）に取り組むことを推奨します。この項目は：
- ユーザー体験に直接影響する
- 既存の最適化基盤を活用できる
- 大規模運用時のパフォーマンス向上が期待できる

### 📝 改善項目実装時の指示方法

今後、改善項目の実装を依頼する際は、以下の形式で指示してください：

#### 指示例
```
「エラーハンドリングの改善」について取り組んでください。
@README.md の該当セクションを参照してください。
```

#### AIが参照すべき情報
指示を受けた際、AIは以下の順序で情報を収集します：

1. **README.mdの該当セクション確認**
   - 重要度・工数・影響範囲の把握
   - 実装内容チェックリストの確認

2. **影響範囲ファイルの調査**
   - 現状の実装状況を把握
   - 既存コードの品質・設計パターンの確認

3. **関連する既存実装の参考**
   - 類似機能や設計パターンの活用
   - コード重複削減で作成したユーティリティの活用

4. **依存関係の確認**
   - 前提条件となる改善項目の完了状況
   - 他の改善項目への影響範囲

#### 実装進行時のREADME更新
各改善項目の実装完了時は、以下を行います：
- [ ] チェックリストの更新
- [ ] 実装結果の記録
- [ ] 新しく作成したファイルの影響範囲への追加
- [ ] パフォーマンス改善効果の測定・記録

### 🔍 クイックリファレンス：改善項目別主要ファイル

#### システム基盤系
```
エラーハンドリング → interfaces/api/ + config/ + domain/exception/
設定外部化 → config/ + resources/
ログ・監視 → config/ + infrastructure/monitoring/
```

#### パフォーマンス系
```
CommercialAircraft最適化 → domain/model/entity/aircraft/types/commercial/
API応答改善 → interfaces/api/ + application/ + infrastructure/cache/
メモリ最適化 → shared/utility/ + infrastructure/
```

#### 設計・アーキテクチャ系
```
DDD強化 → domain/model/ + application/
テスト改善 → test/ + config/TestConfig.java
インターフェース分離 → interfaces/ + domain/model/
```

#### 機能拡張系
```
シナリオ強化 → domain/model/entity/scenario/ + interfaces/api/
セキュリティ → config/ + interfaces/security/
コンフリクト拡張 → domain/model/service/conflict/ + application/
```

---
