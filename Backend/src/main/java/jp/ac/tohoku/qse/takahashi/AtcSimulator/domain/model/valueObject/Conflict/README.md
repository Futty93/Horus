# 航空機間コンフリクト検出システム

## 概要

本システムは、航空管制における航空機間のコンフリクト（衝突危険性）を検出・評価するためのドメインサービスです。CPA（Closest Point of Approach）分析を基盤とした数学的手法により、2機の航空機間の危険度をリアルタイムで計算し、適切なアラートレベルを提供します。

## システム構成

### 主要コンポーネント

1. **ConflictDetector** - メインのコンフリクト検出ドメインサービス
2. **RiskAssessment** - リスク評価結果を格納する値オブジェクト
3. **AlertLevel** - アラートレベルを表現する列挙型

## 危険度評価アルゴリズム

### 1. CPA（Closest Point of Approach）分析

システムの核心となるのは、3次元空間での最接近点分析です：

```java
/**
 * CPA（Closest Point of Approach）計算
 * 3次元空間での最接近点を数学的に計算
 */
private CPAResult calculateCPA(AircraftPosition pos1, AircraftVector vec1,
                              AircraftPosition pos2, AircraftVector vec2) {
    // 相対位置ベクトル（球面座標系での近似計算）
    double[] relativePosition = calculateRelativePosition(pos1, pos2);

    // 相対速度ベクトル（水平:メートル/秒、垂直:フィート/秒）
    double[] relativeVelocity = calculateRelativeVelocity(vec1, vec2);

    // 最接近時刻を計算: t = -(r·v) / |v|²
    double dotProduct = -(relativePosition[0] * relativeVelocity[0] +
                         relativePosition[1] * relativeVelocity[1] +
                         relativePosition[2] * relativeVelocity[2]);

    double timeToClosest = dotProduct / (relativeSpeedMagnitude * relativeSpeedMagnitude);
}
```

#### 座標系と単位

- **水平座標**: メートル（球面座標系から平面近似）
- **垂直座標**: フィート（航空標準）
- **速度**: 水平はメートル/秒、垂直はフィート/秒

### 2. 危険度計算手法

危険度は以下の要素を組み合わせて算出されます：

#### a) 距離ベース評価

**水平リスク評価（5海里基準）**：
```java
private double calculateHorizontalRisk(double horizontalDistance) {
    if (horizontalDistance >= MINIMUM_HORIZONTAL_SEPARATION) {
        return 0.0; // 安全（5海里以上）
    } else if (horizontalDistance >= MINIMUM_HORIZONTAL_SEPARATION * 0.6) {
        // 3-5海里：段階的増加
        return 0.4 * (MINIMUM_HORIZONTAL_SEPARATION - horizontalDistance) /
               (MINIMUM_HORIZONTAL_SEPARATION * 0.4);
    } else {
        // 3海里未満：急激な増加
        double factor = (MINIMUM_HORIZONTAL_SEPARATION * 0.6 - horizontalDistance) /
                       (MINIMUM_HORIZONTAL_SEPARATION * 0.6);
        return 0.4 + 0.6 * factor * factor;
    }
}
```

**垂直リスク評価（1000フィート基準）**：
```java
private double calculateVerticalRisk(double verticalDistance) {
    if (verticalDistance >= MINIMUM_VERTICAL_SEPARATION) {
        return 0.0; // 安全（1000フィート以上）
    } else if (verticalDistance >= MINIMUM_VERTICAL_SEPARATION * 0.5) {
        // 500-1000フィート：段階的増加
        return 0.5 * (MINIMUM_VERTICAL_SEPARATION - verticalDistance) /
               (MINIMUM_VERTICAL_SEPARATION * 0.5);
    } else {
        // 500フィート未満：急激な増加
        double factor = (MINIMUM_VERTICAL_SEPARATION * 0.5 - verticalDistance) /
                       (MINIMUM_VERTICAL_SEPARATION * 0.5);
        return 0.5 + 0.5 * factor * factor;
    }
}
```

