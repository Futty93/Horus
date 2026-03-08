package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/simulation")
@Validated
public class SimulationService {

    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);

    @PostMapping("/start")
    public ResponseEntity<Void> start() {
        GlobalVariables.isSimulationRunning = true;
        logger.info("Simulation started");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pause")
    public ResponseEntity<Void> pause() {
        GlobalVariables.isSimulationRunning = false;
        logger.info("Simulation paused");
        return ResponseEntity.ok().build();
    }

    // シミュレーションの状態を取得する
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSimulationRunning", GlobalVariables.isSimulationRunning);
        return ResponseEntity.ok(response);  // シミュレーションの状態を返す
    }
}