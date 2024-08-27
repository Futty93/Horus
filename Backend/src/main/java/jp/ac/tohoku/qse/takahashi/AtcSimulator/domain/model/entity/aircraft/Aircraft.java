package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public class Aircraft {
    private final Callsign callsign;
    private AircraftPosition aircraftPosition;
    private AircraftVector aircraftVector;
    private final AircraftType aircraftType; //Aircraft ICAO type code

    //Constructor
    public Aircraft(Callsign callsign, AircraftPosition aircraftPosition, AircraftVector aircraftVector, AircraftType aircraftType) {
        this.callsign = callsign;
        this.aircraftPosition = aircraftPosition;
        this.aircraftVector = aircraftVector;
        this.aircraftType = aircraftType;
    };

    public Callsign getCallsign() {
        return this.callsign;
    }

    public AircraftPosition getAircraftPosition() {
        return this.aircraftPosition;
    }

    public AircraftVector getAircraftVector() {
        return this.aircraftVector;
    }

    public AircraftType getAircraftType() {
        return this.aircraftType;
    }

    // aircraftVector から 航空機の次の位置を計算する
    public void calculateNextAircraftPosition() {
        double heading = this.aircraftVector.getHeading();
        double groundSpeed = this.aircraftVector.getGroundSpeed();
        double verticalSpeed = this.aircraftVector.getVerticalSpeed();
        double latitude = this.aircraftPosition.getLatitude();
        double longitude = this.aircraftPosition.getLongitude();
        double altitude = this.aircraftPosition.getAltitude();
        double distance = groundSpeed / 3600; //1秒間に進む距離
        double verticalDistance = verticalSpeed / 3600; //1秒間に進む高度
        double radian = Math.toRadians(heading);
        double latDistance = distance * Math.cos(radian);
        double lonDistance = distance * Math.sin(radian);
        latitude += latDistance;
        longitude += lonDistance;
        altitude += verticalDistance;
        this.aircraftPosition = new AircraftPosition(latitude, longitude, altitude);
    }


    public boolean IsEqualCallsign(Callsign callsign){
        return this.callsign.equals(callsign);
    }
}