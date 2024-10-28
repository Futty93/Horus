package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

public class CommercialAircraft extends AircraftBase implements Aircraft {
    // 最大加速度（kts/s）
    private static final double MAX_ACCELERATION = 3.0;

    // 最大旋回速度（度/秒）
    private static final double MAX_TURN_RATE = 3.0;

    // 最大上昇/降下速度（ft/min）
    private static final double MAX_CLIMB_RATE = 1640.0;

    private final String originIata;
    private final String originIcao;
    private final String destinationIata;
    private final String destinationIcao;
    private final String eta; //Estimated Time of Arrival in ISO 8601 format

    //Constructor
    public CommercialAircraft(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition, AircraftVector aircraftVector, String originIata, String originIcao, String destinationIata, String destinationIcao, String eta) {
        super(callsign, aircraftType, aircraftPosition, aircraftVector);
        this.originIata = originIata;
        this.originIcao = originIcao;
        this.destinationIata = destinationIata;
        this.destinationIcao = destinationIcao;
        this.eta = eta;
    }

    /**
     * 航空機の現在のaircraftPositionとaircraftVectorを元に、次のaircraftPositionを計算する
     */
    public void calculateNextAircraftPosition() {
        final AircraftPosition currentPos = this.aircraftPosition;
        final AircraftVector vector = this.aircraftVector;

        // Refresh rate in milliseconds
        double refreshRateInSeconds = 1.0 / REFRESH_RATE;

        // Ground speed in km/h
        double groundSpeedKmPerHour = vector.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR;

        // Distance traveled in the given time period (in km)
        double distanceTraveled = groundSpeedKmPerHour * (refreshRateInSeconds / 3600.0); // in km

        // Convert heading to radians for calculation
        double headingRad = Math.toRadians(vector.heading.toDouble());

        // Calculate new latitude
        double deltaLat = distanceTraveled / EARTH_RADIUS; // in radians
        Latitude newLat = new Latitude(currentPos.latitude.toDouble() + Math.toDegrees(deltaLat * Math.cos(headingRad)));

        // Calculate new longitude considering Earth curvature
        double deltaLon = - distanceTraveled / (EARTH_RADIUS * Math.cos(Math.toRadians(currentPos.longitude.toDouble()))); // in radians
        Longitude newLon = new Longitude(currentPos.longitude.toDouble() + Math.toDegrees(deltaLon * Math.sin(headingRad)));

        // Calculate new altitude
        Altitude newAlt = new Altitude(currentPos.altitude.toDouble() + (vector.verticalSpeed.toDouble() * refreshRateInSeconds / 60.0)); // in feet

        // Return the new aircraft position
//        this.aircraftPosition = new AircraftPosition(newLat, newLon, newAlt);
        this.setAircraftPosition(new AircraftPosition(newLat, newLon, newAlt));
    }

    private GroundSpeed calculateNextGroundSpeed(double currentGroundSpeed, double targetGroundSpeed) {
        double nextGroundSpeed = currentGroundSpeed;
        if (currentGroundSpeed < targetGroundSpeed) {
            nextGroundSpeed += Math.min(MAX_ACCELERATION, targetGroundSpeed - currentGroundSpeed);
        } else if (currentGroundSpeed > targetGroundSpeed) {
            nextGroundSpeed -= Math.min(MAX_ACCELERATION, currentGroundSpeed - targetGroundSpeed);
        }
        return new GroundSpeed(nextGroundSpeed);
    }

    /**
     * instructedVector から 航空機の次のHeadingを計算する
     */
    private Heading calculateNextHeading(final double currentHeading, final double targetHeading) {
        // ヘディング差を-180度から180度の範囲に正規化
        double headingDifference = ((targetHeading - currentHeading + 540) % 360) - 180;

        // ヘディング差が正の場合は右回転、負の場合は左回転
        double nextHeading = currentHeading + Math.signum(headingDifference) * Math.min(MAX_TURN_RATE, Math.abs(headingDifference));

        return new Heading(nextHeading);
    }

    /**
     * instructedVector から 航空機の次のVerticalSpeedを計算する
     * 指示された高度と現在の高度を比較し、垂直速度を設定する
     */
    private VerticalSpeed calculateNextVerticalSpeed(final double currentAltitude, final double targetAltitude) {
        double nextVarticalSpeed = 0;
        if (currentAltitude < targetAltitude) {
            nextVarticalSpeed = Math.min(MAX_CLIMB_RATE / (60.0 * REFRESH_RATE), targetAltitude - currentAltitude);
        } else if (currentAltitude > targetAltitude) {
            nextVarticalSpeed = Math.min(MAX_CLIMB_RATE / (60.0 * REFRESH_RATE), currentAltitude - targetAltitude) * -1;
        }
        return new VerticalSpeed(nextVarticalSpeed * 60);
    }

