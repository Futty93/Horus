package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;


import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ControlAircraftDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aircraft/control")
public class ControlAircraftService {

    private final AircraftRepository aircraftRepository;
    private final ScenarioService scenarioService;

    public ControlAircraftService(AircraftRepository aircraftRepository, ScenarioService scenarioService) {
        this.aircraftRepository = aircraftRepository;
        this.scenarioService = scenarioService;
    }

    @RequestMapping(path = "/{callsign}", method = RequestMethod.POST)
    public ResponseEntity<String> controlAircraft(
        @PathVariable String callsign,
        @RequestBody ControlAircraftDto controlAircraftDto
    ) {
        if (aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            scenarioService.instructAircraft(new Callsign(callsign), controlAircraftDto);
            return ResponseEntity.ok("Aircraft controlled:" + callsign);
        } else {
            return ResponseEntity.badRequest().body("Aircraft with callsign " + callsign + " does not exist.");
        }
    }

    @RequestMapping(path = "/{callsign}/direct/{fixName}", method = RequestMethod.POST)
    public ResponseEntity<String> directAircraftToFix(
        @PathVariable String callsign,
        @PathVariable String fixName
    ) {
        if (aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            scenarioService.directFixAircraft(new Callsign(callsign), fixName);
            System.out.println("Aircraft directed to fix:" + fixName);
            return ResponseEntity.ok("Aircraft directed to fix:" + fixName);
        } else {
            return ResponseEntity.badRequest().body("Aircraft with callsign " + callsign + " does not exist.");
        }
    }
}
