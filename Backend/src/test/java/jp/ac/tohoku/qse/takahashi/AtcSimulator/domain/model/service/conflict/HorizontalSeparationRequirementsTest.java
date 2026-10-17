package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.WakeTurbulenceCategory;

class HorizontalSeparationRequirementsTest {

    @Test
    void wakeTableHeavyToLight() {
        assertEquals(6.0, HorizontalSeparationRequirements.wakeSeparationTableNm(
                WakeTurbulenceCategory.HEAVY, WakeTurbulenceCategory.LIGHT));
    }
}
