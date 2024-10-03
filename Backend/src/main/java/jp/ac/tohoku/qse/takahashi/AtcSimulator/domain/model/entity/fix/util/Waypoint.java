package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util;

public class Waypoint {
    private String name;
    private double latitude;
    private double longitude;

    public Waypoint(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format("{\"name\":\"%s\", \"latitude\":%f, \"longitude\":%f}", this.name, this.latitude, this.longitude);
    }
}
