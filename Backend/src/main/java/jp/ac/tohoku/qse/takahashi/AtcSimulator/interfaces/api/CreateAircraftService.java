package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aircraft")
public class CreateAircraftService {

    private final AircraftRadarService aircraftRadarService;

    @Autowired
    public CreateAircraftService(AircraftRadarService aircraftRadarService) {
        this.aircraftRadarService = aircraftRadarService;
    }

    @PostMapping(path = "/create")
    public String createAircraft(@RequestBody CreateAircraftDto createAircraftDto) {
        scenarioService.spawnAircraft(createAircraftDto);
        return "Aircraft created:" + createAircraftDto.getCompanyName() + createAircraftDto.getFlightNumber();
    }
}