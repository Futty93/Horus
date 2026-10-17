package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject;

/**
 * Wake turbulence separation category (training simplification, ICAO-inspired).
 */
public enum WakeTurbulenceCategory {
    HEAVY,
    MEDIUM,
    LIGHT;

    public static WakeTurbulenceCategory fromAircraftType(String aircraftType) {
        if (aircraftType == null || aircraftType.isBlank()) {
            return MEDIUM;
        }
        String t = aircraftType.toUpperCase().trim();
        if (matchesHeavy(t)) {
            return HEAVY;
        }
        if (matchesLight(t)) {
            return LIGHT;
        }
        return MEDIUM;
    }

    private static boolean matchesHeavy(String t) {
        return t.startsWith("A124") || t.startsWith("A306") || t.startsWith("A30B")
                || t.startsWith("A332") || t.startsWith("A333") || t.startsWith("A343")
                || t.startsWith("A346") || t.startsWith("A359") || t.startsWith("A388")
                || t.startsWith("B742") || t.startsWith("B744") || t.startsWith("B748")
                || t.startsWith("B762") || t.startsWith("B763") || t.startsWith("B764")
                || t.startsWith("B772") || t.startsWith("B773") || t.startsWith("B777")
                || t.startsWith("B77W") || t.startsWith("B787") || t.startsWith("B788")
                || t.startsWith("B789") || t.startsWith("MD11");
    }

    private static boolean matchesLight(String t) {
        return t.startsWith("C172") || t.startsWith("C182") || t.startsWith("DA40");
    }
}
