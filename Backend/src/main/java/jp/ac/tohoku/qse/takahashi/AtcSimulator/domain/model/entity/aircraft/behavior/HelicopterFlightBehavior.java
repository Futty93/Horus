package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.MathUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PositionUtils;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * ヘリコプターの飛行動作実装
 * ホバリング、垂直上昇、その場回転などの特殊能力を含む
 */
public class HelicopterFlightBehavior implements FlightBehavior {

    private static final double HOVERING_THRESHOLD = 5.0; // ホバリング判定の閾値（ノット）

    @Override
    public AircraftPosition calculateNextPosition(AircraftPosition currentPos, AircraftVector vector, double refreshRate) {
        double refreshRateInSeconds = 1.0 / refreshRate;

        // ホバリング状態の判定
        if (vector.groundSpeed.toDouble() < HOVERING_THRESHOLD) {
            // 空中停止：水平位置は変更せず、垂直移動のみ
            Altitude newAlt = new Altitude(currentPos.altitude.toDouble() +
                (vector.verticalSpeed.toDouble() * refreshRateInSeconds / 60.0));

            return new AircraftPosition(currentPos.latitude, currentPos.longitude, newAlt);
        }

        // 通常の移動：共通ユーティリティを使用
        return PositionUtils.calculateNextPosition(
            currentPos,
            vector.groundSpeed.toDouble(),
            vector.heading.toDouble(),
            vector.verticalSpeed.toDouble(),
            refreshRateInSeconds
        );
    }

    @Override
    public Heading calculateNextHeading(double currentHeading, double targetHeading, double maxTurnRate) {
        // ヘリコプターはより急激な旋回が可能
        double helicopterTurnRate = maxTurnRate * 1.5;
        return PositionUtils.calculateNextHeading(currentHeading, targetHeading, helicopterTurnRate);
    }

    @Override
    public GroundSpeed calculateNextGroundSpeed(double currentGroundSpeed, double targetGroundSpeed, double maxAcceleration) {
        // ヘリコプターは急激な加減速が可能
        double helicopterAcceleration = maxAcceleration * 1.2;
        return PositionUtils.calculateNextGroundSpeed(currentGroundSpeed, targetGroundSpeed, helicopterAcceleration);
    }

    @Override
    public VerticalSpeed calculateNextVerticalSpeed(double currentAltitude, double targetAltitude, double maxClimbRate, double refreshRate) {
        // ヘリコプターは垂直上昇/降下が得意
        double helicopterClimbRate = maxClimbRate * 1.8;
        return PositionUtils.calculateNextVerticalSpeed(currentAltitude, targetAltitude, helicopterClimbRate, refreshRate);
    }

    @Override
    public double calculateTurnAngle(AircraftPosition currentPos, double currentHeading, FixPosition targetFix) {
        // ヘリコプターはその場回転が可能なため、直接的な角度計算
        return PositionUtils.calculateBearingToTarget(
            currentPos,
            targetFix.latitude.toDouble(),
            targetFix.longitude.toDouble()
        );
    }

    /**
     * ホバリング状態かどうかを判定
     */
    public boolean isHovering(AircraftVector vector) {
        return vector.groundSpeed.toDouble() < HOVERING_THRESHOLD;
    }
}
