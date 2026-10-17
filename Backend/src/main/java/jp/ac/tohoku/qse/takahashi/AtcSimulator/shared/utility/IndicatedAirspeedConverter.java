package jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility;

/**
 * Converts indicated airspeed (IAS) to true airspeed (TAS) under standard atmosphere, no wind.
 * Used for terminal speed instructions until a wind model exists (phase 6-1).
 */
public final class IndicatedAirspeedConverter {

    private IndicatedAirspeedConverter() {
    }

    /**
     * @param indicatedAirspeedKnots IAS in knots
     * @param pressureAltitudeFeet     aircraft altitude used as pressure altitude (ft)
     * @return TAS in knots; equals IAS at sea level
     */
    public static double indicatedToTrueAirspeedKnots(double indicatedAirspeedKnots, double pressureAltitudeFeet) {
        if (indicatedAirspeedKnots <= 0) {
            return 0;
        }
        double altitude = Math.max(0, pressureAltitudeFeet);
        double densityRatio = Math.pow(1.0 - 0.0000068756 * altitude, 5.2561);
        if (densityRatio <= 0) {
            return indicatedAirspeedKnots;
        }
        return indicatedAirspeedKnots / Math.sqrt(densityRatio);
    }
}
