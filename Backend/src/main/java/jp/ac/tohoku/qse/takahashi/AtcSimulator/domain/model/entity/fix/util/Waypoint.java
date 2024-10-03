package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util;

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
}
