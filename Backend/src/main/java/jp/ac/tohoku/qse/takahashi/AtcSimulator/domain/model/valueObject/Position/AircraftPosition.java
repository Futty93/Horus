package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import java.util.Objects;

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

    public int getAltitude() {
        return altitude;
    }

    public int getGroundSpeed() {
        return groundSpeed;
    }

    public int getVerticalSpeed() {
        return verticalSpeed;
    }

    // equalsとhashCodeをオーバーライド
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AircraftPosition that = (AircraftPosition) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                heading == that.heading &&
                altitude == that.altitude &&
                groundSpeed == that.groundSpeed &&
                verticalSpeed == that.verticalSpeed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, heading, altitude, groundSpeed, verticalSpeed);
    }

    // toStringメソッドをオーバーライド
    @Override
    public String toString() {
        return "AircraftPosition{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", heading=" + heading +
                ", altitude=" + altitude +
                ", groundSpeed=" + groundSpeed +
                ", verticalSpeed=" + verticalSpeed +
                '}';
    }
}