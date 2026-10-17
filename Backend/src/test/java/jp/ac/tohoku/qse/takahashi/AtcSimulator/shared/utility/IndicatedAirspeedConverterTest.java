package jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IndicatedAirspeedConverterTest {

    @Test
    void iasEqualsTasAtSeaLevel() {
        assertEquals(250, IndicatedAirspeedConverter.indicatedToTrueAirspeedKnots(250, 0), 0.5);
    }

    @Test
    void tasIncreasesWithAltitude() {
        double tas = IndicatedAirspeedConverter.indicatedToTrueAirspeedKnots(180, 10000);
        assertTrue(tas > 180);
    }

    @Test
    void referenceTasAtTenThousandFeet() {
        double tas = IndicatedAirspeedConverter.indicatedToTrueAirspeedKnots(180, 10_000);
        assertEquals(209.5, tas, 1.0);
    }
}
