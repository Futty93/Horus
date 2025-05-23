package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict;

/**
 * コンフリクトアラートのレベルを表す列挙型
 * 航空管制における危険度の段階を定義
 */
public enum AlertLevel {
    /**
     * 安全 - 管制間隔が確保されている状態
     * 危険度: 0-29
     */
    SAFE("SAFE", 0),

    /**
     * 白コンフリクト - 5分以内に管制間隔欠如の可能性
     * 危険度: 30-69
     */
    WHITE_CONFLICT("WHITE_CONFLICT", 1),

    /**
     * 赤コンフリクト - 1分以内に管制間隔欠如の可能性
     * 危険度: 70-100
     */
    RED_CONFLICT("RED_CONFLICT", 2);

    private final String description;
    private final int priority;

    AlertLevel(String description, int priority) {
        this.description = description;
        this.priority = priority;
    }

    /**
     * 危険度から適切なアラートレベルを決定する
     *
     * @param riskLevel 危険度 (0-100)
     * @return 対応するアラートレベル
     */
    public static AlertLevel fromRiskLevel(double riskLevel) {
        if (riskLevel < 30.0) {
            return SAFE;
        } else if (riskLevel < 70.0) {
            return WHITE_CONFLICT;
        } else {
            return RED_CONFLICT;
        }
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * このアラートレベルが指定されたレベルより高い危険度かどうかを判定
     *
     * @param other 比較対象のアラートレベル
     * @return より高い危険度の場合true
     */
    public boolean isHigherThan(AlertLevel other) {
        return this.priority > other.priority;
    }
}
