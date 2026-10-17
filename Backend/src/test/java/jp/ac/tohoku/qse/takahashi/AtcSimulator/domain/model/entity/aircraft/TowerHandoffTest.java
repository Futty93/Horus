package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.ApproachControlReleasedException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FixedWingFlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.NavigationMode;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.VerticalSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

class TowerHandoffTest {

    @Test
    void establishedAircraftWithinTenMilesIsHandedOffWithoutChangingTargets() {
        AircraftBase aircraft = createTestAircraft();
        InstructedVector before = aircraft.getInstructedVector();

        aircraft.beginTowerHandoff(35.0, 139.08);

        assertTrue(aircraft.isTowerHandoffActive());
        assertEquals(NavigationMode.HEADING, aircraft.getNavigationMode());
        assertEquals(before.instructedHeading.toDouble(), aircraft.getInstructedVector().instructedHeading.toDouble());
        assertEquals(before.instructedAltitude.toDouble(), aircraft.getInstructedVector().instructedAltitude.toDouble());
        assertFalse(aircraft.shouldBeRemovedFromSimulation());
    }

    @Test
    void headingThatMissesTheThresholdIsRejected() {
        AircraftBase aircraft = createTestAircraft();
        aircraft.setAircraftPosition(
                new AircraftPosition(new Latitude(35.6327), new Longitude(139.7511), new Altitude(3000)));
        aircraft.setAircraftVector(new AircraftVector(new Heading(143), new GroundSpeed(180), new VerticalSpeed(0)));
        aircraft.setInstructedVector(
                new InstructedVector(new Heading(143), new Altitude(3000), new GroundSpeed(180)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> aircraft.beginTowerHandoff(35.5530, 139.7810));
        assertTrue(ex.getMessage().contains("does not reach"));
        assertFalse(aircraft.isTowerHandoffActive());
    }

    @Test
    void instructedHeadingThatTurnsAwayFromTheThresholdIsRejected() {
        AircraftBase aircraft = createTestAircraft();
        aircraft.setInstructedVector(
                new InstructedVector(new Heading(0), new Altitude(5000), new GroundSpeed(180)));

        assertThrows(IllegalArgumentException.class, () -> aircraft.beginTowerHandoff(35.0, 139.08));
        assertFalse(aircraft.isTowerHandoffActive());
    }

    @Test
    void nearThresholdStillRequiresHeadingMode() {
        AircraftBase aircraft = createTestAircraft();
        aircraft.setAircraftPosition(
                new AircraftPosition(new Latitude(35.002), new Longitude(139.08), new Altitude(5000)));
        aircraft.setNavigationMode(NavigationMode.DIRECT_TO);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> aircraft.beginTowerHandoff(35.0, 139.08));
        assertTrue(ex.getMessage().contains("final approach course"));
        assertFalse(aircraft.isTowerHandoffActive());
    }