    /**
     * instructedVector から 航空機の次のaircraftVectorを計算する
     * 指示された高度と現在の高度を比較し、垂直速度を設定する
     */
    public void calculateNextAircraftVector() {
        InstructedVector instructedVector = this.getInstructedVector();
        //  次のHeadingを計算
        Heading nextHeading = calculateNextHeading(this.getAircraftVector().heading.toDouble(), instructedVector.instructedHeading.toDouble());

        //  次のGroundSpeedを計算
        GroundSpeed nextGroundSpeed = calculateNextGroundSpeed(this.getAircraftVector().groundSpeed.toDouble(), instructedVector.instructedGroundSpeed.toDouble());

        //  次のAltitudeを計算
        VerticalSpeed nextVerticalSpeed = calculateNextVerticalSpeed(this.getAircraftPosition().altitude.toDouble(), instructedVector.instructedAltitude.toDouble());

        // 新しいAircraftVectorを設定
        this.setAircraftVector(new AircraftVector(nextHeading, nextGroundSpeed, nextVerticalSpeed));
    }

    /**
     * Calculates the turn angle required for an aircraft to reach a target position.
     * The function takes into account the current heading, aircraft coordinates,
     * and target coordinates. It calculates the shortest angle
     * (either left or right) needed for the aircraft to align its heading towards the target.
     *
     * @param targetFix The target position (latitude and longitude).
     * @return The turn angle in degrees, rounded to the nearest integer.
     */
    public double calculateTurnAngle(FixPosition targetFix) {
        final AircraftPosition currentPos = this.aircraftPosition;
        final double currentHeading = this.aircraftVector.heading.toDouble();

        final double currentHeadingRad = Math.toRadians(currentHeading);

        // ターゲット位置への相対位置差を計算
        double targetDeltaX = targetFix.longitude.toDouble() - currentPos.longitude.toDouble();
        double targetDeltaY = targetFix.latitude.toDouble() - currentPos.latitude.toDouble();

        // ターゲット位置の方位（北基準、度単位）
        double targetHeading = Math.toDegrees(Math.atan2(targetDeltaX, targetDeltaY));
        targetHeading = normalizeAngle(targetHeading);

        return targetHeading;

//        // 現在のヘディングとの差を計算
//        double headingDifference = ((targetHeading - currentHeading + 540) % 360) - 180;
//
//        final double R = calculateTurnRadius(this.aircraftVector.groundSpeed.toDouble());
//
//        double deltaCenterLat = R * Math.sin(currentHeadingRad);
//        double deltaCenterLon = R * Math.cos(currentHeadingRad);
//
//        // 旋回中心の緯度と経度を計算
//        double turnCenterLat = headingDifference > 0 ? currentPos.latitude.toDouble() + deltaCenterLat : currentPos.latitude.toDouble() - deltaCenterLat;
//        double turnCenterLon = headingDifference > 0 ? currentPos.longitude.toDouble() - deltaCenterLon : currentPos.longitude.toDouble() + deltaCenterLon;
//
//        double slopeCenterToTarget = (targetFix.longitude.toDouble() - turnCenterLon) / (targetFix.latitude.toDouble() - turnCenterLat);
//
//        double angleCenterToTarget = Math.toDegrees(Math.atan(slopeCenterToTarget));
//
//        double dx = targetFix.latitude.toDouble() - turnCenterLat;
//        double dy = targetFix.longitude.toDouble() - turnCenterLon;
//        // 円の接線の傾き m1 と m2 を求める
//        double discriminant = R * R * (dx * dx + dy * dy) - (dy * dx) * (dy * dx);
//        if (discriminant < 0) {
//            throw new IllegalArgumentException("接線が存在しません。指定された点は円の外部にありません。");
//        }
//
//        // 2つの接線の傾き
//        double m1 = (dy * dx + Math.sqrt(discriminant)) / (dx * dx - R * R);
//        double m2 = (dy * dx - Math.sqrt(discriminant)) / (dx * dx - R * R);
//
//        // `theta` のラジアン角に最も近い傾きを選択
//        double thetaRad = Math.atan2(dy, dx);
//        double closestSlope = Math.abs(currentHeadingRad - Math.atan(m1)) < Math.abs(currentHeadingRad - Math.atan(m2)) ? m1 : m2;
//
//        return closestSlope;

//
//        // 旋回方向と角速度を設定
//        double turnIncrement = headingDifference > 0 ? 0.1 : -0.1;
//        double accumulatedTurnAngle = headingDifference > 0 ? targetHeading - 10 : targetHeading + 10;
//
//        // 距離と位置変化量の計算
//        final double timeStep = 1.0 / REFRESH_RATE;
//        final double speedKmPerHour = this.aircraftVector.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR;
//        double travelDistance = speedKmPerHour * (timeStep / 3600.0);
//        double deltaLatitude = travelDistance / EARTH_RADIUS;
//
//        // 航空機の現在位置
//        double aircraftLng = currentPos.longitude.toDouble();
//        double aircraftLat = currentPos.latitude.toDouble();
//
//        // 旋回を行いターゲット方向に一致するまで繰り返す
//        while (true) {
//            // 旋回角度に基づき、航空機の位置を更新
//            aircraftLng += (Math.toDegrees(deltaLatitude * Math.cos(accumulatedTurnAngle)) / (MAX_TURN_RATE / turnIncrement));
//            aircraftLat += (Math.toDegrees(deltaLatitude * Math.sin(accumulatedTurnAngle)) / (MAX_TURN_RATE / turnIncrement));
//
//            // 更新された位置からターゲット方向を再計算
//            double updatedDeltaX = targetFix.longitude.toDouble() - aircraftLng;
//            double updatedDeltaY = targetFix.latitude.toDouble() - aircraftLat;
//            double updatedHeadingToTarget = Math.toDegrees(Math.atan2(updatedDeltaX, updatedDeltaY));
//            updatedHeadingToTarget = updatedHeadingToTarget < 0 ? updatedHeadingToTarget + 360 : updatedHeadingToTarget;
//
//            // ターゲットヘディングと一致（許容範囲内）した場合にループを終了
//            if (Math.abs(normalizeAngle(updatedHeadingToTarget - accumulatedTurnAngle)) < 0.1) {
//                break;
//            }
//
//            // 無限ループ回避のため180度以上旋回したら終了
//            if (Math.abs(accumulatedTurnAngle) > 720) {
//                break;
//            }
//
//            accumulatedTurnAngle = normalizeAngle(accumulatedTurnAngle + turnIncrement);
//        }
//
//        System.out.println("Turn angle: " + accumulatedTurnAngle);
//        return accumulatedTurnAngle;
    }

