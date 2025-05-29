package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception;

/**
 * パラメータのバリデーションに失敗した場合の例外
 */
public class InvalidParameterException extends AtcSimulatorException {

    private static final String ERROR_CODE = "INVALID_PARAMETER";

    /**
     * コンストラクタ
     *
     * @param parameterName パラメータ名
     * @param value 無効な値
     * @param reason 理由
     */
    public InvalidParameterException(String parameterName, Object value, String reason) {
        super(
            ERROR_CODE,
            String.format("Invalid parameter '%s' with value '%s': %s", parameterName, value, reason),
            String.format("パラメータ '%s' の値が無効です: %s", parameterName, reason)
        );
    }

    /**
     * コンストラクタ（単純なメッセージ）
     *
     * @param message エラーメッセージ
     */
    public InvalidParameterException(String message) {
        super(
            ERROR_CODE,
            message,
            "入力パラメータが無効です"
        );
    }

    /**
     * コンストラクタ（原因例外付き）
     *
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public InvalidParameterException(String message, Throwable cause) {
        super(
            ERROR_CODE,
            message,
            "入力パラメータが無効です",
            cause
        );
    }

    @Override
    public ExceptionType getExceptionType() {
        return ExceptionType.VALIDATION_ERROR;
    }
}
