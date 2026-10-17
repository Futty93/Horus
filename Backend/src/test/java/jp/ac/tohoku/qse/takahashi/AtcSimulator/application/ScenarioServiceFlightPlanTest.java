package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.ApproachControlReleasedException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
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
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;

@SpringBootTest
class ScenarioServiceFlightPlanTest {

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private AircraftRepository aircraftRepository;

    private static FixPosition fixAt(double lat, double lon) {
        return new FixPosition(new Latitude(lat), new Longitude(lon));
    }

    @BeforeEach
    void setUp() {
        Aircraft aircraft = AircraftFactory.createCommercialAircraft(
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto(
                        "FP01", 35.0, 139.0, 5000, 250, 0, 90,
                        "B738", "HND", "RJTT", "ITM", "RJOO", "2024-12-13T14:30:00Z"
                ));
        scenarioService.spawnAircraft(aircraft);
    }

    @AfterEach
    void tearDown() {
        try {
            Aircraft a = aircraftRepository.findByCallsign(new Callsign("FP01"));
            aircraftRepository.remove(a);
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("directToFix with resumeFlightPlan=false sets DIRECT_TO mode")
    void directToFix_setsDirectToMode() {
        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign("FP01"));
        scenarioService.directToFix(new Callsign("FP01"), "ABENO", false);

        assertThat(((AircraftBase) aircraft).getNavigationMode()).isEqualTo(NavigationMode.DIRECT_TO);
    }

    @Test
    @DisplayName("directToFix with resumeFlightPlan=true sets DIRECT_TO mode")
    void directToFix_withResume_setsDirectToMode() {
        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign("FP01"));
        var wp = new FlightPlanWaypoint("ABENO", fixAt(34.59, 135.69), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
        var plan = new FlightPlan(new Callsign("FP01"), "RJTT", "RJOO", List.of(wp), new Altitude(35000), new GroundSpeed(450));
        ((AircraftBase) aircraft).setFlightPlan(plan);

        scenarioService.directToFix(new Callsign("FP01"), "ABENO", true);

        assertThat(((AircraftBase) aircraft).getNavigationMode()).isEqualTo(NavigationMode.DIRECT_TO);
    }

    @Test
    @DisplayName("resumeNavigation sets FLIGHT_PLAN when aircraft has flight plan")
    void resumeNavigation_setsFlightPlanMode() {
        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign("FP01"));
        var wp = new FlightPlanWaypoint("WP1", fixAt(35.1, 139.0), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
        var plan = new FlightPlan(new Callsign("FP01"), "RJTT", "RJOO", List.of(wp), new Altitude(35000), new GroundSpeed(450));
        ((AircraftBase) aircraft).setFlightPlan(plan);
        ((AircraftBase) aircraft).setNavigationMode(NavigationMode.HEADING);

        scenarioService.resumeNavigation(new Callsign("FP01"));

        assertThat(((AircraftBase) aircraft).getNavigationMode()).isEqualTo(NavigationMode.FLIGHT_PLAN);
    }

    @Test
    @DisplayName("instructAircraft sets HEADING mode")
    void instructAircraft_setsHeadingMode() {
        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign("FP01"));
        var wp = new FlightPlanWaypoint("WP1", fixAt(35.1, 139.0), null, null, AltitudeConstraint.NONE, WaypointAction.CONTINUE);
        var plan = new FlightPlan(new Callsign("FP01"), "RJTT", "RJOO", List.of(wp), new Altitude(35000), new GroundSpeed(450));
        ((AircraftBase) aircraft).setFlightPlan(plan);
        assertThat(((AircraftBase) aircraft).getNavigationMode()).isEqualTo(NavigationMode.FLIGHT_PLAN);

        scenarioService.instructAircraft(new Callsign("FP01"),
                new InstructedVector(new Heading(180), new Altitude(10000), new GroundSpeed(250)));

        assertThat(((AircraftBase) aircraft).getNavigationMode()).isEqualTo(NavigationMode.HEADING);
    }

    @Test
    @DisplayName("goAround climbs on runway heading and a later heading instruction clears it")
    void goAround_setsHeadingClimbThenClearsOnInstruct() {
        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign("FP01"));
        scenarioService.goAround(new Callsign("FP01"), 35.553, 139.781, 163, 6000);

        AircraftBase base = (AircraftBase) aircraft;
        assertThat(base.isGoAroundActive()).isTrue();
        assertThat(base.getNavigationMode()).isEqualTo(NavigationMode.HEADING);
        assertThat(base.getInstructedVector().instructedHeading.toDouble()).isEqualTo(163);
        assertThat(base.getInstructedVector().instructedAltitude.toDouble()).isEqualTo(6000);

        scenarioService.instructAircraft(new Callsign("FP01"),
                new InstructedVector(new Heading(90), new Altitude(7000), new GroundSpeed(200)));
        assertThat(base.isGoAroundActive()).isFalse();
    }

    @Test
    @DisplayName("goAround rejects aircraft farther than 120 NM from the threshold")
    void goAround_rejectsWhenTooFar() {
        assertThatThrownBy(() -> scenarioService.goAround(new Callsign("FP01"), 0, 0, 163, 6000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("tower handoff keeps heading mode and rejects a later instruction")
    void handOffToTower_locksApproachInstructions() {
        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign("FP01"));
        scenarioService.handOffToTower(new Callsign("FP01"), 35.0, 139.08);

        AircraftBase base = (AircraftBase) aircraft;
        assertThat(base.isTowerHandoffActive()).isTrue();
        assertThat(base.getNavigationMode()).isEqualTo(NavigationMode.HEADING);

        assertThatThrownBy(() -> scenarioService.instructAircraft(new Callsign("FP01"),
                new InstructedVector(new Heading(180), new Altitude(4000), new GroundSpeed(160))))
                .isInstanceOf(ApproachControlReleasedException.class);
        assertThat(base.getNavigationMode()).isEqualTo(NavigationMode.HEADING);
    }

    @Test
    @DisplayName("tower handoff removes the aircraft on the flight update at the threshold")
    void handOffToTower_removesWhenThresholdIsReached() {
        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign("FP01"));
        scenarioService.handOffToTower(new Callsign("FP01"), 35.0, 139.08);
        AircraftBase base = (AircraftBase) aircraft;
        base.calculateNextAircraftVector(1.0);
        assertThat(base.shouldBeRemovedFromSimulation()).isFalse();

        base.setAircraftPosition(new AircraftPosition(new Latitude(35.0), new Longitude(139.08), new Altitude(5000)));
        base.calculateNextAircraftVector(1.0);
        assertThat(base.shouldBeRemovedFromSimulation()).isTrue();
    }

    @Test
    @DisplayName("tower handoff rejects an aircraft that is still intercepting")
    void handOffToTower_rejectsWhenNotEstablished() {
        scenarioService.directToFix(new Callsign("FP01"), "ABENO", false);
        assertThatThrownBy(() -> scenarioService.handOffToTower(new Callsign("FP01"), 35.0, 139.08))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("final approach course");
    }
}
