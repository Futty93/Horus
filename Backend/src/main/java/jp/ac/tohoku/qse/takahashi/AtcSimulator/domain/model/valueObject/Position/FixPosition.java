package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import java.util.Objects;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;

public class FixPosition {
    public final Latitude latitude;
    public final Longitude longitude;

    public FixPosition(Latitude latitude, Longitude longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FixPosition that = (FixPosition) obj;
        return Double.compare(that.latitude.toDouble(), latitude.toDouble()) == 0
                && Double.compare(that.longitude.toDouble(), longitude.toDouble()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude.toDouble(), longitude.toDouble());
    }

    @Override
    public String toString() {
        return String.format("{\"latitude\":%s, \"longitude\":%s}", this.latitude, this.longitude);
    }
}
