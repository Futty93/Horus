package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.MathUtils;

/**
 * 緯度を表すクラス
 * 北緯を正、南緯を負の値で表し、単位は度
 * 緯度は-90度から90度の範囲で表される
 * 時・分・秒ではないので、そちらは利用しないで
 */
public class Latitude {
    final double latitude;

    public Latitude(double latitude) {
        this.latitude = MathUtils.normalizeLatitude(latitude);
    }

    public double toDouble() {
        return this.latitude;
    }

    @Override
    public String toString() {
        return String.valueOf(this.latitude);
    }
}
