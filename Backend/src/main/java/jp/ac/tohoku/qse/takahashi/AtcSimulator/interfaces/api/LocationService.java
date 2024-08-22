package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationService {

    private final AircraftRadarService aircraftRadarService;

    @Autowired
    public LocationService(AircraftRadarService aircraftRadarService) {
        this.aircraftRadarService = aircraftRadarService;
    }

    // 全航空機の位置情報を取得するエンドポイント
    @GetMapping("/all")
    public List<AircraftPosition> getAllAircraftPositions() {
        return aircraftRadarService.getAllAircraftPositions();
    }

    // 特定の航空機の位置情報を取得するエンドポイント
    @GetMapping("/{callsign}")
    public AircraftPosition getAircraftPosition(@PathVariable String callsign) {
        return aircraftRadarService.getAircraftPositionByCallsign(callsign);
    }
}