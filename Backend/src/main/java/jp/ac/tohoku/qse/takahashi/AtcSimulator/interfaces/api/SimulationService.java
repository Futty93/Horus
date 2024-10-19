package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/simulation")
@Validated
public class SimulationService {

    // シミュレーションを開始する
    @PostMapping("/start")
    public ResponseEntity<Void> start() {
        GlobalVariables.isSimulationRunning = true;
        return ResponseEntity.ok().build();  // 成功した場合は200 OKを返す
    }

    // シミュレーションを一時停止する
    @PostMapping("/pause")
    public ResponseEntity<Void> pause() {
        GlobalVariables.isSimulationRunning = false;
        return ResponseEntity.ok().build();  // 成功した場合は200 OKを返す
    }

    // シミュレーションの状態を取得する
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSimulationRunning", GlobalVariables.isSimulationRunning);
        return ResponseEntity.ok(response);  // シミュレーションの状態を返す
    }
}