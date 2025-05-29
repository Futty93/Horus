package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;

public class RadioNavigationAid {
    private String name;
    private String id;
    private String type;
    private String frequency;
    private String hoursOfOperation;
    private double latitude;
    private double longitude;
    private String elevation;
    private String remarks;

    public RadioNavigationAid(String name, String type, double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format("{\"name\":\"%s\", \"type\":\"%s\", \"latitude\":%f, \"longitude\":%f}", this.name, this.type, this.latitude, this.longitude);
    }

    public String getName() {
        return this.name;
    }

    public Latitude getLatitude() {
        return new Latitude(this.latitude);
    }

    public Longitude getLongitude() {
        return new Longitude(this.longitude);
    }
}
