package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

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
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

class FinalApproachInterceptTest {

    @Test
    void unrelatedDirectToClearsFinalApproachPendingFlag() throws Exception {
        AircraftBase aircraft = createTestAircraft();
        aircraft.joinFinalApproachIntercept(
                new FixPosition(new Latitude(35.1), new Longitude(139.1)),
                "RJTT/16R-FA",
                163);
        assertTrue(pendingFlag(aircraft));

        aircraft.setDirectTo(new FixPosition(new Latitude(35.2), new Longitude(139.2)), "OTHER", false);
        assertFalse(pendingFlag(aircraft));
        assertEquals(NavigationMode.DIRECT_TO, aircraft.getNavigationMode());
    }

    private static boolean pendingFlag(AircraftBase aircraft) throws Exception {
        Field field = AircraftBase.class.getDeclaredField("finalApproachCoursePending");
        field.setAccessible(true);
        return field.getBoolean(aircraft);
    }

    private static AircraftBase createTestAircraft() {
        var characteristics = new AircraftCharacteristics(3.0, 3.0, 1640.0, 500.0, 140.0, 42000.0, 0.0,
                AircraftCharacteristics.AircraftCategory.COMMERCIAL_PASSENGER);
        return new AircraftBase(
                new Callsign("TEST01"),
                new AircraftType("B738"),
                new AircraftPosition(new Latitude(35.0), new Longitude(139.0), new Altitude(5000)),
                new AircraftVector(new Heading(0), new GroundSpeed(250), new VerticalSpeed(0)),
                new FixedWingFlightBehavior(),
                characteristics) {
        };
    }
}
