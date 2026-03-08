package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.FlightPlanFromDtoConverter;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftConflictException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlanWaypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.DirectToRequestDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.FlightPlanDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.InitialPositionDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.SpawnWithFlightPlanDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/aircraft")
public class FlightPlanController {

    private static final Logger logger = LoggerFactory.getLogger(FlightPlanController.class);

    private final ScenarioService scenarioService;
    private final AircraftRepository aircraftRepository;
    private final FlightPlanFromDtoConverter flightPlanConverter;

    public FlightPlanController(ScenarioService scenarioService, AircraftRepository aircraftRepository,
                               FlightPlanFromDtoConverter flightPlanConverter) {
        this.scenarioService = scenarioService;
        this.aircraftRepository = aircraftRepository;
        this.flightPlanConverter = flightPlanConverter;
    }

    @Operation(
            summary = "Spawn aircraft with flight plan",
            description = "Creates a new aircraft with initial position and flight plan. Fix names in route must exist in waypoints.json. T09 sector: KOITO, UNAGI, BOKJO, AOIKU."
    )
    @PostMapping(path = "/spawn-with-flightplan", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> spawnWithFlightPlan(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Minimal",
                                    value = "{\"flightPlan\":{\"callsign\":\"TEST01\",\"aircraftType\":\"B738\",\"departureAirport\":\"RJTT\",\"arrivalAirport\":\"RJAA\",\"cruiseAltitude\":10000,\"cruiseSpeed\":250,\"route\":[{\"fix\":\"KOITO\",\"action\":\"CONTINUE\"},{\"fix\":\"UNAGI\",\"action\":\"CONTINUE\"}]},\"initialPosition\":{\"latitude\":34.405,\"longitude\":138.60,\"altitude\":5000,\"heading\":270,\"groundSpeed\":250,\"verticalSpeed\":0}}"
                            )
                    )
            )
            @Valid @RequestBody SpawnWithFlightPlanDto dto) {
        if (dto.initialPosition() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "initialPosition is required"));
        }
        Callsign callsign = new Callsign(dto.flightPlan().callsign());
        if (aircraftRepository.isAircraftExist(callsign)) {
            throw new AircraftConflictException(callsign.toString());
        }

        FlightPlan flightPlan = flightPlanConverter.toDomain(dto.flightPlan());
        Aircraft aircraft = createAircraftWithInitialPosition(dto.flightPlan(), dto.initialPosition());
        ((AircraftBase) aircraft).setFlightPlan(flightPlan);

        scenarioService.spawnAircraft(aircraft);

        logger.info("Aircraft spawned with flight plan: {}", callsign);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "callsign", callsign.toString(),
                "message", "Aircraft spawned with flight plan"
        ));
    }

    @PostMapping(path = "/{callsign}/flightplan", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> assignFlightPlan(
            @PathVariable String callsign,
            @Valid @RequestBody FlightPlanDto flightPlanDto) {
        if (!aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            throw new AircraftNotFoundException(callsign);
        }

        FlightPlanDto dtoWithCallsign = new FlightPlanDto(
                callsign,
                flightPlanDto.aircraftType(),
                flightPlanDto.departureAirport(),
                flightPlanDto.arrivalAirport(),
                flightPlanDto.cruiseAltitude(),
                flightPlanDto.cruiseSpeed(),
                flightPlanDto.route()
        );
        FlightPlan flightPlan = flightPlanConverter.toDomain(dtoWithCallsign);
        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign(callsign));
        ((AircraftBase) aircraft).setFlightPlan(flightPlan);

        logger.info("Flight plan assigned to: {}", callsign);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "callsign", callsign,
                "message", "Flight plan assigned"
        ));
    }

    @Operation(
            summary = "Direct aircraft to fix",
            description = "Instructs aircraft to fly direct to the specified fix. Set resumeFlightPlan=true to resume flight plan after reaching the fix."
    )
    @PostMapping(path = "/{callsign}/direct-to", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> directTo(
            @PathVariable String callsign,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"fixName\":\"UNAGI\",\"resumeFlightPlan\":true}")
                    )
            )
            @Valid @RequestBody DirectToRequestDto request) {
        if (!aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            throw new AircraftNotFoundException(callsign);
        }

        scenarioService.directToFix(new Callsign(callsign), request.fixName(), request.resumeFlightPlan());

        logger.info("Direct to {} for {} (resume={})", request.fixName(), callsign, request.resumeFlightPlan());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "callsign", callsign,
                "targetFix", request.fixName(),
                "navigationMode", "DIRECT_TO"
        ));
    }

    @PostMapping(path = "/{callsign}/resume-navigation")
    public ResponseEntity<Map<String, Object>> resumeNavigation(@PathVariable String callsign) {
        if (!aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            throw new AircraftNotFoundException(callsign);
        }

        scenarioService.resumeNavigation(new Callsign(callsign));

        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign(callsign));
        String nextWaypoint = ((AircraftBase) aircraft).getFlightPlan() != null
                ? ((AircraftBase) aircraft).getFlightPlan()
                .getNextWaypoint(((AircraftBase) aircraft).getCurrentWaypointIndex())
                .map(FlightPlanWaypoint::getFixName)
                .orElse(null)
                : null;

        logger.info("Resume navigation for {} nextWaypoint={}", callsign, nextWaypoint);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("callsign", callsign);
        body.put("navigationMode", "FLIGHT_PLAN");
        if (nextWaypoint != null) {
            body.put("nextWaypoint", nextWaypoint);
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping(path = "/{callsign}/flightplan", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getFlightPlan(@PathVariable String callsign) {
        if (!aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            throw new AircraftNotFoundException(callsign);
        }

        Aircraft aircraft = aircraftRepository.findByCallsign(new Callsign(callsign));
        AircraftBase base = (AircraftBase) aircraft;
        FlightPlan plan = base.getFlightPlan();

        if (plan == null) {
            return ResponseEntity.ok(Map.of(
                    "callsign", callsign,
                    "navigationMode", base.getNavigationMode().name(),
                    "hasFlightPlan", false
            ));
        }

        List<String> remainingWaypoints = plan.getWaypoints().stream()
                .skip(base.getCurrentWaypointIndex())
                .map(FlightPlanWaypoint::getFixName)
                .collect(Collectors.toList());

        String currentWaypoint = plan.getNextWaypoint(base.getCurrentWaypointIndex())
                .map(FlightPlanWaypoint::getFixName)
                .orElse(null);

        Map<String, Object> body = new HashMap<>();
        body.put("callsign", callsign);
        body.put("navigationMode", base.getNavigationMode().name());
        body.put("currentWaypointIndex", base.getCurrentWaypointIndex());
        body.put("currentWaypoint", currentWaypoint);
        body.put("remainingWaypoints", remainingWaypoints);
        body.put("departureAirport", plan.getDepartureAirport());
        body.put("arrivalAirport", plan.getArrivalAirport());
        return ResponseEntity.ok(body);
    }

    private Aircraft createAircraftWithInitialPosition(FlightPlanDto fpDto, InitialPositionDto pos) {
        return jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftFactory.createCommercialAircraft(
                new jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto(
                        fpDto.callsign(),
                        pos.latitude(),
                        pos.longitude(),
                        pos.altitude(),
                        pos.groundSpeed(),
                        pos.verticalSpeed(),
                        pos.heading(),
                        fpDto.aircraftType() != null ? fpDto.aircraftType() : "B738",
                        fpDto.departureAirport() != null && fpDto.departureAirport().length() >= 3 ? fpDto.departureAirport().substring(0, 3) : (fpDto.departureAirport() != null ? fpDto.departureAirport() : "HND"),
                        fpDto.departureAirport() != null ? fpDto.departureAirport() : "RJTT",
                        fpDto.arrivalAirport() != null && fpDto.arrivalAirport().length() >= 3 ? fpDto.arrivalAirport().substring(0, 3) : (fpDto.arrivalAirport() != null ? fpDto.arrivalAirport() : "ITM"),
                        fpDto.arrivalAirport() != null ? fpDto.arrivalAirport() : "RJOO",
                        "2024-12-13T14:30:00Z"
                )
        );
    }
}
