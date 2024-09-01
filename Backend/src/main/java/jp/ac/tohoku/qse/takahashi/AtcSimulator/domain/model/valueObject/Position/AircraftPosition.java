package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;

/**
 * Represents the position of an aircraft in 3D space.
 *
 */
public class AircraftPosition {
    private double latitude;
    private double longitude;
    private Altitude altitude;

    public AircraftPosition(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = new Altitude(altitude);
    }

    public double getLatitude() {
        return this.latitude;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public Altitude getAltitude() {
        return this.altitude;
    }

    public void setLatitude(final double newLatitude) {
        this.latitude = newLatitude;
    }

    public void setLongitude(final double newLongitude) {
        this.longitude = newLongitude;
    }

    @Override
    public String toString() {
        return "AircraftPosition{" +
                "latitude=" + this.latitude +
                ", longitude=" + this.longitude +
                ", altitude=" + this.altitude +
                '}';
    }
}
