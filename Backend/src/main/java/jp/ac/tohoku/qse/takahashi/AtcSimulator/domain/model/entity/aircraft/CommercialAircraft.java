package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import org.apache.tomcat.util.http.HeaderUtil;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.constants.GlobalConstants.*;

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
//    private Heading calculateNextHeading(final double currentHeading, final double targetHeading) {
//        double nextHeading = currentHeading;
//        if (currentHeading < targetHeading) {
//            if (targetHeading - currentHeading > 180) {
//                nextHeading -= Math.min(MAX_TURN_RATE, 360 - targetHeading + currentHeading);
//            } else {
//                nextHeading += Math.min(MAX_TURN_RATE, targetHeading - currentHeading);
//            }
//        } else if (currentHeading > targetHeading) {
//            nextHeading -= Math.min(MAX_TURN_RATE, currentHeading - targetHeading);
//        }
//        return new Heading(nextHeading);
//    }
    private Heading calculateNextHeading(final double currentHeading, final double targetHeading) {
        // ヘディング差を-180度から180度の範囲に正規化
        double headingDifference = ((targetHeading - currentHeading + 540) % 360) - 180;

        // ヘディング差が正の場合は右回転、負の場合は左回転
        double nextHeading = currentHeading + Math.signum(headingDifference) * Math.min(MAX_TURN_RATE, Math.abs(headingDifference));

        return new Heading(nextHeading);
    }

    private VerticalSpeed calculateNextVerticalSpeed(final double currentAltitude, final double targetAltitude) {
        double nextVarticalSpeed = 0;
        if (currentAltitude < targetAltitude) {
            nextVarticalSpeed = Math.min(MAX_CLIMB_RATE / 60.0, targetAltitude - currentAltitude);
        } else if (currentAltitude > targetAltitude) {
            nextVarticalSpeed = Math.min(MAX_CLIMB_RATE / 60.0, currentAltitude - targetAltitude) * -1;
        }
        return new VerticalSpeed(nextVarticalSpeed);
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
