package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.valueObject.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.valueObject.Position;

public class CommercialAircraft extends Aircraft {

    private String aircraftType;  // 機種（例: "Boeing 777"）
    private String originAirport; // 出発地空港（例: "RJTT"）
    private String destinationAirport; // 目的地空港（例: "KLAX"）
    private String airline; // 航空会社（例: "All Nippon Airways"）

    public CommercialAircraft(Callsign callsign, Position position, int altitude, int speed, int heading,
                              String aircraftType, String originAirport, String destinationAirport, String airline) {
        super(callsign, position, altitude, speed, heading);
        this.aircraftType = aircraftType;
        this.originAirport = originAirport;
        this.destinationAirport = destinationAirport;
        this.airline = airline;
    }

    // Getter and Setter methods

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getOriginAirport() {
        return originAirport;
    }

    public void setOriginAirport(String originAirport) {
        this.originAirport = originAirport;
    }

    public String getDestinationAirport() {
        return destinationAirport;
    }

    public void setDestinationAirport(String destinationAirport) {
        this.destinationAirport = destinationAirport;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    @Override
    public String toString() {
        return "CommercialAircraft{" +
                "callsign=" + getCallsign() +
                ", position=" + getPosition() +
                ", altitude=" + getAltitude() +
                ", speed=" + getSpeed() +
                ", heading=" + getHeading() +
                ", aircraftType='" + aircraftType + '\'' +
                ", originAirport='" + originAirport + '\'' +
                ", destinationAirport='" + destinationAirport + '\'' +
                ", airline='" + airline + '\'' +
                '}';
    }
}