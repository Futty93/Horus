package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception;

/**
 * The approach seat has already transferred this aircraft to the tower.
 */
public class ApproachControlReleasedException extends AtcSimulatorException {

    private static final String ERROR_CODE = "APPROACH_CONTROL_RELEASED";
    private static final String MESSAGE = "Aircraft has been handed off to the tower";

    public ApproachControlReleasedException(String callsign) {
        super(ERROR_CODE, MESSAGE + ": " + callsign, MESSAGE);
    }

    @Override
    public ExceptionType getExceptionType() {
        return ExceptionType.CONFLICT;
    }
}
