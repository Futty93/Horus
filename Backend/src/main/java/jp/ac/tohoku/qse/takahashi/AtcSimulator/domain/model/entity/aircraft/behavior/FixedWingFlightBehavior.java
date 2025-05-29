package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.MathUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PositionUtils;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * 固定翼機の飛行動作実装
 * 旅客機、戦闘機、貨物機などの固定翼機に共通する飛行特性を実装
 */
public class FixedWingFlightBehavior implements FlightBehavior {

    @Override
    public AircraftPosition calculateNextPosition(AircraftPosition currentPos, AircraftVector vector, double refreshRate) {
        double refreshRateInSeconds = 1.0 / refreshRate;

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
        return PositionUtils.calculateNextHeading(currentHeading, targetHeading, maxTurnRate);
    }

    @Override
    public GroundSpeed calculateNextGroundSpeed(double currentGroundSpeed, double targetGroundSpeed, double maxAcceleration) {
        return PositionUtils.calculateNextGroundSpeed(currentGroundSpeed, targetGroundSpeed, maxAcceleration);
    }

    @Override
    public VerticalSpeed calculateNextVerticalSpeed(double currentAltitude, double targetAltitude, double maxClimbRate, double refreshRate) {
        return PositionUtils.calculateNextVerticalSpeed(currentAltitude, targetAltitude, maxClimbRate, refreshRate);
    }

    @Override
    public double calculateTurnAngle(AircraftPosition currentPos, double currentHeading, FixPosition targetFix) {
        return PositionUtils.calculateBearingToTarget(
            currentPos,
            targetFix.latitude.toDouble(),
            targetFix.longitude.toDouble()
        );
    }

    /**
     * 角度を0-360度の範囲に正規化
     */
    private static double normalizeAngle(double angle) {
        return (angle % 360 + 360) % 360;
    }
}
