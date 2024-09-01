package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

/**
 * 垂直速度を表すクラス
 * 垂直速度はフィート毎分で表される
 */
public class VerticalSpeed {
    final double verticalSpeed;

    public VerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    /**
     * 垂直速度を変更する
     *
     * @param verticalSpeedDelta　変更する垂直速度
     * @return　変更後の垂直速度
     */
    public VerticalSpeed changeVerticalSpeed(double verticalSpeedDelta) {
        return new VerticalSpeed(this.verticalSpeed + verticalSpeedDelta);
    }
}