#### b) 時間重み係数

最接近までの時間に基づく重み付けを行います：

```java
private double calculateTimeWeight(double timeToClosest) {
    if (timeToClosest <= 60) {
        return 1.0; // 1分以内：最大重み
    } else if (timeToClosest <= 300) {
        // 1分〜5分：線形減衰
        return 1.0 - 0.8 * (timeToClosest - 60) / 240;
    } else {
        return 0.0; // 5分超：考慮外
    }
}
```

#### c) 緊急度補正

現在の距離による緊急度補正を適用：

```java
private double calculateUrgencyFactor(double currentHorizontalDistance,
                                    double currentVerticalDistance) {
    // 既に管制間隔を下回っている場合の緊急度補正
    double horizontalFactor = currentHorizontalDistance < MINIMUM_HORIZONTAL_SEPARATION ? 1.5 : 1.0;
    double verticalFactor = currentVerticalDistance < MINIMUM_VERTICAL_SEPARATION ? 1.5 : 1.0;

    return Math.max(horizontalFactor, verticalFactor);
}
```

### 3. 特殊ケースの処理

#### a) 並行飛行（相対速度ほぼゼロ）

```java
if (relativeSpeedMagnitude < EPSILON) {
    // 相対速度がほぼゼロの場合（並行飛行など）
    timeToClosest = Double.POSITIVE_INFINITY; // 接近しない
    // 現在距離ベースの評価のみ
    double parallelFlightRisk = currentRisk * 0.5; // 危険度を50%に調整
}
```

#### b) すれ違い後の処理

```java
if (timeToClosest < 0) {
    // すれ違い減衰係数（時間が経つほど急激に減衰）
    double decayFactor = calculatePostEncounterDecay(timeToClosest,
                                                   currentHorizontalDistance,
                                                   currentVerticalDistance);
    double totalRisk = currentRisk * decayFactor * urgencyFactor;
}
```

## アラートレベル分類

危険度（0-100）に基づき、3段階のアラートレベルに分類されます：

```java
public enum AlertLevel {
    /**
     * 安全 - 管制間隔が確保されている状態
     * 危険度: 0-29
     */
    SAFE("SAFE", 0),

    /**
     * 白コンフリクト - 5分以内に管制間隔欠如の可能性
     * 危険度: 30-69
     */
    WHITE_CONFLICT("WHITE_CONFLICT", 1),

    /**
     * 赤コンフリクト - 1分以内に管制間隔欠如の可能性
     * 危険度: 70-100
     */
    RED_CONFLICT("RED_CONFLICT", 2);
}
```

## 使用方法

### 1. 単一ペアの危険度計算

```java
ConflictDetector detector = new ConflictDetector();

// 2機間の危険度を計算
RiskAssessment assessment = detector.calculateConflictRisk(aircraft1, aircraft2);

// 結果の取得
double riskLevel = assessment.getRiskLevel();           // 0-100の危険度
double timeToClosest = assessment.getTimeToClosest();   // 最接近までの時間（秒）
double horizontalDist = assessment.getClosestHorizontalDistance(); // 最接近時水平距離（海里）
double verticalDist = assessment.getClosestVerticalDistance();     // 最接近時垂直距離（フィート）
AlertLevel alertLevel = assessment.getAlertLevel();     // アラートレベル
boolean isConflict = assessment.isConflictPredicted();  // 管制間隔欠如予測
```

### 2. 全航空機ペアの危険度計算

```java
List<Aircraft> aircraftList = // 航空機リスト
Map<String, RiskAssessment> conflicts = detector.calculateAllConflicts(aircraftList);

// 結果の処理
for (Map.Entry<String, RiskAssessment> entry : conflicts.entrySet()) {
    String pairId = entry.getKey();           // 航空機ペアID（例："JAL123-ANA456"）
    RiskAssessment risk = entry.getValue();   // リスク評価結果

    if (risk.getAlertLevel() == AlertLevel.RED_CONFLICT) {
        // 赤コンフリクトの処理
        System.out.println("緊急アラート: " + pairId +
                          " 危険度=" + risk.getRiskLevel());
    }
}
```

