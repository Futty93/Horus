package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.RouteSuggestionService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix.AtsRouteFixPositionRepository;

@RestController
@RequestMapping("/ats")
public class AtsRouteService {
    private final AtsRouteFixPositionRepository atsRouteFixPositionRepository;
    private final RouteSuggestionService routeSuggestionService;

    public AtsRouteService(
            AtsRouteFixPositionRepository atsRouteFixPositionRepository,
            RouteSuggestionService routeSuggestionService) {
        this.atsRouteFixPositionRepository = atsRouteFixPositionRepository;
        this.routeSuggestionService = routeSuggestionService;
    }

    @RequestMapping(path = "/route/all", method = RequestMethod.GET)
    public String getAtsRouteInfo() {
        return atsRouteFixPositionRepository.getRouteInfo();
    }

    @RequestMapping(path = "/route/suggest", method = RequestMethod.GET)
    public ResponseEntity<Object> suggestRoute(
            @RequestParam(name = "origin") String origin,
            @RequestParam(name = "destination") String destination) {
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", "origin and destination are required"));
        }
        List<String> waypoints = routeSuggestionService.suggestRoute(origin, destination);
        return ResponseEntity.ok(Map.of("waypoints", waypoints));
    }
}
