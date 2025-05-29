package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.MathUtils;

/**
 * 経度を表すクラス
 * 東経を正、西経を負の値で表し、単位は度
 * 経度は-180度から180度の範囲で表される
 * 時・分・秒ではないので、そちらは利用しないで
 */
public class Longitude {
    final double longitude;

    public Longitude(double longitude) {
        this.longitude = MathUtils.normalizeLongitude(longitude);
    }

    public double toDouble() {
        return this.longitude;
    }

    @Override
    public String toString() {
        return String.valueOf(this.longitude);
    }
}
