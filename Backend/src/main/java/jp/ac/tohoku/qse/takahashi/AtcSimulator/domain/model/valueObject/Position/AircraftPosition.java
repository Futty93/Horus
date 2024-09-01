package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;

/**
 * Represents the position of an aircraft in 3D space.
 *
 */
public class AircraftPosition {
    final Latitude latitude;
    final Longitude longitude;
    final Altitude altitude;

    public AircraftPosition(double latitude, double longitude, double altitude) {
        this.latitude = new Latitude(latitude);
        this.longitude = new Longitude(longitude);
        this.altitude = new Altitude(altitude);
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
