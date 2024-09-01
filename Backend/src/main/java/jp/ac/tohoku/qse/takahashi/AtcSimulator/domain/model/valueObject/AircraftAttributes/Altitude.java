package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

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
}
