package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;

public final class WaypointAltitudeTargetResolver {

    private WaypointAltitudeTargetResolver() {
    }

    public static Altitude resolveTarget(FlightPlanWaypoint waypoint, double currentAltitudeFeet) {
        if (waypoint.getTargetAltitude() == null) {
            return null;
        }
        double constraintAlt = waypoint.getTargetAltitude().toDouble();
        return switch (waypoint.getAltitudeConstraint()) {
            case NONE, AT, BETWEEN -> new Altitude(constraintAlt);
            case AT_OR_ABOVE -> new Altitude(
                    currentAltitudeFeet >= constraintAlt ? currentAltitudeFeet : constraintAlt);
            case AT_OR_BELOW -> new Altitude(constraintAlt);
        };
    }

    public static boolean satisfiesConstraintAtPass(FlightPlanWaypoint waypoint, double altitudeFeet) {
        if (!waypoint.hasAltitudeConstraint() || waypoint.getTargetAltitude() == null) {
            return true;
        }
        double target = waypoint.getTargetAltitude().toDouble();
        return switch (waypoint.getAltitudeConstraint()) {
            case NONE -> true;
            case AT, BETWEEN -> Math.abs(altitudeFeet - target) <= 500;
            case AT_OR_ABOVE -> altitudeFeet + 100 >= target;
            case AT_OR_BELOW -> altitudeFeet - 100 <= target;
        };
    }
}
