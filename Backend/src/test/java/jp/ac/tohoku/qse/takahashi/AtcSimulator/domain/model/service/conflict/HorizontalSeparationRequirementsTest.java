package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FixedWingFlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.VerticalSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.WakeTurbulenceCategory;

class HorizontalSeparationRequirementsTest {

    @Test
    void wakeTableHeavyToLight() {
        assertEquals(6.0, HorizontalSeparationRequirements.wakeSeparationTableNm(
                WakeTurbulenceCategory.HEAVY, WakeTurbulenceCategory.LIGHT));
    }

    @Test
    void terminalAreaPairUsesThreeNmWhenNoWake() {
        AircraftBase a1 = aircraftAt(35.55, 139.78, "B738");
        AircraftBase a2 = aircraftAt(35.56, 139.79, "B738");
        assertEquals(3.0, HorizontalSeparationRequirements.requiredHorizontalSeparationNm(a1, a2), 0.01);
    }

    @Test
    void enroutePairUsesFiveNmWhenNoWake() {
        AircraftBase a1 = aircraftAt(36.5, 141.0, "B738");
        AircraftBase a2 = aircraftAt(36.51, 141.01, "B738");
        assertEquals(5.0, HorizontalSeparationRequirements.requiredHorizontalSeparationNm(a1, a2), 0.01);
    }

    private static AircraftBase aircraftAt(double lat, double lon, String type) {
        var characteristics = new AircraftCharacteristics(3.0, 3.0, 1640.0, 500.0, 140.0, 42000.0, 0.0,
                AircraftCharacteristics.AircraftCategory.COMMERCIAL_PASSENGER);
        return new AircraftBase(
                new Callsign("T1"),
                new AircraftType(type),
                new AircraftPosition(new Latitude(lat), new Longitude(lon), new Altitude(10_000)),
                new AircraftVector(new Heading(90), new GroundSpeed(250), new VerticalSpeed(0)),
                new FixedWingFlightBehavior(),
                characteristics) {
        };
    }
}
