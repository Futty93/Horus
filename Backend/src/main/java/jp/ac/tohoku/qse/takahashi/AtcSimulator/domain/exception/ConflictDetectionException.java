package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception;

/**
 * コンフリクト検出処理中にエラーが発生した場合の例外
 */
public class ConflictDetectionException extends AtcSimulatorException {

    private static final String ERROR_CODE = "CONFLICT_DETECTION_ERROR";

    /**
     * コンストラクタ
     *
     * @param message 詳細メッセージ
     */
    public ConflictDetectionException(String message) {
        super(
            ERROR_CODE,
            String.format("Conflict detection failed: %s", message),
            "コンフリクト検出処理中にエラーが発生しました"
        );
    }

    /**
     * コンストラクタ（原因例外付き）
     *
     * @param message 詳細メッセージ
     * @param cause 原因例外
     */
    public ConflictDetectionException(String message, Throwable cause) {
        super(
            ERROR_CODE,
            String.format("Conflict detection failed: %s", message),
            "コンフリクト検出処理中にエラーが発生しました",
            cause
        );
    }

    @Override
    public ExceptionType getExceptionType() {
        return ExceptionType.SYSTEM_ERROR;
    }
}
