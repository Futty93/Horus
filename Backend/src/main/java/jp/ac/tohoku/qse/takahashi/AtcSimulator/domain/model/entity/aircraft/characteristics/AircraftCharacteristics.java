package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics;

/**
 * 航空機の物理的特性を表す値オブジェクト
 * 各航空機タイプ固有の性能パラメータを格納
 */
public class AircraftCharacteristics {

    /** 最大加速度（knots/second） */
    private final double maxAcceleration;

    /** 最大旋回速度（degrees/second） */
    private final double maxTurnRate;

    /** 最大上昇/降下速度（feet/minute） */
    private final double maxClimbRate;

    /** 最高速度（knots） */
    private final double maxSpeed;

    /** 最低速度（knots） */
    private final double minSpeed;

    /** 最高運用高度（feet） */
    private final double maxAltitude;

    /** 最低運用高度（feet） */
    private final double minAltitude;

    /** 航空機カテゴリ */
    private final AircraftCategory category;

    public AircraftCharacteristics(double maxAcceleration, double maxTurnRate, double maxClimbRate,
                                 double maxSpeed, double minSpeed, double maxAltitude, double minAltitude,
                                 AircraftCategory category) {
        this.maxAcceleration = maxAcceleration;
        this.maxTurnRate = maxTurnRate;
        this.maxClimbRate = maxClimbRate;
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;
        this.maxAltitude = maxAltitude;
        this.minAltitude = minAltitude;
        this.category = category;
    }

    // Getters
    public double getMaxAcceleration() { return maxAcceleration; }
    public double getMaxTurnRate() { return maxTurnRate; }
    public double getMaxClimbRate() { return maxClimbRate; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getMinSpeed() { return minSpeed; }
    public double getMaxAltitude() { return maxAltitude; }
    public double getMinAltitude() { return minAltitude; }
    public AircraftCategory getCategory() { return category; }

    /**
     * 航空機カテゴリの列挙型
     */
    public enum AircraftCategory {
        COMMERCIAL_PASSENGER("商用旅客機"),
        COMMERCIAL_CARGO("商用貨物機"),
        MILITARY_FIGHTER("軍用戦闘機"),
        MILITARY_CARGO("軍用貨物機"),
        HELICOPTER("ヘリコプター");

        private final String description;

        AircraftCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return String.format("AircraftCharacteristics{category=%s, maxSpeed=%.1f, maxTurnRate=%.1f, maxClimbRate=%.1f}",
                category.getDescription(), maxSpeed, maxTurnRate, maxClimbRate);
    }
}
