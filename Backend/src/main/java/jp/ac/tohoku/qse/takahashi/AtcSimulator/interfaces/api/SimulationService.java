package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.SimulationTickScheduler;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.SimulationTiming;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.SimulationSpeedErrorResponse;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.SimulationSpeedRequest;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.SimulationSpeedResponse;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.SimulationStatusDto;

@RestController
@RequestMapping("/simulation")
@Validated
public class SimulationService {

    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);

    private final SimulationTiming simulationTiming;
    private final SimulationTickScheduler simulationTickScheduler;

    public SimulationService(SimulationTiming simulationTiming, SimulationTickScheduler simulationTickScheduler) {
        this.simulationTiming = simulationTiming;
        this.simulationTickScheduler = simulationTickScheduler;
    }

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

    @GetMapping("/status")
    public ResponseEntity<SimulationStatusDto> getStatus() {
        return ResponseEntity.ok(new SimulationStatusDto(
                GlobalVariables.isSimulationRunning,
                simulationTiming.getSpeedMultiplier(),
                simulationTiming.getTickIntervalWallMs()));
    }

    @GetMapping("/speed")
    public ResponseEntity<SimulationSpeedResponse> getSpeed() {
        return ResponseEntity.ok(new SimulationSpeedResponse(
                simulationTiming.getSpeedMultiplier(),
                simulationTiming.getTickIntervalWallMs()));
    }

    @PutMapping("/speed")
    public ResponseEntity<?> putSpeed(@RequestBody SimulationSpeedRequest request) {
        if (!SimulationTiming.isValidPreset(request.speedMultiplier())) {
            return ResponseEntity.badRequest()
                    .body(new SimulationSpeedErrorResponse(
                            "speedMultiplier must be one of: 0.25, 0.5, 1, 2, 4, 10"));
        }
        simulationTiming.setSpeedMultiplier(request.speedMultiplier());
        simulationTickScheduler.reschedule();
        logger.info("Simulation speed set to {}x (tick every {} ms wall)",
                request.speedMultiplier(), simulationTiming.getTickIntervalWallMs());
        return ResponseEntity.ok().build();
    }
}
