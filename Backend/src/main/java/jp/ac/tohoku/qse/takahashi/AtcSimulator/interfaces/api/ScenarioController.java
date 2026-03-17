package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.constants.AtcSimulatorConstants.DEFAULT_SIMULATION_ETA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftFactory;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.FlightPlanFromDtoConverter;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix.AtsRouteFixPositionRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.FlightPlanDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.InitialPositionDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ScenarioLoadDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.SpawnWithFlightPlanDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/scenario")
public class ScenarioController {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);

    private final ScenarioService scenarioService;
    private final AircraftRepository aircraftRepository;
    private final FlightPlanFromDtoConverter flightPlanConverter;
    private final AtsRouteFixPositionRepository airportRepository;

    public ScenarioController(ScenarioService scenarioService, AircraftRepository aircraftRepository,
            FlightPlanFromDtoConverter flightPlanConverter, AtsRouteFixPositionRepository airportRepository) {
        this.scenarioService = scenarioService;
        this.aircraftRepository = aircraftRepository;
        this.flightPlanConverter = flightPlanConverter;
        this.airportRepository = airportRepository;
    }

    @PostMapping(path = "/load", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> loadScenario(
            @Valid @RequestBody ScenarioLoadDto dto) {
        if (dto.aircraft() == null || dto.aircraft().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "aircraft array is required and must not be empty"));
        }

        Set<String> seenCallsigns = new HashSet<>();
        for (SpawnWithFlightPlanDto ac : dto.aircraft()) {
            String cs = ac.flightPlan().callsign();
            if (!seenCallsigns.add(cs)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Duplicate callsign: " + cs));
            }
        }

        // Validate FlightPlan of aircraft that will be spawned; skip those without initialPosition
        for (SpawnWithFlightPlanDto ac : dto.aircraft()) {
            if (ac.initialPosition() == null) continue;
            try {
                flightPlanConverter.toDomain(ac.flightPlan());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", e.getMessage()));
            }
        }

        GlobalVariables.isSimulationRunning = false;
        aircraftRepository.clear();

        int count = 0;
        for (SpawnWithFlightPlanDto ac : dto.aircraft()) {
            if (ac.initialPosition() == null) {
                logger.warn("Skipping aircraft {}: missing initialPosition", ac.flightPlan().callsign());
                continue;
            }
            FlightPlan fp = flightPlanConverter.toDomain(ac.flightPlan());
            var aircraft = createAircraftFromSpawn(ac.flightPlan(), ac.initialPosition());
            aircraft.setFlightPlan(fp);
            scenarioService.spawnAircraft(aircraft);
            count++;
        }

        logger.info("Scenario loaded: {} aircraft, scenarioName={}", count, dto.scenarioName());
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("scenarioName", dto.scenarioName());
        body.put("aircraftCount", count);
        body.put("message", "Scenario loaded successfully");
        return ResponseEntity.ok(body);
    }

    private CommercialAircraft createAircraftFromSpawn(FlightPlanDto fpDto, InitialPositionDto pos) {
        String dep = fpDto.departureAirport() != null ? fpDto.departureAirport() : "RJTT";
        String arr = fpDto.arrivalAirport() != null ? fpDto.arrivalAirport() : "RJAA";
        String originIata = airportRepository.findIataByIcao(dep);
        String destIata = airportRepository.findIataByIcao(arr);
        return AircraftFactory.createCommercialAircraft(
                new CreateAircraftDto(
                        fpDto.callsign(),
                        pos.latitude(),
                        pos.longitude(),
                        pos.altitude(),
                        pos.groundSpeed(),
                        pos.verticalSpeed(),
                        pos.heading(),
                        fpDto.aircraftType() != null ? fpDto.aircraftType() : "B738",
                        originIata,
                        dep,
                        destIata,
                        arr,
                        DEFAULT_SIMULATION_ETA.toString()));
    }
}
