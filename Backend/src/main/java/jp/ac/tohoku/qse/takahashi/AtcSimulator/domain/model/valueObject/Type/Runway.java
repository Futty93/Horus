package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type;

public class Runway {
    private final String name; // Runway name (e.g. 34L)
    private final int length;
    private final int width;
    private final int heading;
    private final int elevation;
    private final IlsType ilsType;

    public Runway(String name, int length, int width, int heading, int elevation, IlsType ilsType) {
        this.name = name;
        this.length = length;
        this.width = width;
        this.heading = heading;
        this.elevation = elevation;
        this.ilsType = ilsType;
    }

    public Runway getRunwayInfo() {
        return this;
    }
}
