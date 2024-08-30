package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

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

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public ResponseEntity<String> createAircraft(CreateAircraftDto createAircraftDto) {
        if (aircraftRepository.isAircraftExist(new Callsign(createAircraftDto.callsign))) {
            return ResponseEntity.badRequest().body("Aircraft with callsign " + createAircraftDto.callsign + " already exists.");
        } else {
            scenarioService.spawnAircraft(createAircraftDto);
            return ResponseEntity.ok("Aircraft created:" + createAircraftDto.callsign);
        }
    }
}
