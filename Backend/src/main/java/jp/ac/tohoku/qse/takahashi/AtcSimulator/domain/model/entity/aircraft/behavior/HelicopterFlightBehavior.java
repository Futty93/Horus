package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * ヘリコプターの飛行動作実装
 * 空中停止、垂直上昇/降下、その場回転などのヘリコプター特有の飛行特性を実装
 */
public class HelicopterFlightBehavior implements FlightBehavior {

    /** ホバリング判定の最小速度（knots） */
    private static final double HOVERING_THRESHOLD = 5.0;

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

        // 通常の移動：固定翼機と同様の計算だが、より急激な機動が可能
        double groundSpeedKmPerHour = vector.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR;
        double distanceTraveled = groundSpeedKmPerHour * (refreshRateInSeconds / 3600.0);

        double headingRad = Math.toRadians(vector.heading.toDouble());

        // ヘリコプターの場合、より正確な位置計算
        double deltaLat = distanceTraveled / EARTH_RADIUS;
        Latitude newLat = new Latitude(currentPos.latitude.toDouble() +
            Math.toDegrees(deltaLat * Math.cos(headingRad)));

        double deltaLon = -distanceTraveled / (EARTH_RADIUS * Math.cos(Math.toRadians(currentPos.longitude.toDouble())));
        Longitude newLon = new Longitude(currentPos.longitude.toDouble() +
            Math.toDegrees(deltaLon * Math.sin(headingRad)));

        // 垂直移動（ヘリコプターは急激な上昇/降下が可能）
        Altitude newAlt = new Altitude(currentPos.altitude.toDouble() +
            (vector.verticalSpeed.toDouble() * refreshRateInSeconds / 60.0));

        return new AircraftPosition(newLat, newLon, newAlt);
    }

    @Override
    public Heading calculateNextHeading(double currentHeading, double targetHeading, double maxTurnRate) {
        // ヘリコプターはその場回転が可能なため、より高速な旋回を実現
        double headingDifference = ((targetHeading - currentHeading + 540) % 360) - 180;

        // ヘリコプターの場合、最大旋回速度をさらに活用
        double helicopterTurnRate = maxTurnRate * 1.5; // ヘリコプターは1.5倍高速に旋回可能
        double nextHeading = currentHeading + Math.signum(headingDifference) *
            Math.min(helicopterTurnRate, Math.abs(headingDifference));

        return new Heading(nextHeading);
    }

    @Override
    public GroundSpeed calculateNextGroundSpeed(double currentGroundSpeed, double targetGroundSpeed, double maxAcceleration) {
        // ヘリコプターは急激な加速・減速が可能
        double helicopterAcceleration = maxAcceleration * 2.0; // より高い加速度

        double nextGroundSpeed = currentGroundSpeed;
        if (currentGroundSpeed < targetGroundSpeed) {
            nextGroundSpeed += Math.min(helicopterAcceleration, targetGroundSpeed - currentGroundSpeed);
        } else if (currentGroundSpeed > targetGroundSpeed) {
            nextGroundSpeed -= Math.min(helicopterAcceleration, currentGroundSpeed - targetGroundSpeed);
        }

        // 完全停止（0 knots）もサポート
        return new GroundSpeed(Math.max(0, nextGroundSpeed));
    }

    @Override
    public VerticalSpeed calculateNextVerticalSpeed(double currentAltitude, double targetAltitude, double maxClimbRate, double refreshRate) {
        // ヘリコプターは垂直上昇/降下が得意
        double helicopterClimbRate = maxClimbRate * 1.8; // より高い上昇/降下速度

        double nextVerticalSpeed = 0;
        if (currentAltitude < targetAltitude) {
            nextVerticalSpeed = Math.min(helicopterClimbRate / (60.0 * refreshRate), targetAltitude - currentAltitude);
        } else if (currentAltitude > targetAltitude) {
            nextVerticalSpeed = Math.min(helicopterClimbRate / (60.0 * refreshRate), currentAltitude - targetAltitude) * -1;
        }

        return new VerticalSpeed(nextVerticalSpeed * 60);
    }

    @Override
    public double calculateTurnAngle(AircraftPosition currentPos, double currentHeading, FixPosition targetFix) {
        // ヘリコプターはその場回転が可能なため、直接的な角度計算
        double targetDeltaX = targetFix.longitude.toDouble() - currentPos.longitude.toDouble();
        double targetDeltaY = targetFix.latitude.toDouble() - currentPos.latitude.toDouble();

        double targetHeading = Math.toDegrees(Math.atan2(targetDeltaX, targetDeltaY));
        return normalizeAngle(targetHeading);
    }

    /**
     * 角度を0-360度の範囲に正規化
     */
    private static double normalizeAngle(double angle) {
        return (angle % 360 + 360) % 360;
    }

    /**
     * ホバリング状態かどうかを判定
     */
    public boolean isHovering(AircraftVector vector) {
        return vector.groundSpeed.toDouble() < HOVERING_THRESHOLD;
    }
}