## パフォーマンス最適化

### 1. 事前フィルタリング

計算量O(n²)を軽減するため、明らかに遠い航空機ペアを事前除外：

```java
private List<AircraftPair> preFilterAircraftPairs(List<Aircraft> aircraftList) {
    // 高速近似計算で明らかに遠い航空機を除外
    double approximateDistance = GeodeticUtils.approximateHorizontalDistance(
        aircraft1.getAircraftPosition(), aircraft2.getAircraftPosition());

    if (approximateDistance <= MAX_CONSIDERATION_DISTANCE) {
        candidatePairs.add(new AircraftPair(aircraft1, aircraft2));
    }
}
```

### 2. 並列処理

```java
// 並列ストリームで効率的に処理（200機対応）
candidatePairs.parallelStream().forEach(pair -> {
    RiskAssessment assessment = calculateConflictRisk(pair.aircraft1, pair.aircraft2);
    // 危険度が閾値以上の場合のみ結果に含める
    if (assessment.getRiskLevel() > 0.0) {
        results.put(pairId, assessment);
    }
});
```

### 3. 航空機リポジトリの最適化

システム全体のパフォーマンス向上のため、**AircraftRepositoryInMemory** を大幅に最適化しました：

#### 最適化内容
- **データ構造**: `ArrayList` から `ConcurrentHashMap` への変更
- **検索アルゴリズム**: O(n) 線形検索から O(1) ハッシュ検索
- **スレッドセーフティ**: 並行アクセス対応による安全性向上
- **メモリ効率**: コールサインによる効率的なインデックス管理

#### パフォーマンス向上の成果
- **1000機の検索**: 従来の数百ms → **100ms以下** （約 **90%の高速化**）
- **並行アクセス**: 10スレッド×100機同時追加でもデッドロックなし
- **大規模シナリオ**: 200機以上の大規模シミュレーション対応

#### 最適化の実装例
```java
// 従来のO(n)検索
return aircrafts.stream()
    .filter(aircraft -> aircraft.isEqualCallsign(callsign))
    .findFirst()
    .orElseThrow(() -> new IllegalArgumentException("Aircraft not found"));

// 最適化後のO(1)検索
Aircraft aircraft = aircraftMap.get(callsign.toString());
if (aircraft == null) {
    throw new IllegalArgumentException("Aircraft not found: " + callsign.toString());
}
return aircraft;
```

この最適化により、コンフリクト検出処理全体の応答速度が大幅に向上し、リアルタイムな航空管制支援が可能になりました。

## 管制間隔基準

本システムでは国際民間航空機関（ICAO）の標準管制間隔を使用：

- **水平分離**: 5海里（約9.26km）
- **垂直分離**: 1000フィート（約305m）

管制間隔欠如は、**水平と垂直の両方が同時に基準を下回る場合**にのみ真の違反として判定されます：

```java
public boolean isSeparationViolation() {
    return closestHorizontalDistance < 5.0 && closestVerticalDistance < 1000.0;
}
```

## 技術的制約と仮定

1. **地球曲率**: 局地的な平面近似を使用（精度は数十海里の範囲で十分）
2. **等速直線運動**: 予測期間中の航空機の運動を等速直線と仮定
3. **予測時間範囲**: 5分を上限とした短期予測
4. **数値安定性**: 並行飛行検出用にEPSILON = 0.1 m/sを設定

## 研究・開発への応用

本システムは以下の研究分野に応用可能です：

- **航空管制支援システム**: リアルタイムコンフリクト検出
- **航空交通流管理**: 効率的な経路計画
- **安全性評価**: 航空交通密度と安全性の定量評価
- **機械学習**: 危険度予測モデルの訓練データ生成

システムは最大200機の同時処理に対応しており、現実的な空域での運用に十分な性能を有しています。
