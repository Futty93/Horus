package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
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

    @RequestMapping(path = "/airports", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getAirports() {
        return atsRouteFixPositionRepository.getAirportsForApi();
    }

    @RequestMapping(path = "/route/all", method = RequestMethod.GET)
    public String getAtsRouteInfo() {
        return atsRouteFixPositionRepository.getRouteInfo();
    }

    @RequestMapping(path = "/route/suggest", method = RequestMethod.GET)
    public ResponseEntity<Object> suggestRoute(
            @RequestParam(name = "origin") String origin,
            @RequestParam(name = "destination") String destination) {
        // Defensive check: @RequestParam(required=true) makes Spring return 400 before this is
        // reached when params are missing. Kept for safety if the API contract changes.
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", "origin and destination are required"));
        }
        var result = routeSuggestionService.suggestRouteWithReason(origin, destination);
        Map<String, Object> body = new HashMap<>();
        body.put("waypoints", result.waypoints());
        if (result.waypoints().isEmpty()) {
            body.put("reason", result.reason());
        }
        return ResponseEntity.ok(body);
    }
}
