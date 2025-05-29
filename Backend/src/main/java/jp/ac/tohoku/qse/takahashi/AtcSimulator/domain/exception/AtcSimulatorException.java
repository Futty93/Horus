package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception;

/**
 * ATCシミュレーターシステムの基底例外クラス
 *
 * システム内で発生する全ての例外の基底クラスとして機能し、
 * 例外コード、詳細メッセージ、原因例外の情報を保持します。
 */
public abstract class AtcSimulatorException extends RuntimeException {

    /**
     * エラーコード（分類用）
     */
    private final String errorCode;

    /**
     * ユーザー向けメッセージ
     */
    private final String userMessage;

    /**
     * コンストラクタ
     *
     * @param errorCode エラーコード
     * @param message 技術的な詳細メッセージ
     * @param userMessage ユーザー向けメッセージ
     */
    protected AtcSimulatorException(String errorCode, String message, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    /**
     * コンストラクタ（原因例外付き）
     *
     * @param errorCode エラーコード
     * @param message 技術的な詳細メッセージ
     * @param userMessage ユーザー向けメッセージ
     * @param cause 原因例外
     */
    protected AtcSimulatorException(String errorCode, String message, String userMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    /**
     * エラーコードを取得
     *
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * ユーザー向けメッセージを取得
     *
     * @return ユーザー向けメッセージ
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * 例外の種別を取得
     *
     * @return 例外種別
     */
    public abstract ExceptionType getExceptionType();

    /**
     * 例外の種別を表す列挙型
     */
    public enum ExceptionType {
        VALIDATION_ERROR,    // バリデーションエラー
        NOT_FOUND,          // リソース未発見
        CONFLICT,           // 競合状態
        SYSTEM_ERROR,       // システムエラー
        EXTERNAL_API_ERROR  // 外部API呼び出しエラー
    }
}
