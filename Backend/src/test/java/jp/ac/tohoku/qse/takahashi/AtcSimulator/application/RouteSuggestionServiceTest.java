package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RadioNavigationAid;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Route;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.RoutePoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.util.Waypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix.AtsRouteFixPositionRepository;

@DisplayName("RouteSuggestionService")
class RouteSuggestionServiceTest {

    private AtsRouteFixPositionRepository repository;

    private static Waypoint wp(String name, double lat, double lon) {
        return new Waypoint(name, lat, lon, "waypoint");
    }

    private static Route route(String name, RoutePoint... points) {
        return new Route(name, "test", List.of(points));
    }

    private static RoutePoint rp(String name, double lat, double lon) {
        return new RoutePoint(name, lat, lon, "waypoint");
    }

    @BeforeEach
    void setUp() {
        repository = mock(AtsRouteFixPositionRepository.class);
    }

    @Nested
    @DisplayName("suggestRoute")
    class SuggestRouteTest {

        @Test
        @DisplayName("returns linear path A→B→C for simple chain graph")
        void returnsPathForLinearGraph() {
            Waypoint a = wp("A", 35.0, 139.0);
            Waypoint b = wp("B", 35.5, 139.5);
            Waypoint c = wp("C", 36.0, 139.0);

            when(repository.getWaypoints()).thenReturn(List.of(a, b, c));
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of(
                    route("R1", rp("A", 35.0, 139.0), rp("B", 35.5, 139.5), rp("C", 36.0, 139.0))));
            when(repository.getRnavRoutes()).thenReturn(List.<Route>of());
            when(repository.findAirportPositionByIcao("ORIG")).thenReturn(Optional.of(new double[] { 35.0, 139.0 }));
            when(repository.findAirportPositionByIcao("DEST")).thenReturn(Optional.of(new double[] { 36.0, 139.0 }));

            RouteSuggestionService service = new RouteSuggestionService(repository);
            List<String> result = service.suggestRoute("ORIG", "DEST");

            assertThat(result).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("returns shortest path when multiple routes exist (diamond graph)")
        void returnsShortestPathForDiamondGraph() {
            Waypoint a = wp("A", 35.0, 139.0);
            Waypoint b = wp("B", 35.5, 139.2);
            Waypoint c = wp("C", 35.5, 138.8);
            Waypoint d = wp("D", 36.0, 139.0);

            when(repository.getWaypoints()).thenReturn(List.of(a, b, c, d));
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of(
                    route("R1", rp("A", 35.0, 139.0), rp("B", 35.5, 139.2), rp("D", 36.0, 139.0)),
                    route("R2", rp("A", 35.0, 139.0), rp("C", 35.5, 138.8), rp("D", 36.0, 139.0))));
            when(repository.getRnavRoutes()).thenReturn(List.<Route>of());
            when(repository.findAirportPositionByIcao("ORIG")).thenReturn(Optional.of(new double[] { 35.0, 139.0 }));
            when(repository.findAirportPositionByIcao("DEST")).thenReturn(Optional.of(new double[] { 36.0, 139.0 }));

            RouteSuggestionService service = new RouteSuggestionService(repository);
            List<String> result = service.suggestRoute("ORIG", "DEST");

            assertThat(result).startsWith("A").endsWith("D");
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("returns empty when origin and destination are same fix")
        void returnsSingleFixWhenSameAirport() {
            Waypoint a = wp("A", 35.0, 139.0);
            when(repository.getWaypoints()).thenReturn(List.of(a));
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of());
            when(repository.getRnavRoutes()).thenReturn(List.<Route>of());
            when(repository.findAirportPositionByIcao("SAME")).thenReturn(Optional.of(new double[] { 35.0, 139.0 }));

            RouteSuggestionService service = new RouteSuggestionService(repository);
            List<String> result = service.suggestRoute("SAME", "SAME");

            assertThat(result).containsExactly("A");
        }

        @Test
        @DisplayName("returns empty when origin airport unknown")
        void returnsEmptyWhenOriginUnknown() {
            when(repository.getWaypoints()).thenReturn(List.of());
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of());
            when(repository.getRnavRoutes()).thenReturn(List.<Route>of());
            when(repository.findAirportPositionByIcao("UNKNOWN")).thenReturn(Optional.empty());
            when(repository.findAirportPositionByIcao("DEST")).thenReturn(Optional.of(new double[] { 36.0, 139.0 }));

            RouteSuggestionService service = new RouteSuggestionService(repository);
            List<String> result = service.suggestRoute("UNKNOWN", "DEST");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when params are null or blank")
        void returnsEmptyWhenParamsInvalid() {
            when(repository.getWaypoints()).thenReturn(List.of());
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of());
            when(repository.getRnavRoutes()).thenReturn(List.<Route>of());

