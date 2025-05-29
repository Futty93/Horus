package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception;

/**
 * 同一コールサインの航空機が既に存在する場合の例外
 */
public class AircraftConflictException extends AtcSimulatorException {

    private static final String ERROR_CODE = "AIRCRAFT_ALREADY_EXISTS";

    /**
     * コンストラクタ
     *
     * @param callsign 重複したコールサイン
     */
    public AircraftConflictException(String callsign) {
        super(
            ERROR_CODE,
            String.format("Aircraft with callsign '%s' already exists in the system", callsign),
            String.format("航空機 '%s' は既に存在します", callsign)
        );
    }

    /**
     * コンストラクタ（原因例外付き）
     *
     * @param callsign 重複したコールサイン
     * @param cause 原因例外
     */
    public AircraftConflictException(String callsign, Throwable cause) {
        super(
            ERROR_CODE,
            String.format("Aircraft with callsign '%s' already exists in the system", callsign),
            String.format("航空機 '%s' は既に存在します", callsign),
            cause
        );
    }

    @Override
    public ExceptionType getExceptionType() {
        return ExceptionType.CONFLICT;
    }
}
