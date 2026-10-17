package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject;

/**
 * Wake turbulence separation category (training simplification).
 */
public enum WakeTurbulenceCategory {
    HEAVY,
    MEDIUM,
    LIGHT;

    public static WakeTurbulenceCategory fromAircraftType(String aircraftType) {
        if (aircraftType == null || aircraftType.isBlank()) {
            return LIGHT;
        }
        String t = aircraftType.toUpperCase().trim();
        if (t.startsWith("A388") || t.startsWith("A359") || t.startsWith("B748")
                || t.startsWith("B772") || t.startsWith("B773") || t.startsWith("B777")
                || t.startsWith("B789") || t.startsWith("A333") || t.startsWith("A343")) {
            return HEAVY;
        }
        if (t.startsWith("B738") || t.startsWith("B739") || t.startsWith("B737")
                || t.startsWith("A320") || t.startsWith("A321") || t.startsWith("A319")
                || t.startsWith("B763") || t.startsWith("A306")) {
            return MEDIUM;
        }
        return LIGHT;
    }
}
