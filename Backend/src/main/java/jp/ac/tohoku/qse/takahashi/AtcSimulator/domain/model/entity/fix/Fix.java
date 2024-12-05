package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

public class Fix {
    private final String name;
    private final FixPosition position;

    public Fix(String name, double latitude, double longitude) {
        this.name = name;
        Latitude lat = new Latitude(latitude);
        Longitude lon = new Longitude(longitude);
        this.position = new FixPosition(lat, lon);
    }

    public String getName() {
        return name;
    }

    public FixPosition getPosition() {
        return position;
    }
}
