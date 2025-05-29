package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception;

/**
 * 指定されたコールサインの航空機が見つからない場合の例外
 */
public class AircraftNotFoundException extends AtcSimulatorException {

    private static final String ERROR_CODE = "AIRCRAFT_NOT_FOUND";

    /**
     * コンストラクタ
     *
     * @param callsign 見つからなかった航空機のコールサイン
     */
    public AircraftNotFoundException(String callsign) {
        super(
            ERROR_CODE,
            String.format("Aircraft with callsign '%s' was not found in the system", callsign),
            String.format("航空機 '%s' が見つかりません", callsign)
        );
    }

    /**
     * コンストラクタ（原因例外付き）
     *
     * @param callsign 見つからなかった航空機のコールサイン
     * @param cause 原因例外
     */
    public AircraftNotFoundException(String callsign, Throwable cause) {
        super(
            ERROR_CODE,
            String.format("Aircraft with callsign '%s' was not found in the system", callsign),
            String.format("航空機 '%s' が見つかりません", callsign),
            cause
        );
    }

    @Override
    public ExceptionType getExceptionType() {
        return ExceptionType.NOT_FOUND;
    }
}
