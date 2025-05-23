package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * 航空機間のコンフリクト検出を行うドメインサービス
 * CPA（Closest Point of Approach）分析を基盤とした危険度評価を実装
 *
 * 主な機能:
 * - 2機間の詳細なリスク計算
 * - 全航空機ペアの効率的な危険度評価
 * - アラートレベルの判定
 * - パフォーマンス最適化（最大200機対応）
 */
public class ConflictDetector {

    // 計算の安定性を保つための微小値
    private static final double EPSILON = 1e-9;

    /**
     * 全航空機ペアの危険度を計算
     *
     * @param aircraftList 航空機リスト
     * @return 航空機ペアIDをキーとするリスク評価マップ
     * @throws IllegalArgumentException 航空機リストがnullまたは空の場合
     */
    public Map<String, RiskAssessment> calculateAllConflicts(List<Aircraft> aircraftList) {
        validateAircraftList(aircraftList);

        if (aircraftList.size() < 2) {
            return new HashMap<>();
        }

        // 並列処理対応のためConcurrentHashMapを使用
        Map<String, RiskAssessment> results = new ConcurrentHashMap<>();

        // 航空機ペアの事前フィルタリングでパフォーマンス向上
        List<AircraftPair> candidatePairs = preFilterAircraftPairs(aircraftList);

        // 並列ストリームで効率的に処理（200機対応）
        candidatePairs.parallelStream().forEach(pair -> {
            try {
                RiskAssessment assessment = calculateConflictRisk(pair.aircraft1, pair.aircraft2);

                // 危険度が閾値以上の場合のみ結果に含める
                if (assessment.getRiskLevel() > 0.0) {
                    String pairId = generatePairId(pair.aircraft1, pair.aircraft2);
                    results.put(pairId, assessment);
                }
            } catch (Exception e) {
                // ログ出力（実装環境に応じて適切なロガーを使用）
                System.err.println("航空機ペア計算エラー: " + e.getMessage());
            }
        });

        return results;
    }

    /**
     * 2機間の危険度を詳細計算
     *
     * @param aircraft1 航空機1
     * @param aircraft2 航空機2
     * @return リスク評価結果
     * @throws IllegalArgumentException 航空機がnullの場合
     */
    public RiskAssessment calculateConflictRisk(Aircraft aircraft1, Aircraft aircraft2) {
        validateAircraft(aircraft1, "aircraft1");
        validateAircraft(aircraft2, "aircraft2");

        // 現在位置と速度ベクトルを取得
        AircraftPosition pos1 = aircraft1.getAircraftPosition();
        AircraftPosition pos2 = aircraft2.getAircraftPosition();
        AircraftVector vec1 = aircraft1.getAircraftVector();
        AircraftVector vec2 = aircraft2.getAircraftVector();

        // 現在の距離をチェック
        double currentHorizontalDistance = GeodeticUtils.calculateHorizontalDistance(pos1, pos2);
        double currentVerticalDistance = GeodeticUtils.calculateVerticalDistance(pos1, pos2);

        // CPA計算による最接近点分析
        CPAResult cpaResult = calculateCPA(pos1, vec1, pos2, vec2);

        // 危険度計算
        double riskLevel = calculateRiskLevel(
            cpaResult.timeToClosest,
            cpaResult.closestHorizontalDistance,
            cpaResult.closestVerticalDistance,
            currentHorizontalDistance,
            currentVerticalDistance
        );

        // 管制間隔欠如予測
        boolean isConflictPredicted = predictSeparationViolation(
            cpaResult.closestHorizontalDistance,
            cpaResult.closestVerticalDistance,
            cpaResult.timeToClosest
        );

        return new RiskAssessment(
            riskLevel,
            cpaResult.timeToClosest,
            cpaResult.closestHorizontalDistance,
            cpaResult.closestVerticalDistance,
            isConflictPredicted
        );
    }

    /**
     * アラートレベルの判定
     *
     * @param riskLevel 危険度レベル（0-100）
     * @return 対応するアラートレベル
     */
    public AlertLevel determineAlertLevel(double riskLevel) {
        return AlertLevel.fromRiskLevel(riskLevel);
    }

