package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

/**
 * 経度を表すクラス
 * 東経を正、西経を負の値で表し、単位は度
 * 経度は-180度から180度の範囲で表される
 * 時・分・秒ではないので、そちらは利用しないで
 */
public class Longitude {
    final double longitude;

    public Longitude(double longitude) {
        this.longitude = normalizeLongitude(longitude);
    }

    /**
     * 入力された経度を-180度から180度の範囲に正規化する
     */
    private double normalizeLongitude(final double longitude) {
        // Normalize longitude to the range [-180, 180)
        return ((longitude + 180) % 360 + 360) % 360 - 180;
    }
}
