package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict;

import java.util.Objects;

/**
 * 航空機間のコンフリクトリスク評価結果を表す値オブジェクト
 * CPA（Closest Point of Approach）分析による危険度評価情報を格納
 */
public class RiskAssessment {

    /**
     * 危険度レベル（0-100）
     * 0: 完全に安全, 100: 極めて危険
     */
    private final double riskLevel;

    /**
     * 最接近までの時間（秒）
     * 負の値は既に最接近点を通過済みを示す
     */
    private final double timeToClosest;

    /**
     * 最接近時の水平距離（海里）
     */
    private final double closestHorizontalDistance;

    /**
     * 最接近時の垂直距離（フィート）
     */
    private final double closestVerticalDistance;

    /**
     * 管制間隔欠如予測フラグ
     * true: 管制間隔（水平5海里または垂直1000フィート）を下回る予測
     */
    private final boolean isConflictPredicted;

    /**
     * アラートレベル
     */
    private final AlertLevel alertLevel;

    /**
     * コンストラクタ
     *
     * @param riskLevel 危険度レベル（0-100）
     * @param timeToClosest 最接近までの時間（秒）
     * @param closestHorizontalDistance 最接近時の水平距離（海里）
     * @param closestVerticalDistance 最接近時の垂直距離（フィート）
     * @param isConflictPredicted 管制間隔欠如予測フラグ
     * @throws IllegalArgumentException 無効な値が渡された場合
     */
    public RiskAssessment(double riskLevel, double timeToClosest,
                         double closestHorizontalDistance, double closestVerticalDistance,
                         boolean isConflictPredicted) {
        validateRiskLevel(riskLevel);
        validateDistance(closestHorizontalDistance, "水平距離");
        validateDistance(closestVerticalDistance, "垂直距離");

        this.riskLevel = riskLevel;
        this.timeToClosest = timeToClosest;
        this.closestHorizontalDistance = closestHorizontalDistance;
        this.closestVerticalDistance = closestVerticalDistance;
        this.isConflictPredicted = isConflictPredicted;
        this.alertLevel = AlertLevel.fromRiskLevel(riskLevel);
    }

    /**
     * 危険度の妥当性を検証
     */
    private void validateRiskLevel(double riskLevel) {
        if (riskLevel < 0.0 || riskLevel > 100.0) {
            throw new IllegalArgumentException("危険度は0-100の範囲で指定してください: " + riskLevel);
        }
    }

    /**
     * 距離の妥当性を検証
     */
    private void validateDistance(double distance, String type) {
        if (distance < 0.0) {
            throw new IllegalArgumentException(type + "は負の値にできません: " + distance);
        }
    }

    // Getters
    public double getRiskLevel() {
        return riskLevel;
    }

    public double getTimeToClosest() {
        return timeToClosest;
    }

    public double getClosestHorizontalDistance() {
        return closestHorizontalDistance;
    }

    public double getClosestVerticalDistance() {
        return closestVerticalDistance;
    }

    public boolean isConflictPredicted() {
        return isConflictPredicted;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    /**
     * 管制間隔基準に基づく危険性判定
     *
     * @return 水平5海里未満または垂直1000フィート未満の場合true
     */
    public boolean isSeparationViolation() {
        return closestHorizontalDistance < 5.0 || closestVerticalDistance < 1000.0;
    }

    /**
     * 緊急性の判定（1分以内の接近）
     *
     * @return 最接近まで60秒以内の場合true
     */
    public boolean isUrgent() {
        return timeToClosest >= 0 && timeToClosest <= 60.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskAssessment that = (RiskAssessment) o;
        return Double.compare(that.riskLevel, riskLevel) == 0 &&
               Double.compare(that.timeToClosest, timeToClosest) == 0 &&
               Double.compare(that.closestHorizontalDistance, closestHorizontalDistance) == 0 &&
               Double.compare(that.closestVerticalDistance, closestVerticalDistance) == 0 &&
               isConflictPredicted == that.isConflictPredicted &&
               alertLevel == that.alertLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(riskLevel, timeToClosest, closestHorizontalDistance,
                           closestVerticalDistance, isConflictPredicted, alertLevel);
    }

    @Override
    public String toString() {
        return String.format(
            "RiskAssessment{riskLevel=%.2f, timeToClosest=%.2f, " +
            "horizontalDistance=%.2f NM, verticalDistance=%.0f ft, " +
            "conflictPredicted=%s, alertLevel=%s}",
            riskLevel, timeToClosest, closestHorizontalDistance,
            closestVerticalDistance, isConflictPredicted, alertLevel
        );
    }
}
