package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type;

/**
 * 滑走路情報を表す値オブジェクト
 */
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

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeading() {
        return heading;
    }

    public int getElevation() {
        return elevation;
    }

    public IlsType getIlsType() {
        return ilsType;
    }

    public Runway getRunwayInfo() {
        return this;
    }

    @Override
    public String toString() {
        return String.format("Runway{name='%s', length=%d, width=%d, heading=%d, elevation=%d, ilsType=%s}",
                name, length, width, heading, elevation, ilsType);
    }
}
