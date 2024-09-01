package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

/**
 * 方位を表すクラス
 * 北を0度とし、時計回りに360度まで表す
 */
public class Heading {
    final double heading;

    public Heading(double heading) {
        this.heading = normalizeHeading(heading);
    }

    /**
     * 入力された方位を0度から360度の範囲に正規化する
     */
    private double normalizeHeading(final double heading) {
        // Normalize heading to the range [0, 360)
        return ((heading % 360) + 360) % 360;
    }

    /**
     * 方位を変更する
     *
     * @param headingDelta　変更する方位
     * @return　変更後の方位
     */
    public Heading changeHeading(double headingDelta) {
        return new Heading(this.heading + headingDelta);
    }
}
