package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import javax.validation.constraints.NotNull;

public class CreateAircraftDto {

    @NotNull
    private String callsign;

    @NotNull
    private String type;

    @NotNull
    private double latitude;

    @NotNull
    private double longitude;

    @NotNull
    private int altitude;

    @NotNull
    private int heading;

    @NotNull
    private int gspeed;

    @NotNull
    private int vspeed;

    @NotNull
    private String origIata;

    @NotNull
    private String origIcao;

    @NotNull
    private String destIata;

    @NotNull
    private String destIcao;

    @NotNull
    private String eta;

    // Getters and setters

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public int getGspeed() {
        return gspeed;
    }

    public void setGspeed(int gspeed) {
        this.gspeed = gspeed;
    }

    public int getVspeed() {
        return vspeed;
    }

    public void setVspeed(int vspeed) {
        this.vspeed = vspeed;
    }

    public String getOrigIata() {
        return origIata;
    }

    public void setOrigIata(String origIata) {
        this.origIata = origIata;
    }

    public String getOrigIcao() {
        return origIcao;
    }

    public void setOrigIcao(String origIcao) {
        this.origIcao = origIcao;
    }

    public String getDestIata() {
        return destIata;
    }

    public void setDestIata(String destIata) {
        this.destIata = destIata;
    }

    public String getDestIcao() {
        return destIcao;
    }

    public void setDestIcao(String destIcao) {
        this.destIcao = destIcao;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }
}