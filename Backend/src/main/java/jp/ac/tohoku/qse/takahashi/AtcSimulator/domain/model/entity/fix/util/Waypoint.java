package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;

public class Waypoint {
    private String name;
    private double latitude;
    private double longitude;
    private String type;

    public Waypoint(String name, double latitude, double longitude, String type) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("{\"name\":\"%s\", \"latitude\":%f, \"longitude\":%f, \"type\":\"%s\"}", this.name, this.latitude, this.longitude, this.type);
    }

    public Object getName() {
        return this.name;
    }

    public Latitude getLatitude() {
        return new Latitude(this.latitude);
    }

    public Longitude getLongitude() {
        return new Longitude(this.longitude);
    }
}
