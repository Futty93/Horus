package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

class WaypointAltitudeTargetResolverTest {

    private static FlightPlanWaypoint wp(AltitudeConstraint constraint, int altFt) {
        return new FlightPlanWaypoint(
                "WP1",
                new FixPosition(new Latitude(35.0), new Longitude(139.0)),
                new Altitude(altFt),
                null,
                constraint,
                WaypointAction.CONTINUE);
    }

    @Test
    void atOrAboveKeepsCurrentWhenAboveConstraint() {
        var wp = wp(AltitudeConstraint.AT_OR_ABOVE, 10_000);
        assertEquals(12_000, WaypointAltitudeTargetResolver.resolveTarget(wp, 12_000).toDouble());
    }

    @Test
    void atOrBelowTargetsConstraintForClimb() {
        var wp = wp(AltitudeConstraint.AT_OR_BELOW, 10_000);
        assertEquals(10_000, WaypointAltitudeTargetResolver.resolveTarget(wp, 5_000).toDouble());
    }

    @Test
    void satisfiesAtOrAboveAtPass() {
        var wp = wp(AltitudeConstraint.AT_OR_ABOVE, 10_000);
        assertTrue(WaypointAltitudeTargetResolver.satisfiesConstraintAtPass(wp, 10_200));
        assertFalse(WaypointAltitudeTargetResolver.satisfiesConstraintAtPass(wp, 9_000));
    }
}
