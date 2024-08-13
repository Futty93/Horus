package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aircraft")
public class CreateAircraftService {

    private final ScenarioService scenarioService;

    public CreateAircraftService(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public String createAircraft(CreateAircraftDto createAircraftDto) {
        scenarioService.spawnAircraft(createAircraftDto);
        return "Aircraft created:" + createAircraftDto.companyName + "-" +createAircraftDto.flightNumber;
    }
}