    /**
     * CPA（Closest Point of Approach）計算
     * 3次元空間での最接近点を数学的に計算
     */
    private CPAResult calculateCPA(AircraftPosition pos1, AircraftVector vec1,
                                  AircraftPosition pos2, AircraftVector vec2) {

        // 相対位置ベクトル（球面座標系での近似計算）
        double[] relativePosition = calculateRelativePosition(pos1, pos2);

        // 相対速度ベクトル（地上速度を考慮）
        double[] relativeVelocity = calculateRelativeVelocity(vec1, vec2);

        // 相対速度の大きさ
        double relativeSpeedMagnitude = Math.sqrt(
            relativeVelocity[0] * relativeVelocity[0] +
            relativeVelocity[1] * relativeVelocity[1] +
            relativeVelocity[2] * relativeVelocity[2]
        );

        double timeToClosest;
        double closestHorizontalDistance;
        double closestVerticalDistance;

        if (relativeSpeedMagnitude < EPSILON) {
            // 相対速度がほぼゼロの場合（並行飛行など）
            timeToClosest = 0.0;
            closestHorizontalDistance = Math.sqrt(
                relativePosition[0] * relativePosition[0] +
                relativePosition[1] * relativePosition[1]
            ) / NAUTICAL_MILES_TO_KM;
            closestVerticalDistance = Math.abs(relativePosition[2]) / FEET_TO_METERS;
        } else {
            // 最接近時刻を計算: t = -(r·v) / |v|²
            double dotProduct = -(relativePosition[0] * relativeVelocity[0] +
                                 relativePosition[1] * relativeVelocity[1] +
                                 relativePosition[2] * relativeVelocity[2]);

            timeToClosest = dotProduct / (relativeSpeedMagnitude * relativeSpeedMagnitude);

            // 最接近時の位置計算: r_closest = r + t * v
            double[] closestRelativePosition = {
                relativePosition[0] + timeToClosest * relativeVelocity[0],
                relativePosition[1] + timeToClosest * relativeVelocity[1],
                relativePosition[2] + timeToClosest * relativeVelocity[2]
            };

            // 水平距離と垂直距離を計算
            closestHorizontalDistance = Math.sqrt(
                closestRelativePosition[0] * closestRelativePosition[0] +
                closestRelativePosition[1] * closestRelativePosition[1]
            ) / NAUTICAL_MILES_TO_KM;

            closestVerticalDistance = Math.abs(closestRelativePosition[2]) / FEET_TO_METERS;
        }

        // 予測時間範囲の制限
        if (timeToClosest < 0 || timeToClosest > MAX_PREDICTION_TIME) {
            timeToClosest = Math.max(0, Math.min(timeToClosest, MAX_PREDICTION_TIME));
        }

        return new CPAResult(timeToClosest, closestHorizontalDistance, closestVerticalDistance);
    }

    /**
     * 危険度計算（時間重み付け・距離評価の組み合わせ）
     */
    private double calculateRiskLevel(double timeToClosest, double closestHorizontalDistance,
                                    double closestVerticalDistance, double currentHorizontalDistance,
                                    double currentVerticalDistance) {

        // 時間重み係数の計算
        double timeWeight = calculateTimeWeight(timeToClosest);

        // 水平リスク評価
        double horizontalRisk = calculateHorizontalRisk(closestHorizontalDistance);

        // 垂直リスク評価
        double verticalRisk = calculateVerticalRisk(closestVerticalDistance);

        // 現在距離による緊急度補正
        double urgencyFactor = calculateUrgencyFactor(currentHorizontalDistance, currentVerticalDistance);

        // 総合危険度 = max(水平リスク, 垂直リスク) × 時間重み × 緊急度補正
        double baseRisk = Math.max(horizontalRisk, verticalRisk);
        double totalRisk = baseRisk * timeWeight * urgencyFactor;

        // 0-100の範囲に正規化
        return Math.min(100.0, Math.max(0.0, totalRisk * 100));
    }

