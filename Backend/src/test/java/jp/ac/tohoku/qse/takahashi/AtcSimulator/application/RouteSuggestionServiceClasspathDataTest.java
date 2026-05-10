package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix.AtsRouteFixPositionRepository;

@DisplayName("RouteSuggestionService (classpath fix data)")
class RouteSuggestionServiceClasspathDataTest {

    private static void assertAStarOk(String origin, String destination) {
        AtsRouteFixPositionRepository repository = new AtsRouteFixPositionRepository();
        RouteSuggestionService service = new RouteSuggestionService(repository);
        RouteSuggestionService.SuggestResult result = service.suggestRouteWithReason(origin, destination);
        assertThat(result.waypoints()).as("%s–%s waypoint count", origin, destination).hasSizeGreaterThan(2);
        assertThat(result.reason()).as("%s–%s reason", origin, destination).isEqualTo(RouteSuggestionService.REASON_OK_ASTAR);
    }

    @Test
    @DisplayName("RJSM–RJFK: Kyushu–Honshu template leg uses TESAB bridge")
    void rjsmToRjfk_isConnectedInGraph() {
        assertAStarOk("RJSM", "RJFK");
    }

    @Test
    @DisplayName("RJBB–RJAA: Kansai–Narita (PHLOX vic. bridge)")
    void rjbbToRjaa_isConnectedInGraph() {
        assertAStarOk("RJBB", "RJAA");
    }

    @Test
    @DisplayName("RJOO–RJTT: Itami–Haneda (KAMAT vic. bridge)")
    void rjooToRjtt_isConnectedInGraph() {
        assertAStarOk("RJOO", "RJTT");
    }
}
