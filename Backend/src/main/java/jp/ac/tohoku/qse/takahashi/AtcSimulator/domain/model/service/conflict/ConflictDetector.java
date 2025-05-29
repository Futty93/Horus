package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.ConflictDetectionException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.InvalidParameterException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
@Service
public class ConflictDetector {

    private static final Logger logger = LoggerFactory.getLogger(ConflictDetector.class);

    // 計算の安定性を保つための微小値（並行飛行検出用に調整）
    private static final double EPSILON = 0.1; // 0.1 m/s ≈ 0.2 knots

    /**
     * 全航空機ペアの危険度を計算
     *
     * @param aircraftList 航空機リスト
     * @return 航空機ペアIDをキーとするリスク評価マップ
     * @throws InvalidParameterException 航空機リストがnullの場合
     */
    public Map<String, RiskAssessment> calculateAllConflicts(List<Aircraft> aircraftList) {
        validateAircraftList(aircraftList);

        if (aircraftList.size() < 2) {
            logger.debug("航空機数が不足しているためコンフリクト計算をスキップ: {}機", aircraftList.size());
            return new HashMap<>();
        }

        logger.debug("コンフリクト計算開始: {}機", aircraftList.size());

        // 並列処理対応のためConcurrentHashMapを使用
        Map<String, RiskAssessment> results = new ConcurrentHashMap<>();

        // 航空機ペアの事前フィルタリングでパフォーマンス向上
        List<AircraftPair> candidatePairs = preFilterAircraftPairs(aircraftList);

        logger.debug("候補ペア数: {}", candidatePairs.size());

        // 並列ストリームで効率的に処理（200機対応）
        candidatePairs.parallelStream().forEach(pair -> {
            try {
                RiskAssessment assessment = calculateConflictRisk(pair.aircraft1, pair.aircraft2);

                // 危険度が閾値以上の場合のみ結果に含める
                if (assessment.getRiskLevel() > 0.0) {
                    String pairId = StringUtils.generatePairId(
                        pair.aircraft1.getCallsign().toString(),
                        pair.aircraft2.getCallsign().toString()
                    );
                    results.put(pairId, assessment);
                }
            } catch (Exception e) {
                String errorMsg = String.format("航空機ペア計算エラー: %s - %s",
                                                pair.aircraft1.getCallsign().toString(),
                                                pair.aircraft2.getCallsign().toString());
                logger.error(errorMsg + " - " + e.getMessage(), e);

                // 個別ペアのエラーは全体の処理を停止させない
                // ただし、システムエラーとして記録する
            }
        });

        logger.debug("コンフリクト計算完了: {}件のコンフリクトを検出", results.size());
        return results;
    }

    /**
     * 2機の航空機間のコンフリクトリスクを計算
     *
     * @param aircraft1 航空機1
     * @param aircraft2 航空機2
     * @return リスク評価結果
     * @throws InvalidParameterException 航空機がnullの場合
     * @throws ConflictDetectionException 計算処理中にエラーが発生した場合
     */
    public RiskAssessment calculateConflictRisk(Aircraft aircraft1, Aircraft aircraft2) {
        validateAircraftPair(aircraft1, aircraft2);

        try {
            // CPA分析による最接近点計算
            CPAResult cpaResult = calculateCPA(aircraft1, aircraft2);

            // 危険度評価の実行
            double riskLevel = assessRiskLevel(cpaResult);
            boolean isConflictPredicted = predictSeparationViolation(cpaResult);

            return new RiskAssessment(
                riskLevel,
                cpaResult.timeToClosest,
                cpaResult.horizontalDistance,
                cpaResult.verticalDistance,
                isConflictPredicted
            );
        } catch (Exception e) {
            String errorMsg = String.format("コンフリクトリスク計算エラー: %s vs %s",
                                            aircraft1.getCallsign().toString(),
                                            aircraft2.getCallsign().toString());
            throw new ConflictDetectionException(errorMsg, e);
        }
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
    private CPAResult calculateCPA(Aircraft aircraft1, Aircraft aircraft2) {
        // 現在位置と速度ベクトルを取得
        AircraftPosition pos1 = aircraft1.getAircraftPosition();
        AircraftPosition pos2 = aircraft2.getAircraftPosition();
        AircraftVector vec1 = aircraft1.getAircraftVector();
        AircraftVector vec2 = aircraft2.getAircraftVector();

        // 相対位置ベクトル（球面座標系での近似計算）
        double[] relativePosition = calculateRelativePosition(pos1, pos2);

        // 相対速度ベクトル（水平:メートル/秒、垂直:フィート/秒）
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
            // 距離は一定のため、現在距離をそのまま使用
            timeToClosest = Double.POSITIVE_INFINITY; // 接近しない
            closestHorizontalDistance = Math.sqrt(
                relativePosition[0] * relativePosition[0] +
                relativePosition[1] * relativePosition[1]
            ) / 1000.0 / NAUTICAL_MILES_TO_KM; // メートルから海里に変換
            closestVerticalDistance = Math.abs(relativePosition[2]); // フィート単位
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
            ) / 1000.0 / NAUTICAL_MILES_TO_KM; // メートルから海里に変換

            closestVerticalDistance = Math.abs(closestRelativePosition[2]); // フィート単位
        }

