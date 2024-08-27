package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

/**
 * Represents the position of an aircraft in 3D space.
 *
 * @param latitude the latitude of the aircraft (in degrees)
 * @param longitude the longitude of the aircraft (in degrees)
 * @param altitude the altitude of the aircraft (in feet)
 */
public class AircraftPosition {

    private final double latitude;   // 緯度
    private final double longitude;  // 経度
    private final int heading;       // 機首方向 (度単位、0-360)
    private final int altitude;      // 高度 (フィート)
    private final int groundSpeed;   // 対地速度 (ノット)
    private final int verticalSpeed; // 垂直速度 (フィート/分)

    // コンストラクタ
    public AircraftPosition(double latitude, double longitude, int heading, int altitude, int groundSpeed, int verticalSpeed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.heading = heading;
        this.altitude = altitude;
        this.groundSpeed = groundSpeed;
        this.verticalSpeed = verticalSpeed;
    }

    // ゲッター
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getHeading() {
        return heading;
    }

    public void setLatitude(final double newLatitude) {
        this.latitude = newLatitude;
    }

    public void setLongitude(final double newLongitude) {
        this.longitude = newLongitude;
    }

    public void setAltitude(final double newAltitude) {
        this.altitude = newAltitude;
    }
}