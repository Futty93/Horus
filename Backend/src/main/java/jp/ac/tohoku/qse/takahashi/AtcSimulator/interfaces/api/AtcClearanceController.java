package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ControlAircraftDto;

import jakarta.validation.Valid;

/**
 * 管制クリアランスメモ（InstructionMemo と同型の数値）を機体に紐づけて保持する API。
 * パイロット操縦目標の {@code POST /api/aircraft/control/{callsign}} とは別概念。
 */
@RestController
@RequestMapping("/api/aircraft")
public class AtcClearanceController {

    private static final Logger logger = LoggerFactory.getLogger(AtcClearanceController.class);

    private final AircraftRepository aircraftRepository;
    private final ScenarioService scenarioService;

    public AtcClearanceController(AircraftRepository aircraftRepository, ScenarioService scenarioService) {
        this.aircraftRepository = aircraftRepository;
        this.scenarioService = scenarioService;
    }

    @PostMapping("/{callsign}/atc-clearance")
    public ResponseEntity<String> setAtcClearance(
            @PathVariable String callsign,
            @Valid @RequestBody ControlAircraftDto body) {
        logger.debug("管制クリアランス記録要求: {}", callsign);

        Callsign cs = new Callsign(callsign);
        if (!aircraftRepository.isAircraftExist(cs)) {
            throw new AircraftNotFoundException(callsign);
        }

        InstructedVector clearance = new InstructedVector(
                new Heading(body.instructedHeading()),
                new Altitude(body.instructedAltitude()),
                new GroundSpeed(body.instructedGroundSpeed()));
        scenarioService.setAtcClearance(cs, clearance);

        logger.info("管制クリアランス記録完了: {}", callsign);
        return ResponseEntity.ok("ATC clearance recorded: " + callsign);
    }
}