        // 予測時間範囲の制限（並行飛行の場合は制限なし）
        if (timeToClosest != Double.POSITIVE_INFINITY && timeToClosest > MAX_PREDICTION_TIME) {
            timeToClosest = MAX_PREDICTION_TIME;
        }

        return new CPAResult(timeToClosest, closestHorizontalDistance, closestVerticalDistance);
    }

    /**
     * 危険度計算（時間重み付け・距離評価の組み合わせ）
     */
    private double calculateRiskLevel(double timeToClosest, double closestHorizontalDistance,
                                    double closestVerticalDistance, double currentHorizontalDistance,
                                    double currentVerticalDistance) {

        // 現在の距離による基本危険度評価
        double currentHorizontalRisk = calculateHorizontalRisk(currentHorizontalDistance);
        double currentVerticalRisk = calculateVerticalRisk(currentVerticalDistance);

        // 垂直分離が十分な場合は水平リスクを軽減
        if (currentVerticalDistance >= MINIMUM_VERTICAL_SEPARATION) {
            currentHorizontalRisk *= 0.3; // 垂直分離が確保されている場合は水平リスクを70%軽減
        }

        double currentRisk = Math.max(currentHorizontalRisk, currentVerticalRisk);

        // 並行飛行（相対速度ほぼゼロ）の場合
        if (timeToClosest == Double.POSITIVE_INFINITY) {
            // 現在距離ベースの危険度のみを使用（時間要素なし）
            // 並行飛行で十分な距離があれば安全、近い場合は適度な危険度
            double parallelFlightRisk = currentRisk * 0.5; // 並行飛行は危険度を50%に調整
            return Math.min(100.0, Math.max(0.0, parallelFlightRisk * 100));
        }

        // 予測計算（timeToClosest が有限値の場合）

        // 時間重み係数の計算
        double timeWeight = calculateTimeWeight(timeToClosest);

        // 予測位置での危険度評価
        double predictedHorizontalRisk = calculateHorizontalRisk(closestHorizontalDistance);
        double predictedVerticalRisk = calculateVerticalRisk(closestVerticalDistance);

        // 垂直分離が十分な場合は水平リスクを軽減
        if (closestVerticalDistance >= MINIMUM_VERTICAL_SEPARATION) {
            predictedHorizontalRisk *= 0.3; // 垂直分離が確保されている場合は水平リスクを70%軽減
        }

        double predictedRisk = Math.max(predictedHorizontalRisk, predictedVerticalRisk);

        // 現在距離による緊急度補正
        double urgencyFactor = calculateUrgencyFactor(currentHorizontalDistance, currentVerticalDistance);

        // すれ違った後の処理（timeToClosest < 0）
        if (timeToClosest < 0) {
            // すれ違い減衰係数（時間が経つほど急激に減衰）
            double decayFactor = calculatePostEncounterDecay(timeToClosest, currentHorizontalDistance, currentVerticalDistance);

            // すれ違い後の危険度 = 現在距離リスク × 減衰係数 × 緊急度補正
            double totalRisk = currentRisk * decayFactor * urgencyFactor;
            return Math.min(100.0, Math.max(0.0, totalRisk * 100));
        }

        // 通常の予測計算（timeToClosest >= 0）

        // 現在リスクと予測リスクの重み付け組み合わせ
        // 現在距離も考慮に入れることで、より現実的な評価を行う
        double combinedRisk = Math.max(
            currentRisk * 0.4,  // 現在距離の重み（40%）
            predictedRisk * timeWeight  // 予測距離の重み（時間減衰あり）
        );

        double totalRisk = combinedRisk * urgencyFactor;

        // 0-100の範囲に正規化
        return Math.min(100.0, Math.max(0.0, totalRisk * 100));
    }

    /**
     * 時間重み係数の計算
     * 1分以内=1.0, 5分=0.2, 5分超=0.0
     * 並行飛行（無限大）は別処理
     */
    private double calculateTimeWeight(double timeToClosest) {
        if (timeToClosest == Double.POSITIVE_INFINITY) {
            // 並行飛行：時間要素なし
            return 0.0;
        } else if (timeToClosest < 0) {
            // すれ違った後は別の処理で計算するため、ここでは0を返す
            return 0.0;
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
     * CPA結果に基づくリスク評価
     */
    private double assessRiskLevel(CPAResult cpaResult) {
        // 既存のcalculateRiskLevelメソッドを使用
        return calculateRiskLevel(
            cpaResult.timeToClosest,
            cpaResult.horizontalDistance,
            cpaResult.verticalDistance,
            cpaResult.horizontalDistance, // 現在距離として使用
            cpaResult.verticalDistance   // 現在距離として使用
        );
    }

    /**
     * CPA結果に基づく管制間隔欠如予測
     */
    private boolean predictSeparationViolation(CPAResult cpaResult) {
        if (cpaResult.timeToClosest < 0 || cpaResult.timeToClosest == Double.POSITIVE_INFINITY) {
            // すれ違った後や並行飛行は現在距離での判定は行わない
            return false;
        }

        // 予測時間範囲内かつ管制間隔基準を下回る場合
        // 航空管制では水平と垂直の両方が同時に不足している場合のみ真の管制間隔違反
        return cpaResult.timeToClosest >= 0 && cpaResult.timeToClosest <= MAX_PREDICTION_TIME &&
               (cpaResult.horizontalDistance < MINIMUM_HORIZONTAL_SEPARATION &&
                cpaResult.verticalDistance < MINIMUM_VERTICAL_SEPARATION);
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
     * 相対位置ベクトルの計算（メートル単位）
     */
    private double[] calculateRelativePosition(AircraftPosition pos1, AircraftPosition pos2) {
        // 緯度・経度差をキロメートルに変換
        double deltaLatKm = (pos2.latitude.toDouble() - pos1.latitude.toDouble()) * EARTH_RADIUS * DEGREES_TO_RADIANS;

        // 経度差は平均緯度で補正
        double avgLatRad = Math.toRadians((pos1.latitude.toDouble() + pos2.latitude.toDouble()) / 2);
        double deltaLonKm = (pos2.longitude.toDouble() - pos1.longitude.toDouble()) *
                           EARTH_RADIUS * DEGREES_TO_RADIANS * Math.cos(avgLatRad);

        // 高度差はフィート単位で保持
        double deltaAltFt = pos2.altitude.toDouble() - pos1.altitude.toDouble();

        return new double[]{deltaLonKm * 1000, deltaLatKm * 1000, deltaAltFt}; // x,y:メートル, z:フィート
    }

    /**
     * 相対速度ベクトルの計算（水平:メートル/秒、垂直:フィート/秒）
     */
    private double[] calculateRelativeVelocity(AircraftVector vec1, AircraftVector vec2) {
        // 速度ベクトルをメートル/秒に変換
        double v1x = vec1.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3.6 *
                    Math.sin(Math.toRadians(vec1.heading.toDouble()));
        double v1y = vec1.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3.6 *
                    Math.cos(Math.toRadians(vec1.heading.toDouble()));
        double v1z = vec1.verticalSpeed.toDouble() / 60.0; // ft/min to ft/s

        double v2x = vec2.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3.6 *
                    Math.sin(Math.toRadians(vec2.heading.toDouble()));
        double v2y = vec2.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3.6 *
                    Math.cos(Math.toRadians(vec2.heading.toDouble()));
        double v2z = vec2.verticalSpeed.toDouble() / 60.0;

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

    /**
     * 航空機リストの妥当性を検証
     */
    private void validateAircraftList(List<Aircraft> aircraftList) {
        if (aircraftList == null) {
            throw new InvalidParameterException("aircraftList", null, "航空機リストがnullです");
        }
    }

    /**
     * 航空機ペアの妥当性を検証
     */
    private void validateAircraftPair(Aircraft aircraft1, Aircraft aircraft2) {
        if (aircraft1 == null) {
            throw new InvalidParameterException("aircraft1", null, "航空機1がnullです");
        }
        if (aircraft2 == null) {
            throw new InvalidParameterException("aircraft2", null, "航空機2がnullです");
        }
    }

    /**
     * CPA計算結果を格納する内部クラス
     */
    private static class CPAResult {
        final double timeToClosest;
        final double horizontalDistance;
        final double verticalDistance;

        CPAResult(double timeToClosest, double horizontalDistance, double verticalDistance) {
            this.timeToClosest = timeToClosest;
            this.horizontalDistance = horizontalDistance;
            this.verticalDistance = verticalDistance;
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

    /**
     * すれ違い後の危険度減衰係数計算
     * 航空機の特性上、すれ違った後は距離が急激に増加するため危険度も急激に低下する
     */
    private double calculatePostEncounterDecay(double timeToClosest, double currentHorizontalDistance, double currentVerticalDistance) {
        // timeToClosest は負の値（すれ違ってからの経過時間の絶対値）
        double timeSinceEncounter = Math.abs(timeToClosest);

        // 基本減衰（時間ベース：15秒で50%、30秒で10%、60秒で1%）
        double timeDecay;
        if (timeSinceEncounter <= 15) {
            // 15秒以内：線形減衰（100% → 50%）
            timeDecay = 1.0 - 0.5 * (timeSinceEncounter / 15.0);
        } else if (timeSinceEncounter <= 30) {
            // 15-30秒：急激な減衰（50% → 10%）
            timeDecay = 0.5 - 0.4 * ((timeSinceEncounter - 15) / 15.0);
        } else if (timeSinceEncounter <= 60) {
            // 30-60秒：さらなる減衰（10% → 1%）
            timeDecay = 0.1 - 0.09 * ((timeSinceEncounter - 30) / 30.0);
        } else {
            // 60秒超：ほぼゼロ
            timeDecay = 0.01;
        }

        // 距離ベース減衰係数
        double distanceDecay = calculateDistanceDecay(currentHorizontalDistance, currentVerticalDistance);

        // 最終減衰係数 = min(時間減衰, 距離減衰)
        return Math.min(timeDecay, distanceDecay);
    }

    /**
     * 距離ベース減衰係数計算
     * 現在の距離が安全な距離になるほど危険度を下げる
     */
    private double calculateDistanceDecay(double currentHorizontalDistance, double currentVerticalDistance) {
        // 水平距離による減衰
        double horizontalDecay;
        if (currentHorizontalDistance >= MINIMUM_HORIZONTAL_SEPARATION * 1.5) {
            // 7.5海里以上：安全
            horizontalDecay = 0.0;
        } else if (currentHorizontalDistance >= MINIMUM_HORIZONTAL_SEPARATION) {
            // 5-7.5海里：線形減衰
            horizontalDecay = 1.0 - 2.0 * (currentHorizontalDistance - MINIMUM_HORIZONTAL_SEPARATION) / MINIMUM_HORIZONTAL_SEPARATION;
        } else {
            // 5海里未満：高い危険度維持（ただし減衰は適用）
            horizontalDecay = 1.0;
        }

        // 垂直距離による減衰
        double verticalDecay;
        if (currentVerticalDistance >= MINIMUM_VERTICAL_SEPARATION * 1.5) {
            // 1500フィート以上：安全
            verticalDecay = 0.0;
        } else if (currentVerticalDistance >= MINIMUM_VERTICAL_SEPARATION) {
            // 1000-1500フィート：線形減衰
            verticalDecay = 1.0 - 2.0 * (currentVerticalDistance - MINIMUM_VERTICAL_SEPARATION) / MINIMUM_VERTICAL_SEPARATION;
        } else {
            // 1000フィート未満：高い危険度維持（ただし減衰は適用）
            verticalDecay = 1.0;
        }

        // より厳しい条件（高い減衰）を採用
        return Math.max(horizontalDecay, verticalDecay);
    }
}
