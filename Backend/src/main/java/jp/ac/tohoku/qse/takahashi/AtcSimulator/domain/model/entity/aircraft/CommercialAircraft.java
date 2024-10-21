package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
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
     * Calculates the turn angle required for an aircraft to reach a target position.
     * The function takes into account the current heading, aircraft coordinates,
     * target coordinates, and turn radius. It calculates the shortest angle
     * (either left or right) needed for the aircraft to align its heading towards the target.
     *
     *  @param targetLongitude             The X coordinate (longitude) of the target point.
     * @param targetLatitude             The Y coordinate (latitude) of the target point.
     * @param turnRadius          The turn radius of the aircraft.
     * @return The turn angle in degrees, rounded to the nearest integer.
     */
    public int calculateTurnAngle(double targetLongitude, double targetLatitude) {
        final AircraftPosition currentPosition = this.aircraftPosition;
        final double currentHeading = this.aircraftVector.heading.toDouble();

        // 1. Calculate the angle between the current position and the target position
        double deltaX = targetLongitude - currentPosition.longitude.toDouble();
        double deltaY = targetLatitude - currentPosition.latitude.toDouble();

        // Convert the target position to an angle in degrees (relative to North)
        double targetHeading = Math.toDegrees(Math.atan2(deltaX, deltaY));

        // Ensure targetHeading is between 0 and 360 degrees
        if (targetHeading < 0) {
            targetHeading += 360;
        }

        // 3. Simulate aircraft moving along the turn radius until the target is aligned
        double aircraftLongitude = currentPosition.longitude.toDouble();
        double aircraftLatitude = currentPosition.latitude.toDouble();
        double heading = currentHeading;
        double turnAngleAccumulated = 0.0;


        // Loop until the aircraft's heading aligns with the target heading
        while (true) {
            turnAngleAccumulated += MAX_TURN_RATE;

            // Calculate new heading
            heading = normalizeAngle(heading + MAX_TURN_RATE);

            // Update the aircraft's position along the circular arc
            double currentGroundSpeed = this.aircraftVector.groundSpeed.toDouble();
            aircraftLongitude += currentGroundSpeed * Math.cos(Math.toRadians(heading));
            aircraftLatitude += currentGroundSpeed * Math.sin(Math.toRadians(heading));

            // Recalculate the angle between the updated position and the target
            double newDeltaX = targetLongitude - aircraftLongitude;
            double newDeltaY = targetLatitude - aircraftLatitude;
            double newHeadingToTarget = Math.toDegrees(Math.atan2(newDeltaX, newDeltaY));

            if (newHeadingToTarget < 0) {
                newHeadingToTarget += 360;
            }

            // Check if the heading aligns with the target heading (within a small tolerance)
            if (Math.abs(normalizeAngle(newHeadingToTarget - heading)) < 1.0) {
                break; // Stop if the heading is aligned with the target
            }

            // Prevent infinite loops by breaking after a full 360-degree turn
            if (turnAngleAccumulated > 360) {
                break; // Exit after making a full circle
            }
        }

        // Return the total accumulated turn angle, rounded to the nearest integer
        return (int) Math.round(turnAngleAccumulated);
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
