//package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;
//
//import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
//import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
//import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario.ScenarioService;
//import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
//import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/aircraft")
//public class CreateAircraftService {
//
//    private final ScenarioService scenarioService;
//    private final AircraftRepository aircraftRepository;
//
//    public CreateAircraftService(ScenarioService scenarioService, AircraftRepository aircraftRepository) {
//        this.scenarioService = scenarioService;
//        this.aircraftRepository = aircraftRepository;
//    }
//
//    @RequestMapping(path = "/create", method = RequestMethod.POST)
//    public ResponseEntity<String> createAircraft(CreateAircraftDto createAircraftDto) {
//        if (aircraftRepository.isAircraftExist(new Callsign(createAircraftDto.callsign))) {
//            return ResponseEntity.badRequest().body("Aircraft with callsign " + createAircraftDto.callsign + " already exists.");
//        } else {
//            scenarioService.spawnAircraft(createAircraftDto);
//            return ResponseEntity.ok("Aircraft created:" + createAircraftDto.callsign);
//        }
//    }
//}

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
            summary = "Create a new aircraft",
            description = "This API creates a new aircraft based on the provided DTO.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateAircraftDto.class),
                            examples = @ExampleObject(
                                    value = "{ \"callsign\": \"SKY514\", \"latitude\": 33.99, \"longitude\": 138.613, \"altitude\": 27050, \"groundSpeed\": 400, \"verticalSpeed\": 0, \"heading\": 50, \"type\": \"B738\", \"originIata\": \"OKA\", \"originIcao\": \"ROAH\", \"destinationIata\": \"HND\", \"destinationIcao\": \"RJTT\", \"eta\": \"2024-09-11T12:55:00Z\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Aircraft created successfully",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Aircraft created: SKY514"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Aircraft already exists",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Aircraft with callsign SKY514 already exists."))
                    )
            }
    )
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public ResponseEntity<String> createAircraft(@RequestBody CreateAircraftDto createAircraftDto) {
        if (aircraftRepository.isAircraftExist(new Callsign(createAircraftDto.callsign))) {
            return ResponseEntity.badRequest().body("Aircraft with callsign " + createAircraftDto.callsign + " already exists.");
        } else {
            scenarioService.spawnAircraft(createAircraftDto);
            return ResponseEntity.ok("Aircraft created: " + createAircraftDto.callsign);
        }
    }
}
