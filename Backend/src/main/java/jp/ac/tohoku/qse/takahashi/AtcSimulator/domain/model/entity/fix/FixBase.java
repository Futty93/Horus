package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.FixType;

import java.util.List;

public class FixBase {
    private final String name; // Fix name
    private final FixType type; // Fix type
    private final double latitude; // Latitude of the fix
    private final double longitude; // Longitude of the fix
    private final List<FixBase> connectedFix; // List of connected fixes

    public FixBase(String name, FixType type, double latitude, double longitude, List<FixBase> connectedFix) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.connectedFix = connectedFix;
    }
}