    /**
     * 時間重み係数の計算
     * 1分以内=1.0, 5分=0.2, 5分超=0.0
     */
    private double calculateTimeWeight(double timeToClosest) {
        if (timeToClosest <= 0) {
            return 0.0; // 既に過ぎた時点
        } else if (timeToClosest <= 60) {
            return 1.0; // 1分以内
        } else if (timeToClosest <= 300) {
            // 1分〜5分：線形減衰
            return 1.0 - 0.8 * (timeToClosest - 60) / 240;
        } else {
            return 0.0; // 5分超
        }
    }

    /**
     * 水平リスク評価（5海里基準）
     */
    private double calculateHorizontalRisk(double horizontalDistance) {
        if (horizontalDistance >= MINIMUM_HORIZONTAL_SEPARATION) {
            return 0.0; // 安全
        } else if (horizontalDistance >= MINIMUM_HORIZONTAL_SEPARATION * 0.6) {
            // 3-5海里：段階的増加
            return 0.4 * (MINIMUM_HORIZONTAL_SEPARATION - horizontalDistance) / (MINIMUM_HORIZONTAL_SEPARATION * 0.4);
        } else {
            // 3海里未満：急激な増加
            double factor = (MINIMUM_HORIZONTAL_SEPARATION * 0.6 - horizontalDistance) / (MINIMUM_HORIZONTAL_SEPARATION * 0.6);
            return 0.4 + 0.6 * factor * factor;
        }
    }

    /**
     * 垂直リスク評価（1000フィート基準）
     */
    private double calculateVerticalRisk(double verticalDistance) {
        if (verticalDistance >= MINIMUM_VERTICAL_SEPARATION) {
            return 0.0; // 安全
        } else if (verticalDistance >= MINIMUM_VERTICAL_SEPARATION * 0.5) {
            // 500-1000フィート：段階的増加
            return 0.5 * (MINIMUM_VERTICAL_SEPARATION - verticalDistance) / (MINIMUM_VERTICAL_SEPARATION * 0.5);
        } else {
            // 500フィート未満：急激な増加
            double factor = (MINIMUM_VERTICAL_SEPARATION * 0.5 - verticalDistance) / (MINIMUM_VERTICAL_SEPARATION * 0.5);
            return 0.5 + 0.5 * factor * factor;
        }
    }

    /**
     * 現在距離による緊急度補正
     */
    private double calculateUrgencyFactor(double currentHorizontalDistance, double currentVerticalDistance) {
        // 既に管制間隔を下回っている場合の緊急度補正
        double horizontalFactor = currentHorizontalDistance < MINIMUM_HORIZONTAL_SEPARATION ? 1.5 : 1.0;
        double verticalFactor = currentVerticalDistance < MINIMUM_VERTICAL_SEPARATION ? 1.5 : 1.0;

        return Math.max(horizontalFactor, verticalFactor);
    }

    /**
     * 管制間隔欠如の予測
     */
    private boolean predictSeparationViolation(double closestHorizontalDistance,
                                             double closestVerticalDistance,
                                             double timeToClosest) {
        // 予測時間範囲内かつ管制間隔基準を下回る場合
        return timeToClosest >= 0 && timeToClosest <= MAX_PREDICTION_TIME &&
               (closestHorizontalDistance < MINIMUM_HORIZONTAL_SEPARATION ||
                closestVerticalDistance < MINIMUM_VERTICAL_SEPARATION);
    }

    /**
     * 航空機ペアの事前フィルタリング（パフォーマンス最適化）
     */
    private List<AircraftPair> preFilterAircraftPairs(List<Aircraft> aircraftList) {
        List<AircraftPair> candidatePairs = new ArrayList<>();

        for (int i = 0; i < aircraftList.size(); i++) {
            for (int j = i + 1; j < aircraftList.size(); j++) {
                Aircraft aircraft1 = aircraftList.get(i);
                Aircraft aircraft2 = aircraftList.get(j);

                // 高速近似計算で明らかに遠い航空機を除外
                double approximateDistance = GeodeticUtils.approximateHorizontalDistance(
                    aircraft1.getAircraftPosition(),
                    aircraft2.getAircraftPosition()
                );

                if (approximateDistance <= MAX_CONSIDERATION_DISTANCE) {
                    candidatePairs.add(new AircraftPair(aircraft1, aircraft2));
                }
            }
        }

        return candidatePairs;
    }

