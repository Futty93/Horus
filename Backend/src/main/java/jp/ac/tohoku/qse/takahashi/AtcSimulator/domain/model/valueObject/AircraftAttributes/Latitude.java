package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

/**
 * 緯度を表すクラス
 * 北緯を正、南緯を負の値で表し、単位は度
 * 緯度は-90度から90度の範囲で表される
 * 時・分・秒ではないので、そちらは利用しないで
 */
public class Latitude {
    final double latitude;

    public Latitude(double latitude) {
        this.latitude = normalizeLatitude(latitude);
    }

    private double normalizeLatitude(final double latitude) {
        // Normalize latitude to the range [-90, 90]
        return Math.max(-90, Math.min(90, latitude));
    }

    public double toDouble() {
        return this.latitude;
    }
}
