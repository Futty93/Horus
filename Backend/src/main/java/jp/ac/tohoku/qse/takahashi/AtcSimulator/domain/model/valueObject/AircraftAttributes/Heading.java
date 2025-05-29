package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.MathUtils;

/**
 * 方位を表すクラス
 * 北を0度とし、時計回りに360度まで表す
 */
public class Heading {
    final double heading;

    public Heading(double heading) {
        this.heading = MathUtils.normalizeAngle(heading);
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

    public double toDouble() {
        return this.heading;
    }

    @Override
    public String toString() {
        return String.valueOf(this.heading);
    }
}
