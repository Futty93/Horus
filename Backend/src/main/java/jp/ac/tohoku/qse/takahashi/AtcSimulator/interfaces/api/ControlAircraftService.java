package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ControlAircraftDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 航空機制御API
 *
 * 航空機への管制指示を処理する
 */
@RestController
@RequestMapping("/api/aircraft/control")
public class ControlAircraftService {

    private static final Logger logger = LoggerFactory.getLogger(ControlAircraftService.class);

    private final AircraftRepository aircraftRepository;
    private final ScenarioService scenarioService;

    public ControlAircraftService(AircraftRepository aircraftRepository, ScenarioService scenarioService) {
        this.aircraftRepository = aircraftRepository;
        this.scenarioService = scenarioService;
    }

    /**
     * 航空機に管制指示を送信
     *
     * @param callsign 航空機のコールサイン
     * @param controlAircraftDto 管制指示内容
     * @return 制御結果メッセージ
     * @throws AircraftNotFoundException 航空機が見つからない場合
     */
    @RequestMapping(path = "/{callsign}", method = RequestMethod.POST)
    public ResponseEntity<String> controlAircraft(
        @PathVariable String callsign,
        @RequestBody ControlAircraftDto controlAircraftDto
    ) {
        logger.debug("航空機制御要求: {}", callsign);

        Callsign aircraftCallsign = new Callsign(callsign);

        // 航空機の存在チェック（例外が発生する場合はGlobalExceptionHandlerで処理）
        if (!aircraftRepository.isAircraftExist(aircraftCallsign)) {
            throw new AircraftNotFoundException(callsign);
        }

        scenarioService.instructAircraft(aircraftCallsign, controlAircraftDto);

        logger.info("航空機制御完了: {}", callsign);
        return ResponseEntity.ok("Aircraft controlled: " + callsign);
    }

    /**
     * 航空機を特定のフィックスに直行させる
     *
     * @param callsign 航空機のコールサイン
     * @param fixName 目標フィックス名
     * @return 制御結果メッセージ
     * @throws AircraftNotFoundException 航空機が見つからない場合
     */
    @RequestMapping(path = "/{callsign}/direct/{fixName}", method = RequestMethod.POST)
    public ResponseEntity<String> directAircraftToFix(
        @PathVariable String callsign,
        @PathVariable String fixName
    ) {
        logger.debug("航空機フィックス直行要求: {} -> {}", callsign, fixName);

        Callsign aircraftCallsign = new Callsign(callsign);

        // 航空機の存在チェック（例外が発生する場合はGlobalExceptionHandlerで処理）
        if (!aircraftRepository.isAircraftExist(aircraftCallsign)) {
            throw new AircraftNotFoundException(callsign);
        }

        scenarioService.directFixAircraft(aircraftCallsign, fixName);

        logger.info("航空機フィックス直行完了: {} -> {}", callsign, fixName);
        return ResponseEntity.ok("Aircraft directed to fix: " + fixName);
    }
}
