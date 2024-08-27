package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

/**
 * Represents the position of an aircraft in 3D space.
 *
 * @param latitude the latitude of the aircraft (in degrees)
 * @param longitude the longitude of the aircraft (in degrees)
 * @param altitude the altitude of the aircraft (in feet)
 */
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

    public void setLatitude(final double newLatitude) {
        this.latitude = newLatitude;
    }

    public void setLongitude(final double newLongitude) {
        this.longitude = newLongitude;
    }

    public void setAltitude(final double newAltitude) {
        this.altitude = newAltitude;
    }
}
