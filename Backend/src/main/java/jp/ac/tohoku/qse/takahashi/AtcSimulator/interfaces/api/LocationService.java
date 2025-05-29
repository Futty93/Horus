package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 航空機位置情報取得API
 *
 * 航空機の現在位置とリスク評価情報を提供する
 */
@RestController
@RequestMapping("/aircraft")
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final AircraftRadarService aircraftRadarService;
    private final AircraftRepository aircraftRepository;
    private final ConflictAlertService conflictAlertService;

    public LocationService(AircraftRadarService aircraftRadarService,
                          AircraftRepository aircraftRepository,
                          ConflictAlertService conflictAlertService) {
        this.aircraftRadarService = aircraftRadarService;
        this.aircraftRepository = aircraftRepository;
        this.conflictAlertService = conflictAlertService;
    }

    /**
     * 全航空機の位置情報と危険度を取得
     *
     * @return 全航空機の詳細情報（危険度付き）
     */
    @RequestMapping(path = "/location/all", method = RequestMethod.GET)
    public ResponseEntity<String> getAllAircraftLocation() {
        logger.debug("全航空機位置情報取得要求");

        // 全航空機の基本情報を取得
        String basicLocationInfo = aircraftRadarService.getAllAircraftLocation();

        // 各航空機の危険度情報を取得
        Map<String, RiskAssessment> allConflicts = conflictAlertService.getAllConflictAlerts();
        List<Aircraft> allAircraft = aircraftRepository.findAll();

        StringBuilder enhancedInfo = new StringBuilder();
        String[] aircraftLines = basicLocationInfo.split("\n");

        for (String aircraftLine : aircraftLines) {
            if (aircraftLine.trim().isEmpty()) {
                continue;
            }

            // コールサインを抽出
            String callsign = StringUtils.extractByRegex(aircraftLine, "callsign=([^,}]+)", 1);
            if (!callsign.isEmpty()) {
                double maxRiskLevel = calculateMaxRiskForAircraft(callsign, allConflicts, allAircraft);

                // 最後の}の直前にriskLevel情報を追加
                int lastBraceIndex = aircraftLine.lastIndexOf("}");
                if (lastBraceIndex > 0) {
                    String enhancedLine = aircraftLine.substring(0, lastBraceIndex) +
                                         ", riskLevel=" + StringUtils.formatRiskLevel(maxRiskLevel) + "}";
                    enhancedInfo.append(enhancedLine).append("\n");
                } else {
                    enhancedInfo.append(aircraftLine).append("\n");
                }
            } else {
                enhancedInfo.append(aircraftLine).append("\n");
            }
        }

        return ResponseEntity.ok(enhancedInfo.toString());
    }

    /**
     * 特定航空機の位置情報と危険度を取得
     *
     * @param callsign 航空機のコールサイン
     * @return 指定航空機の詳細情報（危険度付き）
     * @throws AircraftNotFoundException 航空機が見つからない場合
     */
    @RequestMapping(path = "/location", method = RequestMethod.GET)
    public ResponseEntity<String> getAircraftLocation(@RequestParam String callsign) {
        logger.debug("航空機位置情報取得要求: {}", callsign);

        Callsign aircraftCallsign = new Callsign(callsign);

        // 航空機の存在チェック（例外が発生する場合はGlobalExceptionHandlerで処理）
        if (!aircraftRepository.isAircraftExist(aircraftCallsign)) {
            throw new AircraftNotFoundException(callsign);
        }

        String basicInfo = aircraftRadarService.getAircraftLocation(aircraftCallsign);

        // 特定航空機の危険度を計算
        Map<String, RiskAssessment> allConflicts = conflictAlertService.getAllConflictAlerts();
        List<Aircraft> allAircraft = aircraftRepository.findAll();
        double maxRiskLevel = calculateMaxRiskForAircraft(callsign, allConflicts, allAircraft);

        // 最後の}の直前にriskLevel情報を追加
        int lastBraceIndex = basicInfo.lastIndexOf("}");
        String enhancedInfo;
        if (lastBraceIndex > 0) {
            enhancedInfo = basicInfo.substring(0, lastBraceIndex) +
                          ", riskLevel=" + StringUtils.formatRiskLevel(maxRiskLevel) + "}";
        } else {
            enhancedInfo = basicInfo;
        }

        return ResponseEntity.ok(enhancedInfo);
    }

    /**
     * 特定航空機の最高危険度を計算
     *
     * @param targetCallsign 対象航空機のコールサイン
     * @param allConflicts 全コンフリクト情報
     * @param allAircraft 全航空機リスト
     * @return 最高危険度
     */
    private double calculateMaxRiskForAircraft(String targetCallsign,
                                              Map<String, RiskAssessment> allConflicts,
                                              List<Aircraft> allAircraft) {
        double maxRisk = 0.0;

        for (Aircraft other : allAircraft) {
            if (!other.getCallsign().toString().equals(targetCallsign)) {
                String pairId = StringUtils.generatePairId(targetCallsign, other.getCallsign().toString());
                RiskAssessment risk = allConflicts.get(pairId);
                if (risk != null && risk.getRiskLevel() > maxRisk) {
                    maxRisk = risk.getRiskLevel();
                }
            }
        }
        return maxRisk;
    }
}
