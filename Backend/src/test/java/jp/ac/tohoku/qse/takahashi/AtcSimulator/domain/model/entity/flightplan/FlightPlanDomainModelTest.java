package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

@DisplayName("FlightPlan ドメインモデル")
class FlightPlanDomainModelTest {

    private static FixPosition fixAt(double lat, double lon) {
        return new FixPosition(new Latitude(lat), new Longitude(lon));
    }

    @Nested
    @DisplayName("NavigationMode")
    class NavigationModeTest {
        @Test
        void allModesExist() {
            assertEquals(3, NavigationMode.values().length);
            assertNotNull(NavigationMode.valueOf("FLIGHT_PLAN"));
            assertNotNull(NavigationMode.valueOf("HEADING"));
            assertNotNull(NavigationMode.valueOf("DIRECT_TO"));
        }
    }

    @Nested
    @DisplayName("AltitudeConstraint")
    class AltitudeConstraintTest {
        @Test
        void allConstraintsExist() {
            assertTrue(AltitudeConstraint.values().length >= 4);
            assertNotNull(AltitudeConstraint.valueOf("AT"));
            assertNotNull(AltitudeConstraint.valueOf("AT_OR_ABOVE"));
            assertNotNull(AltitudeConstraint.valueOf("AT_OR_BELOW"));
            assertNotNull(AltitudeConstraint.valueOf("NONE"));
        }
    }

    @Nested
    @DisplayName("WaypointAction")
    class WaypointActionTest {
        @Test
        void allActionsExist() {
            assertNotNull(WaypointAction.valueOf("CONTINUE"));
            assertNotNull(WaypointAction.valueOf("REMOVE_AIRCRAFT"));
            assertNotNull(WaypointAction.valueOf("HANDOFF"));
        }
    }

    @Nested
    @DisplayName("FlightPlanWaypoint")
    class FlightPlanWaypointTest {
        @Test
        void createsWaypointWithAllFields() {
            var wp = new FlightPlanWaypoint(
                    "ABENO",
                    fixAt(34.59, 135.69),
                    new Altitude(10000),
                    new GroundSpeed(250),
                    AltitudeConstraint.AT_OR_ABOVE,
                    WaypointAction.CONTINUE
            );
            assertEquals("ABENO", wp.getFixName());
            assertEquals(10000, wp.getTargetAltitude().toDouble());
            assertEquals(250, wp.getTargetSpeed().toDouble());
            assertEquals(AltitudeConstraint.AT_OR_ABOVE, wp.getAltitudeConstraint());
            assertEquals(WaypointAction.CONTINUE, wp.getAction());
            assertTrue(wp.hasAltitudeConstraint());
            assertTrue(wp.hasSpeedConstraint());
            assertFalse(wp.shouldRemoveAircraft());
        }

        @Test
        void createsWaypointWithoutOptionalConstraints() {
            var wp = new FlightPlanWaypoint(
                    "MAIKO",
                    fixAt(34.5, 135.5),
                    null,
                    null,
                    null,
                    null
            );
            assertFalse(wp.hasAltitudeConstraint());
            assertFalse(wp.hasSpeedConstraint());
            assertEquals(AltitudeConstraint.NONE, wp.getAltitudeConstraint());
            assertEquals(WaypointAction.CONTINUE, wp.getAction());
        }

        @Test
        void shouldRemoveAircraftReturnsTrueForRemoveAction() {
            var wp = new FlightPlanWaypoint(
                    "RJOO",
                    fixAt(34.78, 135.44),
                    null,
                    null,
                    AltitudeConstraint.NONE,
                    WaypointAction.REMOVE_AIRCRAFT
            );
            assertTrue(wp.shouldRemoveAircraft());
        }

