package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import java.util.*;
import java.util.stream.Collectors;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict.ConflictDetector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ConflictAlertDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ConflictStatisticsDto;

/**
 * コンフリクトアラート機能のアプリケーションサービス
 * ドメインサービスとインフラストラクチャを連携させ、
 * 航空管制業務に必要なコンフリクト検出機能を提供
 */
public class ConflictAlertService {

    private final ConflictDetector conflictDetector;
    private final AircraftRepository aircraftRepository;

    /**
     * コンストラクタ
     *
     * @param conflictDetector コンフリクト検出ドメインサービス
     * @param aircraftRepository 航空機リポジトリ
     */
    public ConflictAlertService(ConflictDetector conflictDetector, AircraftRepository aircraftRepository) {
        this.conflictDetector = conflictDetector;
        this.aircraftRepository = aircraftRepository;
    }

    /**
     * 現在の全航空機のコンフリクト評価を実行
     *
     * @return コンフリクト評価結果マップ
     */
    public Map<String, RiskAssessment> getAllConflictAlerts() {
        List<Aircraft> allAircraft = aircraftRepository.findAll();
        return conflictDetector.calculateAllConflicts(allAircraft);
    }

    /**
     * 危険度レベルでフィルタされたコンフリクトアラートを取得
     *
     * @param minimumAlertLevel 最小アラートレベル
     * @return フィルタされたコンフリクト評価結果
     */
    public Map<String, RiskAssessment> getFilteredConflictAlerts(AlertLevel minimumAlertLevel) {
        Map<String, RiskAssessment> allConflicts = getAllConflictAlerts();

        return allConflicts.entrySet().stream()
            .filter(entry -> entry.getValue().getAlertLevel().isHigherThan(minimumAlertLevel) ||
                           entry.getValue().getAlertLevel() == minimumAlertLevel)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }

    /**
     * 緊急度の高いコンフリクトアラート（赤コンフリクト）のみを取得
     *
     * @return 赤コンフリクトのリスト
     */
    public List<ConflictAlertDto> getCriticalAlerts() {
        Map<String, RiskAssessment> allConflicts = getAllConflictAlerts();

        return allConflicts.entrySet().stream()
            .filter(entry -> entry.getValue().getAlertLevel() == AlertLevel.RED_CONFLICT)
            .map(entry -> toDto(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(ConflictAlertDto::timeToClosest))
            .collect(Collectors.toList());
    }

    /**
     * 管制間隔欠如が予測されるコンフリクトアラートを取得
     *
     * @return 管制間隔欠如予測のあるコンフリクトのリスト
     */
    public List<ConflictAlertDto> getSeparationViolationAlerts() {
        Map<String, RiskAssessment> allConflicts = getAllConflictAlerts();

        return allConflicts.entrySet().stream()
            .filter(entry -> entry.getValue().isConflictPredicted())
            .map(entry -> toDto(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(ConflictAlertDto::timeToClosest))
            .collect(Collectors.toList());
    }

    /**
     * 特定の航空機に関連するコンフリクトアラートを取得
     *
     * @param callsign 対象航空機のコールサイン
     * @return 指定航空機に関連するコンフリクトのリスト
     */
    public List<ConflictAlertDto> getAircraftConflicts(String callsign) {
        Map<String, RiskAssessment> allConflicts = getAllConflictAlerts();

        return allConflicts.entrySet().stream()
            .filter(entry -> entry.getKey().contains(callsign))
            .map(entry -> toDto(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(ConflictAlertDto::riskLevel).reversed())
            .collect(Collectors.toList());
    }

    /**
     * コンフリクトアラートの統計情報を取得
     *
     * @return アラート統計情報
     */
    public ConflictStatisticsDto getConflictStatistics() {
        Map<String, RiskAssessment> allConflicts = getAllConflictAlerts();

        long totalConflicts = allConflicts.size();
        long safeCount = allConflicts.values().stream()
            .mapToLong(assessment -> assessment.getAlertLevel() == AlertLevel.SAFE ? 1 : 0)
            .sum();
        long whiteConflictCount = allConflicts.values().stream()
            .mapToLong(assessment -> assessment.getAlertLevel() == AlertLevel.WHITE_CONFLICT ? 1 : 0)
            .sum();
        long redConflictCount = allConflicts.values().stream()
            .mapToLong(assessment -> assessment.getAlertLevel() == AlertLevel.RED_CONFLICT ? 1 : 0)
            .sum();
        long separationViolationCount = allConflicts.values().stream()
            .mapToLong(assessment -> assessment.isConflictPredicted() ? 1 : 0)
            .sum();

        double maxRiskLevel = allConflicts.values().stream()
            .mapToDouble(RiskAssessment::getRiskLevel)
            .max()
            .orElse(0.0);

        double avgRiskLevel = allConflicts.values().stream()
            .mapToDouble(RiskAssessment::getRiskLevel)
            .average()
            .orElse(0.0);

        return new ConflictStatisticsDto(
            totalConflicts, safeCount, whiteConflictCount, redConflictCount,
            separationViolationCount, maxRiskLevel, avgRiskLevel
        );
    }

    private static ConflictAlertDto toDto(String pairId, RiskAssessment assessment) {
        return new ConflictAlertDto(
            pairId,
            assessment.getRiskLevel(),
            assessment.getAlertLevel().name(),
            assessment.getTimeToClosest(),
            assessment.getClosestHorizontalDistance(),
            assessment.getClosestVerticalDistance(),
            assessment.isConflictPredicted()
        );
    }
}