            RouteSuggestionService service = new RouteSuggestionService(repository);

            assertThat(service.suggestRoute(null, "DEST")).isEmpty();
            assertThat(service.suggestRoute("ORIG", null)).isEmpty();
            assertThat(service.suggestRoute("", "DEST")).isEmpty();
            assertThat(service.suggestRoute("ORIG", "  ")).isEmpty();
        }

        @Test
        @DisplayName("builds graph from both atsLowerRoutes and rnavRoutes")
        void mergesAtsAndRnavRoutesIntoGraph() {
            Waypoint a = wp("A", 35.0, 139.0);
            Waypoint b = wp("B", 35.5, 139.0);
            Waypoint c = wp("C", 36.0, 139.0);

            when(repository.getWaypoints()).thenReturn(List.of(a, b, c));
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of(
                    route("ATS1", rp("A", 35.0, 139.0), rp("B", 35.5, 139.0))));
            when(repository.getRnavRoutes()).thenReturn(List.of(
                    route("RNAV1", rp("B", 35.5, 139.0), rp("C", 36.0, 139.0))));
            when(repository.findAirportPositionByIcao("ORIG")).thenReturn(Optional.of(new double[] { 35.0, 139.0 }));
            when(repository.findAirportPositionByIcao("DEST")).thenReturn(Optional.of(new double[] { 36.0, 139.0 }));

            RouteSuggestionService service = new RouteSuggestionService(repository);
            List<String> result = service.suggestRoute("ORIG", "DEST");

            assertThat(result).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("returns empty when nearest fix exceeds 500km threshold")
        void returnsEmptyWhenNearestFixTooFar() {
            Waypoint a = wp("A", 35.0, 139.0);
            when(repository.getWaypoints()).thenReturn(List.of(a));
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of());
            when(repository.getRnavRoutes()).thenReturn(List.<Route>of());
            when(repository.findAirportPositionByIcao("FAR"))
                    .thenReturn(Optional.of(new double[] { 20.0, 139.0 }));
            when(repository.findAirportPositionByIcao("NEAR"))
                    .thenReturn(Optional.of(new double[] { 35.0, 139.0 }));

            RouteSuggestionService service = new RouteSuggestionService(repository);
            assertThat(service.suggestRoute("FAR", "NEAR")).isEmpty();
        }

        @Test
        @DisplayName("returns empty when start and goal are in disconnected subgraphs")
        void returnsEmptyForDisconnectedGraph() {
            Waypoint a = wp("A", 35.0, 139.0);
            Waypoint b = wp("B", 35.5, 139.0);
            Waypoint c = wp("C", 36.0, 140.0);
            Waypoint d = wp("D", 36.5, 140.0);

            when(repository.getWaypoints()).thenReturn(List.of(a, b, c, d));
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of(
                    route("R1", rp("A", 35.0, 139.0), rp("B", 35.5, 139.0)),
                    route("R2", rp("C", 36.0, 140.0), rp("D", 36.5, 140.0))));
            when(repository.getRnavRoutes()).thenReturn(List.<Route>of());
            when(repository.findAirportPositionByIcao("ORIG"))
                    .thenReturn(Optional.of(new double[] { 35.0, 139.0 }));
            when(repository.findAirportPositionByIcao("DEST"))
                    .thenReturn(Optional.of(new double[] { 36.5, 140.0 }));

            RouteSuggestionService service = new RouteSuggestionService(repository);
            assertThat(service.suggestRoute("ORIG", "DEST")).isEmpty();
        }

        @Test
        @DisplayName("skips route points not in waypoints/radioNavAids")
        void skipsOrphanRoutePoints() {
            Waypoint a = wp("A", 35.0, 139.0);
            Waypoint c = wp("C", 36.0, 139.0);

            when(repository.getWaypoints()).thenReturn(List.of(a, c));
            when(repository.getRadioNavigationAids()).thenReturn(List.<RadioNavigationAid>of());
            when(repository.getAtsLowerRoutes()).thenReturn(List.of(
                    route("R1", rp("A", 35.0, 139.0), rp("ORPHAN", 35.5, 139.0), rp("C", 36.0, 139.0))));
            when(repository.getRnavRoutes()).thenReturn(List.<Route>of());
            when(repository.findAirportPositionByIcao("ORIG")).thenReturn(Optional.of(new double[] { 35.0, 139.0 }));
            when(repository.findAirportPositionByIcao("DEST")).thenReturn(Optional.of(new double[] { 36.0, 139.0 }));

            RouteSuggestionService service = new RouteSuggestionService(repository);
            List<String> result = service.suggestRoute("ORIG", "DEST");

            assertThat(result).isEmpty();
        }
    }
}
