package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

public class AircraftPosition {
    private double latitude;
    private double longitude;
    private double altitude;

    public AircraftPosition(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public double getLatitude() {
        return this.latitude;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public double getAltitude() {
        return this.altitude;
    }

}
