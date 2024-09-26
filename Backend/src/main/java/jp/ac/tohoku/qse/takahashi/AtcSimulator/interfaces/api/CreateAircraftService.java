package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/aircraft")
public class CreateAircraftService {

    private final ScenarioService scenarioService;
    private final AircraftRepository aircraftRepository;

    public CreateAircraftService(ScenarioService scenarioService, AircraftRepository aircraftRepository) {
        this.scenarioService = scenarioService;
        this.aircraftRepository = aircraftRepository;
    }

    @Operation(
            summary = "Create multiple new aircraft",
            description = "This API creates multiple aircraft based on the provided DTO array. Aircraft that already exist are skipped.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateAircraftDto[].class),
                            examples = @ExampleObject(
                                    value = "[ { \"callsign\": \"SKY514\", \"latitude\": 34.99, \"longitude\": 139.613, \"altitude\": 27050, \"groundSpeed\": 400, \"verticalSpeed\": 0, \"heading\": 70, \"type\": \"B738\", \"originIata\": \"OKA\", \"originIcao\": \"ROAH\", \"destinationIata\": \"HND\", \"destinationIcao\": \"RJTT\", \"eta\": \"2024-09-11T12:55:00Z\" }, { \"callsign\": \"ANA123\", \"latitude\": 34.86, \"longitude\": 139.764, \"altitude\": 30000, \"groundSpeed\": 450, \"verticalSpeed\": 0, \"heading\": 60, \"type\": \"B777\", \"originIata\": \"ITM\", \"originIcao\": \"RJOO\", \"destinationIata\": \"NRT\", \"destinationIcao\": \"RJAA\", \"eta\": \"2024-09-11T14:10:00Z\" } ]"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Aircraft created successfully",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[\"Aircraft created: SKY514\", \"Aircraft created: JAL123\"]"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "All aircraft already exist",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "All aircraft already exist."))
                    )
            }
    )
    @RequestMapping(path = "/create-multiple", method = RequestMethod.POST)
    public ResponseEntity<List<String>> createMultipleAircraft(@RequestBody List<CreateAircraftDto> createAircraftDtos) {
        List<String> createdAircrafts = new ArrayList<>();
        List<String> existingAircrafts = new ArrayList<>();

        for (CreateAircraftDto dto : createAircraftDtos) {
            if (!aircraftRepository.isAircraftExist(new Callsign(dto.callsign))) {
                scenarioService.spawnAircraft(dto);
                createdAircrafts.add("Aircraft created: " + dto.callsign);
            } else {
                existingAircrafts.add("Aircraft already exists: " + dto.callsign);
            }
        }

        if (createdAircrafts.isEmpty()) {
            return ResponseEntity.badRequest().body(existingAircrafts); // If all aircraft already exist, return 400
        }

        createdAircrafts.addAll(existingAircrafts); // Append existing aircraft info to the response
        return ResponseEntity.ok(createdAircrafts);
    }
}