package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aircraft")
@Validated
public class CreateAircraftService {

    private final AircraftRadarService aircraftRadarService;

    @Autowired
    public CreateAircraftService(AircraftRadarService aircraftRadarService) {
        this.aircraftRadarService = aircraftRadarService;
    }

    @PostMapping("/create")
    public ResponseEntity<Aircraft> createAircraft(@RequestBody CreateAircraftDto createAircraftDto) {
        // サービス層を使って新しい航空機を作成
        Aircraft createdAircraft = aircraftRadarService.createAircraft(createAircraftDto);
        
        // 作成した航空機情報を返す
        return new ResponseEntity<>(createdAircraft, HttpStatus.CREATED);
    }
}