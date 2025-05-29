package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * 固定翼機の飛行動作実装
 * 旅客機、戦闘機、貨物機などの固定翼機に共通する飛行特性を実装
 */
public class FixedWingFlightBehavior implements FlightBehavior {

    @Override
    public AircraftPosition calculateNextPosition(AircraftPosition currentPos, AircraftVector vector, double refreshRate) {
        // Refresh rate in seconds
        double refreshRateInSeconds = 1.0 / refreshRate;

        // Ground speed in km/h
        double groundSpeedKmPerHour = vector.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR;

        // Distance traveled in the given time period (in km)
        double distanceTraveled = groundSpeedKmPerHour * (refreshRateInSeconds / 3600.0);

        // Convert heading to radians for calculation
        double headingRad = Math.toRadians(vector.heading.toDouble());

        // Calculate new latitude
        double deltaLat = distanceTraveled / EARTH_RADIUS; // in radians
        Latitude newLat = new Latitude(currentPos.latitude.toDouble() + Math.toDegrees(deltaLat * Math.cos(headingRad)));

        // Calculate new longitude considering Earth curvature
        double deltaLon = -distanceTraveled / (EARTH_RADIUS * Math.cos(Math.toRadians(currentPos.longitude.toDouble())));
        Longitude newLon = new Longitude(currentPos.longitude.toDouble() + Math.toDegrees(deltaLon * Math.sin(headingRad)));

        // Calculate new altitude
        Altitude newAlt = new Altitude(currentPos.altitude.toDouble() + (vector.verticalSpeed.toDouble() * refreshRateInSeconds / 60.0));

        return new AircraftPosition(newLat, newLon, newAlt);
    }

    @Override
    public Heading calculateNextHeading(double currentHeading, double targetHeading, double maxTurnRate) {
        // ヘディング差を-180度から180度の範囲に正規化
        double headingDifference = ((targetHeading - currentHeading + 540) % 360) - 180;

        // ヘディング差が正の場合は右回転、負の場合は左回転
        double nextHeading = currentHeading + Math.signum(headingDifference) * Math.min(maxTurnRate, Math.abs(headingDifference));

        return new Heading(nextHeading);
    }

    @Override
    public GroundSpeed calculateNextGroundSpeed(double currentGroundSpeed, double targetGroundSpeed, double maxAcceleration) {
        double nextGroundSpeed = currentGroundSpeed;
        if (currentGroundSpeed < targetGroundSpeed) {
            nextGroundSpeed += Math.min(maxAcceleration, targetGroundSpeed - currentGroundSpeed);
        } else if (currentGroundSpeed > targetGroundSpeed) {
            nextGroundSpeed -= Math.min(maxAcceleration, currentGroundSpeed - targetGroundSpeed);
        }
        return new GroundSpeed(nextGroundSpeed);
    }

    @Override
    public VerticalSpeed calculateNextVerticalSpeed(double currentAltitude, double targetAltitude, double maxClimbRate, double refreshRate) {
        double nextVerticalSpeed = 0;
        if (currentAltitude < targetAltitude) {
            nextVerticalSpeed = Math.min(maxClimbRate / (60.0 * refreshRate), targetAltitude - currentAltitude);
        } else if (currentAltitude > targetAltitude) {
            nextVerticalSpeed = Math.min(maxClimbRate / (60.0 * refreshRate), currentAltitude - targetAltitude) * -1;
        }
        return new VerticalSpeed(nextVerticalSpeed * 60);
    }

    @Override
    public double calculateTurnAngle(AircraftPosition currentPos, double currentHeading, FixPosition targetFix) {
        // ターゲット位置への相対位置差を計算
        double targetDeltaX = targetFix.longitude.toDouble() - currentPos.longitude.toDouble();
        double targetDeltaY = targetFix.latitude.toDouble() - currentPos.latitude.toDouble();

        // ターゲット位置の方位（北基準、度単位）
        double targetHeading = Math.toDegrees(Math.atan2(targetDeltaX, targetDeltaY));
        return normalizeAngle(targetHeading);
    }

    /**
     * 角度を0-360度の範囲に正規化
     */
    private static double normalizeAngle(double angle) {
        return (angle % 360 + 360) % 360;
    }
}
