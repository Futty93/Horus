package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

public class ControlAircraftDto {
    private String callsign;  // The aircraft's callsign
    private Integer heading;  // New heading for the aircraft
    private Integer altitude; // New altitude for the aircraft
    private Integer speed;    // New speed for the aircraft

    // Constructor
    public ControlAircraftDto(String callsign, Integer heading, Integer altitude, Integer speed) {
        this.callsign = callsign;
        this.heading = heading;
        this.altitude = altitude;
        this.speed = speed;
    }

    // Getters and setters
    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public Integer getHeading() {
        return heading;
    }

    public void setHeading(Integer heading) {
        this.heading = heading;
    }

    public Integer getAltitude() {
        return altitude;
    }

    public void setAltitude(Integer altitude) {
        this.altitude = altitude;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }
}