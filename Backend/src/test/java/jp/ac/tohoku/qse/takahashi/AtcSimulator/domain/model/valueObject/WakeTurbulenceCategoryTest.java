package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WakeTurbulenceCategoryTest {

    @Test
    void b787IsHeavy() {
        assertEquals(WakeTurbulenceCategory.HEAVY, WakeTurbulenceCategory.fromAircraftType("B787"));
    }

    @Test
    void unknownDefaultsToMedium() {
        assertEquals(WakeTurbulenceCategory.MEDIUM, WakeTurbulenceCategory.fromAircraftType("ZZ999"));
    }

    @Test
    void e190IsMediumNotLight() {
        assertEquals(WakeTurbulenceCategory.MEDIUM, WakeTurbulenceCategory.fromAircraftType("E190"));
    }

    @Test
    void c172IsLight() {
        assertEquals(WakeTurbulenceCategory.LIGHT, WakeTurbulenceCategory.fromAircraftType("C172"));
    }
}
