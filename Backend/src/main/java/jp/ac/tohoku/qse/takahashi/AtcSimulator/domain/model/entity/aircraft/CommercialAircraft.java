package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.constants.GlobalConstants.*;

public class CommercialAircraft extends AircraftBase implements Aircraft {
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
        double deltaLon = distanceTraveled / (EARTH_RADIUS * Math.cos(Math.toRadians(currentPos.latitude.toDouble()))); // in radians
        Longitude newLon = new Longitude(currentPos.latitude.toDouble() + Math.toDegrees(deltaLon * Math.sin(headingRad)));

        // Calculate new altitude
        Altitude newAlt = new Altitude(currentPos.altitude.toDouble() + (vector.verticalSpeed.toDouble() * refreshRateInSeconds / 60.0)); // in feet

        // Return the new aircraft position
        this.aircraftPosition = new AircraftPosition(newLat, newLon, newAlt);
    }

    /**
     * instructedVector から 航空機の次のaircraftVectorを計算する
     * 指示された高度と現在の高度を比較し、垂直速度を設定する
     */
    public void calculateNextAircraftVector() {
        InstructedVector instructedVector = this.getInstructedVector();

        // 現在の高度と指示された高度を比較
        double altitudeDelta = this.aircraftPosition.altitude.compareAltitude(this.aircraftPosition.altitude, instructedVector.instructedAltitude);
        int verticalSpeed;

        if (altitudeDelta > 0) {
            // 指示された高度の方が高い場合
            verticalSpeed = 10;
        } else if (altitudeDelta < 0) {
            // 指示された高度の方が低い場合
            verticalSpeed = -10;
        } else {
            // 高度が同じ場合
            verticalSpeed = 0;
        }

        // 新しいAircraftVectorを設定
        this.setAircraftVector(new AircraftVector(instructedVector.instructedHeading, instructedVector.instructedGroundSpeed, new VerticalSpeed(verticalSpeed)));
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
                "}, type=" + this.getAircraftType() +
                ", originIata=" + this.originIata +
                ", originIcao=" + this.originIcao +
                ", destinationIata=" + this.destinationIata +
                ", destinationIcao=" + this.destinationIcao +
                ", eta=" + this.eta +
                '}';
    }
}
