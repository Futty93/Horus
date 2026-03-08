package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FixedWingFlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.AltitudeConstraint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlanWaypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.NavigationMode;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.WaypointAction;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.VerticalSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FlightPlan ナビゲーション")
class FlightPlanNavigationTest {

    private static FixPosition fixAt(double lat, double lon) {
        return new FixPosition(new Latitude(lat), new Longitude(lon));
    }

    private AircraftBase createTestAircraft(double lat, double lon, double alt, double heading, double speed) {
        var characteristics = new AircraftCharacteristics(3.0, 3.0, 1640.0, 500.0, 140.0, 42000.0, 0.0,
                AircraftCharacteristics.AircraftCategory.COMMERCIAL_PASSENGER);
        return new AircraftBase(
                new Callsign("TEST01"),
                new AircraftType("B738"),
                new AircraftPosition(new Latitude(lat), new Longitude(lon), new Altitude(alt)),
                new AircraftVector(new Heading(heading), new GroundSpeed(speed), new VerticalSpeed(0)),
                new FixedWingFlightBehavior(),
                characteristics
        ) {
        };
    }

    @Nested
    @DisplayName("setFlightPlan / setDirectTo / setResumeNavigation")
    class FlightPlanStateTest {

        @Test
        void setFlightPlanSetsNavigationModeAndIndex() {
            var aircraft = createTestAircraft(35.0, 139.0, 5000, 0, 250);
            var wp1 = new FlightPlanWaypoint("WP1", fixAt(35.1, 139.0), new Altitude(10000), null, AltitudeConstraint.AT_OR_ABOVE, WaypointAction.CONTINUE);
            var plan = new FlightPlan(new Callsign("TEST01"), "RJTT", "RJOO", List.of(wp1),
                    new Altitude(35000), new GroundSpeed(450));

            aircraft.setFlightPlan(plan);

            assertEquals(NavigationMode.FLIGHT_PLAN, aircraft.getNavigationMode());
            assertEquals(0, aircraft.getCurrentWaypointIndex());
            assertEquals(plan, aircraft.getFlightPlan());
        }

        @Test
        void setDirectToSetsNavigationMode() {
            var aircraft = createTestAircraft(35.0, 139.0, 5000, 0, 250);
            var wp1 = new FlightPlanWaypoint("WP1", fixAt(35.1, 139.0), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
            var plan = new FlightPlan(new Callsign("TEST01"), "RJTT", "RJOO", List.of(wp1),
                    new Altitude(35000), new GroundSpeed(450));
            aircraft.setFlightPlan(plan);

            aircraft.setDirectTo(fixAt(35.5, 139.5), "TARGET", true);

            assertEquals(NavigationMode.DIRECT_TO, aircraft.getNavigationMode());
        }

        @Test
        void setResumeNavigationResetsToFlightPlan() {
            var aircraft = createTestAircraft(35.0, 139.0, 5000, 0, 250);
            var wp1 = new FlightPlanWaypoint("WP1", fixAt(35.1, 139.0), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
            var plan = new FlightPlan(new Callsign("TEST01"), "RJTT", "RJOO", List.of(wp1),
                    new Altitude(35000), new GroundSpeed(450));
            aircraft.setFlightPlan(plan);
            aircraft.setNavigationMode(NavigationMode.HEADING);

            aircraft.setResumeNavigation();

            assertEquals(NavigationMode.FLIGHT_PLAN, aircraft.getNavigationMode());
        }
    }

    @Nested
    @DisplayName("FLIGHT_PLAN 自動ナビゲーション")
    class FlightPlanAutoNavigationTest {

        @Test
        void instructedVectorUpdatesTowardNextWaypoint() {
            var aircraft = createTestAircraft(35.0, 139.0, 5000, 0, 250);
            var wp1 = new FlightPlanWaypoint("WP1", fixAt(35.1, 139.0), new Altitude(10000), null, AltitudeConstraint.AT_OR_ABOVE, WaypointAction.CONTINUE);
            var plan = new FlightPlan(new Callsign("TEST01"), "RJTT", "RJOO", List.of(wp1),
                    new Altitude(35000), new GroundSpeed(450));
            aircraft.setFlightPlan(plan);

            aircraft.calculateNextAircraftVector();

            double instructedHeading = aircraft.getInstructedVector().instructedHeading.toDouble();
            assertTrue(instructedHeading >= 0 && instructedHeading <= 360, "Bearing to north should be ~0");
            assertEquals(10000, aircraft.getInstructedVector().instructedAltitude.toDouble());
        }
    }

    @Nested
    @DisplayName("shouldBeRemovedFromSimulation")
    class RemoveAircraftTest {

        @Test
        void returnsFalseByDefault() {
            var aircraft = createTestAircraft(35.0, 139.0, 5000, 0, 250);
            assertFalse(aircraft.shouldBeRemovedFromSimulation());
        }
    }
}
