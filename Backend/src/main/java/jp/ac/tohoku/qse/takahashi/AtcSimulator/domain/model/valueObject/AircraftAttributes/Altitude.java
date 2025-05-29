package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

import java.util.Objects;

/**
 * 高度を表すクラス
 * 高度はフィートで表される
 */
public class Altitude {
    private final double MinAltitude = -1266; // 最低高度 イスラエルのバル・イェホスアフ空港
    final double altitude;

    /**
     * 高度を取得する
     */
    public Altitude(double altitude) {
        if (altitude < -1266) {
            throw new IllegalArgumentException("高度は-1266以上である必要があります");
        }
        this.altitude = altitude;
    }

    /**
     * 高度を変更する
     *
     * @param altitudeDelta　変更する高度
     * @return　変更後の高度
     */
    public Altitude changeAltitude(double altitudeDelta) {
        return new Altitude(this.altitude + altitudeDelta);
    }

    /**
     * 高度を比較する
     *
     * @return　比較後の高度
     */
    public double compareAltitude(final Altitude currentAltitude, final Altitude targetAltitude) {
        return targetAltitude.altitude - currentAltitude.altitude;
    }

    public double toDouble() {
        return this.altitude;
    }

    @Override
    public String toString() {
        return String.valueOf(this.altitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Altitude altitude1 = (Altitude) obj;
        return Double.compare(altitude1.altitude, altitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(altitude);
    }
}