    @Test
    void indicatedAirspeedTurnThatMissesTheThresholdIsRejected() {
        AircraftBase aircraft = new AircraftBase(
                new Callsign("TWRIAS"),
                new AircraftType("B738"),
                new AircraftPosition(new Latitude(35.03), new Longitude(139.00), new Altitude(10000)),
                new AircraftVector(new Heading(270), new GroundSpeed(180), new VerticalSpeed(0)),
                new FixedWingFlightBehavior(),
                new AircraftCharacteristics(3.0, 3.0, 1640.0, 500.0, 140.0, 42000.0, 0.0,
                        AircraftCharacteristics.AircraftCategory.COMMERCIAL_PASSENGER)) {
        };
        aircraft.setInstructedVector(
                new InstructedVector(new Heading(90), new Altitude(10000), new GroundSpeed(180)),
                200);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> aircraft.beginTowerHandoff(35.00, 139.08));
        assertTrue(ex.getMessage().contains("does not reach"));
        assertFalse(aircraft.isTowerHandoffActive());
    }

    @Test
    void acceptedIndicatedAirspeedAircraftIsRemovedByTheSimulator() {
        CommercialAircraft aircraft = new CommercialAircraft(
                new Callsign("TWRIA2"),
                new AircraftType("B738"),
                new AircraftPosition(new Latitude(35.0), new Longitude(139.0), new Altitude(3000)),
                new AircraftVector(new Heading(90), new GroundSpeed(180), new VerticalSpeed(0)),
                "HND", "RJTT", "HND", "RJTT", "2026-10-17T06:00:00Z");
        aircraft.setInstructedVector(
                new InstructedVector(new Heading(90), new Altitude(3000), new GroundSpeed(180)),
                180);
        aircraft.beginTowerHandoff(35.0, 139.08);
        assertTrue(aircraft.isTowerHandoffActive());

        boolean removed = false;
        for (int second = 0; second < 400 && !removed; second++) {
            aircraft.calculateNextAircraftVector(1.0);
            aircraft.calculateNextAircraftPosition(1.0);
            removed = aircraft.shouldBeRemovedFromSimulation();
        }
        assertTrue(removed);
    }

    @Test
    void acceptedCommercialAircraftIsRemovedByTheSimulator() {
        CommercialAircraft aircraft = new CommercialAircraft(
                new Callsign("TWR99"),
                new AircraftType("B738"),
                new AircraftPosition(new Latitude(35.0), new Longitude(139.0), new Altitude(3000)),
                new AircraftVector(new Heading(90), new GroundSpeed(180), new VerticalSpeed(0)),
                "HND", "RJTT", "HND", "RJTT", "2026-10-17T06:00:00Z");
        aircraft.beginTowerHandoff(35.0, 139.08);
        assertTrue(aircraft.isTowerHandoffActive());

        boolean removed = false;
        for (int second = 0; second < 400 && !removed; second++) {
            aircraft.calculateNextAircraftVector(1.0);
            aircraft.calculateNextAircraftPosition(1.0);
            removed = aircraft.shouldBeRemovedFromSimulation();
        }
        assertTrue(removed);
    }

    @Test
    void offCenterlineIsRejected() {
        AircraftBase aircraft = createTestAircraft();
        assertThrows(IllegalArgumentException.class, () -> aircraft.beginTowerHandoff(35.1, 139.0));
        assertFalse(aircraft.isTowerHandoffActive());
    }

    @Test
    void headingInstructionAfterHandoffIsRejected() {
        AircraftBase aircraft = createTestAircraft();
        aircraft.beginTowerHandoff(35.0, 139.08);
        assertThrows(ApproachControlReleasedException.class, () -> aircraft.setInstructedVector(
                new InstructedVector(new Heading(180), new Altitude(4000), new GroundSpeed(160))));
        assertEquals(90, aircraft.getInstructedVector().instructedHeading.toDouble(), 0.01);
    }

    @Test
    void reachingTheThresholdMarksRemoval() {
        AircraftBase aircraft = createTestAircraft();
        aircraft.beginTowerHandoff(35.0, 139.08);
        assertFalse(aircraft.markTowerHandoffArrival());

        aircraft.setAircraftPosition(
                new AircraftPosition(new Latitude(35.0), new Longitude(139.08), new Altitude(5000)));
        assertTrue(aircraft.markTowerHandoffArrival());
        assertTrue(aircraft.shouldBeRemovedFromSimulation());
    }

    private static AircraftBase createTestAircraft() {
        var characteristics = new AircraftCharacteristics(3.0, 3.0, 1640.0, 500.0, 140.0, 42000.0, 0.0,
                AircraftCharacteristics.AircraftCategory.COMMERCIAL_PASSENGER);
        return new AircraftBase(
                new Callsign("TEST01"),
                new AircraftType("B738"),
                new AircraftPosition(new Latitude(35.0), new Longitude(139.0), new Altitude(5000)),
                new AircraftVector(new Heading(90), new GroundSpeed(180), new VerticalSpeed(0)),
                new FixedWingFlightBehavior(),
                characteristics) {
        };
    }
}
