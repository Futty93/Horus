package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;

/**
 * Represents the position of an aircraft in 3D space.
 *
 */
public class AircraftPosition {
    public final Latitude latitude;
    public final Longitude longitude;
    public final Altitude altitude;

    public AircraftPosition(Latitude latitude, Longitude longitude, Altitude altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
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
