package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.SimulationTiming;
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
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.IndicatedAirspeedConverter;

class CommercialAircraftIndicatedAirspeedTest {

    @Test
    void switchingToIasAtSameNumericSpeedAcceleratesTowardConvertedTarget() {
        CommercialAircraft aircraft = new CommercialAircraft(
                new Callsign("IAS01"),
                new AircraftType("B738"),
                new AircraftPosition(new Latitude(35.0), new Longitude(139.0), new Altitude(10_000)),
                new AircraftVector(new Heading(90), new GroundSpeed(250), new VerticalSpeed(0)),
                "HND", "RJTT", "NRT", "RJAA", "2026-01-01T00:00:00Z");
        aircraft.setNavigationMode(NavigationMode.HEADING);
        aircraft.setInstructedVector(new InstructedVector(
                new Heading(90), new Altitude(10_000), new GroundSpeed(250)), null);

        double before = aircraft.getAircraftVector().groundSpeed.toDouble();
        aircraft.setInstructedVector(new InstructedVector(
                new Heading(90), new Altitude(10_000), new GroundSpeed(250)), 250);
        aircraft.calculateNextAircraftVector(SimulationTiming.SIM_DELTA_SECONDS);

        double expectedTarget = IndicatedAirspeedConverter.indicatedToTrueAirspeedKnots(250, 10_000);
        double after = aircraft.getAircraftVector().groundSpeed.toDouble();
        assertTrue(expectedTarget > before + 5, "expected IAS target above current GS");
        assertTrue(after > before, "GS should increase toward IAS-converted target");
    }
}