    /**
     * 相対位置ベクトルの計算（キロメートル単位）
     */
    private double[] calculateRelativePosition(AircraftPosition pos1, AircraftPosition pos2) {
        // 緯度・経度差をキロメートルに変換
        double deltaLatKm = (pos2.latitude.toDouble() - pos1.latitude.toDouble()) * EARTH_RADIUS * DEGREES_TO_RADIANS;

        // 経度差は平均緯度で補正
        double avgLatRad = Math.toRadians((pos1.latitude.toDouble() + pos2.latitude.toDouble()) / 2);
        double deltaLonKm = (pos2.longitude.toDouble() - pos1.longitude.toDouble()) *
                           EARTH_RADIUS * DEGREES_TO_RADIANS * Math.cos(avgLatRad);

        // 高度差はメートルに変換
        double deltaAltM = (pos2.altitude.toDouble() - pos1.altitude.toDouble()) * FEET_TO_METERS;

        return new double[]{deltaLonKm * 1000, deltaLatKm * 1000, deltaAltM}; // メートル単位
    }

    /**
     * 相対速度ベクトルの計算（メートル/秒単位）
     */
    private double[] calculateRelativeVelocity(AircraftVector vec1, AircraftVector vec2) {
        // 速度ベクトルをメートル/秒に変換
        double v1x = vec1.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3.6 *
                    Math.sin(Math.toRadians(vec1.heading.toDouble()));
        double v1y = vec1.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3.6 *
                    Math.cos(Math.toRadians(vec1.heading.toDouble()));
        double v1z = vec1.verticalSpeed.toDouble() * FEET_TO_METERS / 60.0; // ft/min to m/s

        double v2x = vec2.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3.6 *
                    Math.sin(Math.toRadians(vec2.heading.toDouble()));
        double v2y = vec2.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3.6 *
                    Math.cos(Math.toRadians(vec2.heading.toDouble()));
        double v2z = vec2.verticalSpeed.toDouble() * FEET_TO_METERS / 60.0;

        return new double[]{v2x - v1x, v2y - v1y, v2z - v1z};
    }

    /**
     * 航空機ペアIDの生成
     */
    private String generatePairId(Aircraft aircraft1, Aircraft aircraft2) {
        String callsign1 = aircraft1.getCallsign().toString();
        String callsign2 = aircraft2.getCallsign().toString();

        // アルファベット順でソートして一意性を保証
        if (callsign1.compareTo(callsign2) < 0) {
            return callsign1 + "-" + callsign2;
        } else {
            return callsign2 + "-" + callsign1;
        }
    }

    // バリデーションメソッド
    private void validateAircraftList(List<Aircraft> aircraftList) {
        if (aircraftList == null) {
            throw new IllegalArgumentException("航空機リストがnullです");
        }
    }

    private void validateAircraft(Aircraft aircraft, String paramName) {
        if (aircraft == null) {
            throw new IllegalArgumentException(paramName + "がnullです");
        }
    }

    /**
     * CPA計算結果を格納する内部クラス
     */
    private static class CPAResult {
        final double timeToClosest;
        final double closestHorizontalDistance;
        final double closestVerticalDistance;

        CPAResult(double timeToClosest, double closestHorizontalDistance, double closestVerticalDistance) {
            this.timeToClosest = timeToClosest;
            this.closestHorizontalDistance = closestHorizontalDistance;
            this.closestVerticalDistance = closestVerticalDistance;
        }
    }

    /**
     * 航空機ペアを表現する内部クラス
     */
    private static class AircraftPair {
        final Aircraft aircraft1;
        final Aircraft aircraft2;

        AircraftPair(Aircraft aircraft1, Aircraft aircraft2) {
            this.aircraft1 = aircraft1;
            this.aircraft2 = aircraft2;
        }
    }
}
