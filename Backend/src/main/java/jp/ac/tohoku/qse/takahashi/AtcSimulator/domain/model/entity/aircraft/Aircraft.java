package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;

public class Aircraft {

    private final Callsign callsign;
    private AircraftPosition position;
    private int altitude; // in feet
    private int groundSpeed; // in knots
    private int verticalSpeed; // in feet per minute
    private int heading; // in degrees
    private String type; // Aircraft ICAO type code
    private String originIata;
    private String originIcao;
    private String destinationIata;
    private String destinationIcao;
    private String eta; // Estimated Time of Arrival in ISO 8601 format

    // Constructor
    public Aircraft(Callsign callsign, AircraftPosition position, int altitude, int groundSpeed,
                    int verticalSpeed, int heading, String type, String originIata, 
                    String originIcao, String destinationIata, String destinationIcao, String eta) {
        this.callsign = callsign;
        this.position = position;
        this.altitude = altitude;
        this.groundSpeed = groundSpeed;
        this.verticalSpeed = verticalSpeed;
        this.heading = heading;
        this.type = type;
        this.originIata = originIata;
        this.originIcao = originIcao;
        this.destinationIata = destinationIata;
        this.destinationIcao = destinationIcao;
        this.eta = eta;
    }

    // Getters
    public Callsign getCallsign() {
        return callsign;
    }

    public AircraftPosition getPosition() {
        return position;
    }

    public int getAltitude() {
        return altitude;
    }

    public int getGroundSpeed() {
        return groundSpeed;
    }

    public int getVerticalSpeed() {
        return verticalSpeed;
    }

    public int getHeading() {
        return heading;
    }

    public String getType() {
        return type;
    }

    public String getOriginIata() {
        return originIata;
    }

    public String getOriginIcao() {
        return originIcao;
    }

    public String getDestinationIata() {
        return destinationIata;
    }

    public String getDestinationIcao() {
        return destinationIcao;
    }

    public String getEta() {
        return eta;
    }

    // Setters
    public void setPosition(AircraftPosition position) {
        this.position = position;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public void setGroundSpeed(int groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public void setVerticalSpeed(int verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOriginIata(String originIata) {
        this.originIata = originIata;
    }

    public void setOriginIcao(String originIcao) {
        this.originIcao = originIcao;
    }

    public void setDestinationIata(String destinationIata) {
        this.destinationIata = destinationIata;
    }

    public void setDestinationIcao(String destinationIcao) {
        this.destinationIcao = destinationIcao;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    // Update position based on heading, speed, and time (simplified)
    public void updatePosition(int elapsedTimeInSeconds) {
        // Calculate new position based on heading and speed
        double distanceTraveled = (groundSpeed * elapsedTimeInSeconds) / 3600.0; // in nautical miles
        double deltaLat = distanceTraveled * Math.cos(Math.toRadians(heading));
        double deltaLon = distanceTraveled * Math.sin(Math.toRadians(heading));
        
        // Update position
        position.setLatitude(position.getLatitude() + deltaLat);
        position.setLongitude(position.getLongitude() + deltaLon);
        
        // Update altitude based on vertical speed
        altitude += (verticalSpeed * elapsedTimeInSeconds) / 60;
    }
}