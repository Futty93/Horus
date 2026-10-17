package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.ApproachControlReleasedException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FixedWingFlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
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

        aircraft.beginTowerHandoff(35.0, 139.08, 90);

        assertTrue(aircraft.isTowerHandoffActive());
        assertEquals(NavigationMode.HEADING, aircraft.getNavigationMode());
        assertEquals(before.instructedHeading.toDouble(), aircraft.getInstructedVector().instructedHeading.toDouble());
        assertEquals(before.instructedAltitude.toDouble(), aircraft.getInstructedVector().instructedAltitude.toDouble());
        assertFalse(aircraft.shouldBeRemovedFromSimulation());
    }

    @Test
    void offCenterlineIsRejected() {
        AircraftBase aircraft = createTestAircraft();
        assertThrows(IllegalArgumentException.class, () -> aircraft.beginTowerHandoff(35.1, 139.0, 90));
        assertFalse(aircraft.isTowerHandoffActive());
    }

    @Test
    void headingInstructionAfterHandoffIsRejected() {
        AircraftBase aircraft = createTestAircraft();
        aircraft.beginTowerHandoff(35.0, 139.08, 90);
        assertThrows(ApproachControlReleasedException.class, () -> aircraft.setInstructedVector(
                new InstructedVector(new Heading(180), new Altitude(4000), new GroundSpeed(160))));
        assertEquals(90, aircraft.getInstructedVector().instructedHeading.toDouble(), 0.01);
    }

    @Test
    void reachingTheThresholdMarksRemoval() {
        AircraftBase aircraft = createTestAircraft();
        aircraft.beginTowerHandoff(35.0, 139.08, 90);
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
