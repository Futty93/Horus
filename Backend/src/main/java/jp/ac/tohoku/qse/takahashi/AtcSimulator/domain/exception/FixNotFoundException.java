package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception;

/**
 * 指定された Fix が見つからない場合の例外
 */
public class FixNotFoundException extends AtcSimulatorException {

    private static final String ERROR_CODE = "FIX_NOT_FOUND";

    public FixNotFoundException(String fixName) {
        super(
            ERROR_CODE,
            String.format("Fix '%s' was not found in waypoints", fixName),
            String.format("Fix '%s' が見つかりません", fixName)
        );
    }

    public FixNotFoundException(String fixName, Throwable cause) {
        super(
            ERROR_CODE,
            String.format("Fix '%s' was not found in waypoints", fixName),
            String.format("Fix '%s' が見つかりません", fixName),
            cause
        );
    }

    @Override
    public ExceptionType getExceptionType() {
        return ExceptionType.NOT_FOUND;
    }
}
