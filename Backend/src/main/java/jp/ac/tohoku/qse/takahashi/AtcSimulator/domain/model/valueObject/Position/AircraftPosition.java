package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;

/**
 * Represents the position of an aircraft in 3D space.
 *
 */
public class AircraftPosition {
    private double latitude;
    final Longitude longitude;
    final Altitude altitude;

    public AircraftPosition(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = new Longitude(longitude);
        this.altitude = new Altitude(altitude);
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(final double newLatitude) {
        this.latitude = newLatitude;
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
