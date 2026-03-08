package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.AltitudeConstraint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlanWaypoint;
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

@DisplayName("CommercialAircraft フライトプラン リグレッション")
class CommercialAircraftFlightPlanRegressionTest {

    private static FixPosition fixAt(double lat, double lon) {
        return new FixPosition(new Latitude(lat), new Longitude(lon));
    }

    private CommercialAircraft createAircraft(double lat, double lon, double alt, double heading, double speed) {
        return new CommercialAircraft(
                new Callsign("TEST01"),
                new AircraftType("B738"),
                new AircraftPosition(new Latitude(lat), new Longitude(lon), new Altitude(alt)),
                new AircraftVector(new Heading(heading), new GroundSpeed(speed), new VerticalSpeed(0)),
                "HND", "RJTT", "ITM", "RJOO", "2024-12-13T14:30:00Z"
        );
    }

    @Nested
    @DisplayName("Direct To 発行時に旋回開始")
    class DirectToTurnRegression {

        @Test
        @DisplayName("DIRECT_TO時はshouldUpdateVectorがtrueを返し、instructedVectorが目標fix向きに更新される")
        void directTo_updatesInstructedVectorTowardTarget() {
            var aircraft = createAircraft(34.4, 138.54, 5000, 270, 250);
            var targetFix = fixAt(34.65, 138.61);
            aircraft.setDirectTo(targetFix, "UNAGI", false);

            aircraft.calculateNextAircraftVector();

            double instructedHdg = aircraft.getInstructedVector().instructedHeading.toDouble();
            assertThat(instructedHdg).isBetween(0.0, 360.0);
            assertThat(Math.abs(instructedHdg - 270.0)).isGreaterThan(5.0);
        }
    }

    @Nested
    @DisplayName("ウェイポイント通過後に次fixへ旋回")
    class WaypointPassTurnRegression {

        @Test
        @DisplayName("FLIGHT_PLAN時、currentWaypointIndex=1のときinstructedVectorがwaypoint1向きに更新される")
        void afterWaypointPass_instructedVectorPointsToNextWaypoint() {
            var wp0 = new FlightPlanWaypoint("KOITO", fixAt(34.4046, 138.5381), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
            var wp1 = new FlightPlanWaypoint("UNAGI", fixAt(34.6544, 138.6113), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
            var plan = new FlightPlan(new Callsign("TEST01"), "RJTT", "RJOO", List.of(wp0, wp1),
                    new Altitude(10000), new GroundSpeed(250));

            var aircraft = createAircraft(34.40, 138.53, 5000, 270, 250);
            aircraft.setFlightPlan(plan);
            aircraft.setCurrentWaypointIndex(1);

            aircraft.calculateNextAircraftVector();

            double instructedHdg = aircraft.getInstructedVector().instructedHeading.toDouble();
            assertThat(instructedHdg).isBetween(0.0, 360.0);
            assertThat(Math.abs(instructedHdg - 270.0)).isGreaterThan(5.0);
        }
    }

    @Nested
    @DisplayName("ウェイポイント通過検出")
    class WaypointPassDetectionRegression {

        @Test
        @DisplayName("positionをwaypoint通過後に設定するとcurrentWaypointIndexが進む")
        void waypointPass_detectedWhenMovingAwayWithinThreshold() throws Exception {
            var wp0 = new FlightPlanWaypoint("KOITO", fixAt(34.4046, 138.5381), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
            var wp1 = new FlightPlanWaypoint("UNAGI", fixAt(34.6544, 138.6113), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
            var plan = new FlightPlan(new Callsign("TEST01"), "RJTT", "RJOO", List.of(wp0, wp1),
                    new Altitude(10000), new GroundSpeed(250));

            var aircraft = createAircraft(34.4046, 138.5381, 5000, 270, 250);
            aircraft.setFlightPlan(plan);

            var field = AircraftBase.class.getDeclaredField("previousDistanceToWaypoint");
            field.setAccessible(true);
            field.set(aircraft, 0.1);

            aircraft.setAircraftPosition(new AircraftPosition(
                    new Latitude(34.4046), new Longitude(138.52), new Altitude(5000)));

            aircraft.calculateNextAircraftVector();

            assertThat(aircraft.getCurrentWaypointIndex()).isEqualTo(1);
        }
    }
}
