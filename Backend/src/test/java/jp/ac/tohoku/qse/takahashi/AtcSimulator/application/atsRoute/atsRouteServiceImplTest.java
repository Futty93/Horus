package jp.ac.tohoku.qse.takahashi.AtcSimulator.application.atsRoute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class atsRouteServiceImplTest {

    private final AtsRouteServiceImpl atsRouteService = new AtsRouteServiceImpl();

    public atsRouteServiceImplTest() throws IOException {
    }

    @Test
    @DisplayName("Fixが取得できる")
    void CanGetAllFix() {
        var fixes = atsRouteService.toString();
        System.out.println(fixes);
        assertNotEquals("", fixes);
    }
}
