package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftControlService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ControlAircraftDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control")
public class ControlService {

    private final AircraftControlService aircraftControlService;

    @Autowired
    public ControlService(AircraftControlService aircraftControlService) {
        this.aircraftControlService = aircraftControlService;
    }

    // 特定の航空機に対する管制指示を送信するエンドポイント
    @PostMapping("/{callsign}")
    public ResponseEntity<String> controlAircraft(
            @PathVariable String callsign,
            @RequestBody ControlAircraftDto controlAircraftDto) {
        
        boolean result = aircraftControlService.controlAircraft(callsign, controlAircraftDto);
        
        if (result) {
            return ResponseEntity.ok("Control command applied successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to apply control command.");
        }
    }
}