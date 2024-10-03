package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util;

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

    public RadioNavigationAid(String name, String id, String type, String frequency, double latitude, double longitude) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.frequency = frequency;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
