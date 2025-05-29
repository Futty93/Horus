package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/aircraft")
public class LocationService {
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

    @RequestMapping(path = "/location/all", method = RequestMethod.GET)
    public String getAllAircraftLocation() {
        // 全航空機の基本情報を取得
        String basicLocationInfo = aircraftRadarService.getAllAircraftLocation();

        // 各航空機の危険度情報を取得
        Map<String, RiskAssessment> allConflicts = conflictAlertService.getAllConflictAlerts();
        List<Aircraft> allAircraft = aircraftRepository.findAll();

        StringBuilder enhancedInfo = new StringBuilder();
        String[] aircraftLines = basicLocationInfo.split("\n");

        for (String line : aircraftLines) {
            if (line.startsWith("Aircraft{") && !line.trim().isEmpty()) {
                // 航空機のコールサインを抽出
                String callsign = extractCallsignFromLine(line);

                // その航空機の最高危険度を計算
                double maxRiskLevel = calculateMaxRiskForAircraft(callsign, allConflicts, allAircraft);

                // 最後の}の直前にriskLevel情報を追加
                int lastBraceIndex = line.lastIndexOf("}");
                if (lastBraceIndex > 0) {
                    String enhancedLine = line.substring(0, lastBraceIndex) +
                                         ", riskLevel=" + String.format("%.2f", maxRiskLevel) + "}";
                    enhancedInfo.append(enhancedLine).append("\n");
                } else {
                    enhancedInfo.append(line).append("\n");
                }
            } else if (!line.trim().isEmpty()) {
                enhancedInfo.append(line).append("\n");
            }
        }

        return enhancedInfo.toString();
    }

    @RequestMapping(path = "/location", method = RequestMethod.GET)
    public ResponseEntity<String> getAircraftLocation(String callsign) {
        if (aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            String basicInfo = aircraftRadarService.getAircraftLocation(new Callsign(callsign));

            // 特定航空機の危険度を計算
            Map<String, RiskAssessment> allConflicts = conflictAlertService.getAllConflictAlerts();
            List<Aircraft> allAircraft = aircraftRepository.findAll();
            double maxRiskLevel = calculateMaxRiskForAircraft(callsign, allConflicts, allAircraft);

            // 最後の}の直前にriskLevel情報を追加
            int lastBraceIndex = basicInfo.lastIndexOf("}");
            String enhancedInfo;
            if (lastBraceIndex > 0) {
                enhancedInfo = basicInfo.substring(0, lastBraceIndex) +
                              ", riskLevel=" + String.format("%.2f", maxRiskLevel) + "}";
            } else {
                enhancedInfo = basicInfo;
            }

            return ResponseEntity.ok(enhancedInfo);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Aircraft with callsign " + callsign + " does not exist.");
        }
    }

    /**
     * 航空機情報行からコールサインを抽出
     */
    private String extractCallsignFromLine(String line) {
        int start = line.indexOf("callsign=") + 9;
        int end = line.indexOf(",", start);
        return line.substring(start, end);
    }

    /**
     * 特定航空機の最高危険度を計算
     */
    private double calculateMaxRiskForAircraft(String callsign, Map<String, RiskAssessment> allConflicts, List<Aircraft> allAircraft) {
        double maxRisk = 0.0;

        // この航空機が関与する全てのコンフリクトを検査
        for (Map.Entry<String, RiskAssessment> entry : allConflicts.entrySet()) {
            String conflictPair = entry.getKey();
            RiskAssessment risk = entry.getValue();

            // コンフリクトペア名にこの航空機のコールサインが含まれているかチェック
            if (conflictPair.contains(callsign)) {
                maxRisk = Math.max(maxRisk, risk.getRiskLevel());
            }
        }

        return maxRisk;
    }
}
