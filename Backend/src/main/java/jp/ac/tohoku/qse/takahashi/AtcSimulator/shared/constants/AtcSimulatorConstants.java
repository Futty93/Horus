package jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.constants;

/**
 * ATC simulation domain constants.
 * Placed in shared package so domain and infrastructure can use without depending on config.
 */
public final class AtcSimulatorConstants {

    private AtcSimulatorConstants() {
    }

    public static final int REFRESH_RATE = 1;

    /** Tick interval in milliseconds for simulation (1000 / REFRESH_RATE). Defined explicitly to avoid implicit integer division at call sites. */
    public static final int TICK_INTERVAL_MS = 1000 / REFRESH_RATE;

    public static final double EARTH_RADIUS = 6378.1;
    public static final double KNOTS_TO_KM_PER_HOUR = 1.852;
    public static final double NAUTICAL_MILES_TO_KM = 1.852;
    public static final double FEET_TO_METERS = 0.3048;
    public static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
    public static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;
    public static final double PI_OVER_180 = Math.PI / 180.0;
    public static final double HALF_PI = Math.PI / 2.0;
    public static final double TWO_PI = 2.0 * Math.PI;

    public static final double MINIMUM_HORIZONTAL_SEPARATION = 5.0;
    public static final double MINIMUM_VERTICAL_SEPARATION = 1000.0;
    public static final double MAX_PREDICTION_TIME = 300.0;
    public static final double MAX_CONSIDERATION_DISTANCE = 50.0;

    public static final double SIN_COS_CACHE_PRECISION = 1000.0;
    public static final int SIN_COS_CACHE_SIZE = (int) (360.0 * SIN_COS_CACHE_PRECISION);

    public static final double EARTH_RADIUS_NM = EARTH_RADIUS / NAUTICAL_MILES_TO_KM;
    public static final double EARTH_CIRCUMFERENCE_KM = 2.0 * Math.PI * EARTH_RADIUS;
    public static final double EARTH_CIRCUMFERENCE_NM = EARTH_CIRCUMFERENCE_KM / NAUTICAL_MILES_TO_KM;
    public static final double KM_PER_HOUR_TO_KNOTS = 1.0 / KNOTS_TO_KM_PER_HOUR;
    public static final double KNOTS_TO_METERS_PER_SECOND = KNOTS_TO_KM_PER_HOUR * 1000.0 / 3600.0;
    public static final double FEET_PER_MINUTE_TO_METERS_PER_SECOND = FEET_TO_METERS / 60.0;

    public static final double POSITION_UPDATE_THRESHOLD = 0.1;
    public static final double HEADING_UPDATE_THRESHOLD = 0.1;
    public static final double SPEED_UPDATE_THRESHOLD = 0.1;
    public static final double ALTITUDE_UPDATE_THRESHOLD = 5.0;

    public static final double FLOATING_POINT_EPSILON = 1e-9;
    public static final double ANGULAR_EPSILON = 1e-6;
    public static final double DISTANCE_EPSILON = 1e-3;

    public static final int POSITION_CACHE_SIZE = 1000;
    public static final int VECTOR_POOL_SIZE = 500;
}
