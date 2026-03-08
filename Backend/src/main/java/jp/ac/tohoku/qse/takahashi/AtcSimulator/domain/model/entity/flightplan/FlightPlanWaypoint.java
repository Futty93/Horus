package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

import java.util.Objects;

/**
 * フライトプラン上の1つのウェイポイント。
 * Fix 名、位置、高度・速度制約、通過時アクションを保持する。
 */
public final class FlightPlanWaypoint {
    private final String fixName;
    private final FixPosition position;
    private final Altitude targetAltitude;
    private final GroundSpeed targetSpeed;
    private final AltitudeConstraint altitudeConstraint;
    private final WaypointAction action;

    public FlightPlanWaypoint(
            String fixName,
            FixPosition position,
            Altitude targetAltitude,
            GroundSpeed targetSpeed,
            AltitudeConstraint altitudeConstraint,
            WaypointAction action) {
        this.fixName = Objects.requireNonNull(fixName, "fixName");
        this.position = Objects.requireNonNull(position, "position");
        this.targetAltitude = targetAltitude;
        this.targetSpeed = targetSpeed;
        this.altitudeConstraint = altitudeConstraint != null ? altitudeConstraint : AltitudeConstraint.NONE;
        this.action = action != null ? action : WaypointAction.CONTINUE;
    }

    public String getFixName() {
        return fixName;
    }

    public FixPosition getPosition() {
        return position;
    }

    public Altitude getTargetAltitude() {
        return targetAltitude;
    }

    public GroundSpeed getTargetSpeed() {
        return targetSpeed;
    }

    public AltitudeConstraint getAltitudeConstraint() {
        return altitudeConstraint;
    }

    public WaypointAction getAction() {
        return action;
    }

    public boolean hasAltitudeConstraint() {
        return altitudeConstraint != AltitudeConstraint.NONE && targetAltitude != null;
    }

    public boolean hasSpeedConstraint() {
        return targetSpeed != null;
    }

    public boolean shouldRemoveAircraft() {
        return action == WaypointAction.REMOVE_AIRCRAFT;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FlightPlanWaypoint that = (FlightPlanWaypoint) obj;
        return Objects.equals(fixName, that.fixName)
                && Objects.equals(position, that.position)
                && Objects.equals(targetAltitude, that.targetAltitude)
                && Objects.equals(targetSpeed, that.targetSpeed)
                && altitudeConstraint == that.altitudeConstraint
                && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fixName, position, targetAltitude, targetSpeed, altitudeConstraint, action);
    }
}