    /**
     * Calculates the turn radius based on the aircraft's ground speed and the maximum turn rate.
     *
     * @param groundSpeed The aircraft's ground speed in knots.
     * @return The turn radius in nautical miles.
     */
    private double calculateTurnRadius(double groundSpeed) {
        // Convert the max turn rate from degrees per second to radians per second
        double turnRateRadiansPerSecond = Math.toRadians(MAX_TURN_RATE);

        // Calculate the turn radius (in nautical miles)
        return groundSpeed / turnRateRadiansPerSecond;
    }

    /**
     * Normalizes an angle to the range [0, 360).
     *
     * @param angle The angle to normalize.
     * @return The normalized angle.
     */
    private static double normalizeAngle(double angle) {
        return (angle % 360 + 360) % 360;
    }


    @Override
    public String toString() {
        AircraftPosition aircraftPosition = this.aircraftPosition;
        AircraftVector aircraftVector = this.getAircraftVector();

        return "CommercialAircraft{" +
                "callsign=" + this.getCallsign().toString() +
                ", position={" +
                "latitude=" + aircraftPosition.latitude.toString() +
                ", longitude=" + aircraftPosition.longitude.toString() +
                ", altitude=" + aircraftPosition.altitude.toString() +
                "}, vector={" +
                "heading=" + aircraftVector.heading.toString() +
                ", groundSpeed=" + aircraftVector.groundSpeed.toString() +
                ", verticalSpeed=" + aircraftVector.verticalSpeed.toString() +
                "}, instructedVector={" +
                "heading=" + instructedVector.instructedHeading.toString() +
                ", groundSpeed=" + instructedVector.instructedGroundSpeed.toString() +
                ", altitude=" + instructedVector.instructedAltitude.toString() +
                "}, type=" + this.getAircraftType() +
                ", originIata=" + this.originIata +
                ", originIcao=" + this.originIcao +
                ", destinationIata=" + this.destinationIata +
                ", destinationIcao=" + this.destinationIcao +
                ", eta=" + this.eta +
                '}';
    }
}