        @Test
        void equalsAndHashCode() {
            var wp1 = new FlightPlanWaypoint("ABENO", fixAt(34.59, 135.69), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
            var wp2 = new FlightPlanWaypoint("ABENO", fixAt(34.59, 135.69), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
            assertEquals(wp1, wp2);
            assertEquals(wp1.hashCode(), wp2.hashCode());
        }
    }

    @Nested
    @DisplayName("FlightPlan")
    class FlightPlanTest {
        private FlightPlan createSamplePlan() {
            var wp1 = new FlightPlanWaypoint("WP1", fixAt(35.0, 139.0), new Altitude(5000), null, AltitudeConstraint.AT_OR_ABOVE, WaypointAction.CONTINUE);
            var wp2 = new FlightPlanWaypoint("WP2", fixAt(35.5, 139.5), new Altitude(35000), new GroundSpeed(450), AltitudeConstraint.AT, WaypointAction.CONTINUE);
            var wp3 = new FlightPlanWaypoint("WP3", fixAt(36.0, 140.0), null, null, AltitudeConstraint.NONE, WaypointAction.REMOVE_AIRCRAFT);
            return new FlightPlan(
                    new Callsign("JAL512"),
                    "RJTT",
                    "RJOO",
                    List.of(wp1, wp2, wp3),
                    new Altitude(35000),
                    new GroundSpeed(450)
            );
        }

        @Test
        void createsPlanWithAllFields() {
            var plan = createSamplePlan();
            assertEquals("JAL512", plan.getCallsign().getCallsign());
            assertEquals("RJTT", plan.getDepartureAirport());
            assertEquals("RJOO", plan.getArrivalAirport());
            assertEquals(3, plan.getWaypoints().size());
            assertEquals(35000, plan.getCruiseAltitude().toDouble());
            assertEquals(450, plan.getCruiseSpeed().toDouble());
        }

        @Test
        void getNextWaypointReturnsCorrectWaypoint() {
            var plan = createSamplePlan();
            Optional<FlightPlanWaypoint> wp0 = plan.getNextWaypoint(0);
            assertTrue(wp0.isPresent());
            assertEquals("WP1", wp0.get().getFixName());

            Optional<FlightPlanWaypoint> wp2 = plan.getNextWaypoint(2);
            assertTrue(wp2.isPresent());
            assertEquals("WP3", wp2.get().getFixName());
        }

        @Test
        void getNextWaypointReturnsEmptyForInvalidIndex() {
            var plan = createSamplePlan();
            assertTrue(plan.getNextWaypoint(-1).isEmpty());
            assertTrue(plan.getNextWaypoint(3).isEmpty());
        }

        @Test
        void getWaypointByNameFindsCorrectWaypoint() {
            var plan = createSamplePlan();
            Optional<FlightPlanWaypoint> found = plan.getWaypointByName("WP2");
            assertTrue(found.isPresent());
            assertEquals("WP2", found.get().getFixName());
        }

        @Test
        void getWaypointByNameReturnsEmptyForUnknown() {
            var plan = createSamplePlan();
            assertTrue(plan.getWaypointByName("UNKNOWN").isEmpty());
        }

        @Test
        void findWaypointIndexReturnsCorrectIndex() {
            var plan = createSamplePlan();
            assertEquals(0, plan.findWaypointIndex("WP1"));
            assertEquals(1, plan.findWaypointIndex("WP2"));
            assertEquals(2, plan.findWaypointIndex("WP3"));
            assertEquals(-1, plan.findWaypointIndex("UNKNOWN"));
        }

        @Test
        void isLastWaypoint() {
            var plan = createSamplePlan();
            assertFalse(plan.isLastWaypoint(0));
            assertFalse(plan.isLastWaypoint(1));
            assertTrue(plan.isLastWaypoint(2));
            assertFalse(plan.isLastWaypoint(-1));
            assertFalse(plan.isLastWaypoint(3));
        }

        @Test
        void hasWaypoints() {
            var plan = createSamplePlan();
            assertTrue(plan.hasWaypoints());
        }

        @Test
        void emptyWaypointsListIsValid() {
            var plan = new FlightPlan(
                    new Callsign("TEST"),
                    "RJTT",
                    "RJOO",
                    List.of(),
                    new Altitude(35000),
                    new GroundSpeed(450)
            );
            assertFalse(plan.hasWaypoints());
            assertTrue(plan.getNextWaypoint(0).isEmpty());
        }
    }
}
