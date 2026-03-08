package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.GetAllAircraftLocationsWithRiskUseCase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.AircraftLocationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 航空機位置情報取得API
 *
 * 航空機の現在位置とリスク評価情報をJSON形式で提供する
 */
@RestController
@RequestMapping("/aircraft")
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final GetAllAircraftLocationsWithRiskUseCase getAllAircraftLocationsWithRiskUseCase;
    private final AircraftRepository aircraftRepository;

    public LocationService(GetAllAircraftLocationsWithRiskUseCase getAllAircraftLocationsWithRiskUseCase,
                           AircraftRepository aircraftRepository) {
        this.getAllAircraftLocationsWithRiskUseCase = getAllAircraftLocationsWithRiskUseCase;
        this.aircraftRepository = aircraftRepository;
    }

    @GetMapping(path = "/location/all", produces = "application/json")
    public ResponseEntity<List<AircraftLocationDto>> getAllAircraftLocation() {
        logger.debug("全航空機位置情報取得要求");
        List<AircraftLocationDto> locations = getAllAircraftLocationsWithRiskUseCase.execute();
        return ResponseEntity.ok(locations);
    }

    @GetMapping(path = "/location", produces = "application/json")
    public ResponseEntity<AircraftLocationDto> getAircraftLocation(@RequestParam String callsign) {
        logger.debug("航空機位置情報取得要求: {}", callsign);

        Callsign aircraftCallsign = new Callsign(callsign);
        if (!aircraftRepository.isAircraftExist(aircraftCallsign)) {
            throw new AircraftNotFoundException(callsign);
        }

        AircraftLocationDto dto = getAllAircraftLocationsWithRiskUseCase.execute(callsign);
        return ResponseEntity.ok(dto);
    }
}
