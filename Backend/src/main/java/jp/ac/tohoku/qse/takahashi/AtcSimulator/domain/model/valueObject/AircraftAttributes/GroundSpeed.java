package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes;

/**
 * 地上速度を表すクラス
 * 地上速度はノットで表される
 */
public class GroundSpeed {
    final double groundSpeed;

    public GroundSpeed(double groundSpeed) {
        if (groundSpeed < 0) {
            throw new IllegalArgumentException("地上速度は0以上である必要があります");
        }
        this.groundSpeed = groundSpeed;
    }

    /**
     * 地上速度を変更する
     *
     * @param groundSpeedDelta　変更する地上速度
     * @return　変更後の地上速度
     */
    public GroundSpeed changeGroundSpeed(double groundSpeedDelta) {
        return new GroundSpeed(this.groundSpeed + groundSpeedDelta);
    }

    public double toDouble() {
        return this.groundSpeed;
    }

    @Override
    public String toString() {
        return String.valueOf(this.groundSpeed);
    }
}
