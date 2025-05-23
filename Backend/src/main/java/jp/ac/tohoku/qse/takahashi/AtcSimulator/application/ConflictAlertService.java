package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict.ConflictDetector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;

import java.util.*;
import java.util.stream.Collectors;

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
    public List<ConflictAlert> getCriticalAlerts() {
        Map<String, RiskAssessment> allConflicts = getAllConflictAlerts();

        return allConflicts.entrySet().stream()
            .filter(entry -> entry.getValue().getAlertLevel() == AlertLevel.RED_CONFLICT)
            .map(entry -> new ConflictAlert(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(alert -> alert.getRiskAssessment().getTimeToClosest()))
            .collect(Collectors.toList());
    }

    /**
     * 管制間隔欠如が予測されるコンフリクトアラートを取得
     *
     * @return 管制間隔欠如予測のあるコンフリクトのリスト
     */
    public List<ConflictAlert> getSeparationViolationAlerts() {
        Map<String, RiskAssessment> allConflicts = getAllConflictAlerts();

        return allConflicts.entrySet().stream()
            .filter(entry -> entry.getValue().isConflictPredicted())
            .map(entry -> new ConflictAlert(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(alert -> alert.getRiskAssessment().getTimeToClosest()))
            .collect(Collectors.toList());
    }

    /**
     * 特定の航空機に関連するコンフリクトアラートを取得
     *
     * @param callsign 対象航空機のコールサイン
     * @return 指定航空機に関連するコンフリクトのリスト
     */
    public List<ConflictAlert> getAircraftConflicts(String callsign) {
        Map<String, RiskAssessment> allConflicts = getAllConflictAlerts();

        return allConflicts.entrySet().stream()
            .filter(entry -> entry.getKey().contains(callsign))
            .map(entry -> new ConflictAlert(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(alert -> -alert.getRiskAssessment().getRiskLevel())) // 危険度降順
            .collect(Collectors.toList());
    }

    /**
     * コンフリクトアラートの統計情報を取得
     *
     * @return アラート統計情報
     */
    public ConflictStatistics getConflictStatistics() {
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

        return new ConflictStatistics(
            totalConflicts, safeCount, whiteConflictCount, redConflictCount,
            separationViolationCount, maxRiskLevel, avgRiskLevel
        );
    }

    /**
     * コンフリクトアラート情報を表すデータクラス
     */
    public static class ConflictAlert {
        private final String pairId;
        private final RiskAssessment riskAssessment;

        public ConflictAlert(String pairId, RiskAssessment riskAssessment) {
            this.pairId = pairId;
            this.riskAssessment = riskAssessment;
        }

        public String getPairId() {
            return pairId;
        }

        public RiskAssessment getRiskAssessment() {
            return riskAssessment;
        }

        public String[] getCallsigns() {
            return pairId.split("-");
        }

        @Override
        public String toString() {
            return String.format("ConflictAlert{pairId='%s', riskLevel=%.2f, alertLevel=%s, timeToClosest=%.1fs}",
                pairId, riskAssessment.getRiskLevel(), riskAssessment.getAlertLevel(),
                riskAssessment.getTimeToClosest());
        }
    }

    /**
     * コンフリクト統計情報を表すデータクラス
     */
    public static class ConflictStatistics {
        private final long totalConflicts;
        private final long safeCount;
        private final long whiteConflictCount;
        private final long redConflictCount;
        private final long separationViolationCount;
        private final double maxRiskLevel;
        private final double avgRiskLevel;

        public ConflictStatistics(long totalConflicts, long safeCount, long whiteConflictCount,
                                long redConflictCount, long separationViolationCount,
                                double maxRiskLevel, double avgRiskLevel) {
            this.totalConflicts = totalConflicts;
            this.safeCount = safeCount;
            this.whiteConflictCount = whiteConflictCount;
            this.redConflictCount = redConflictCount;
            this.separationViolationCount = separationViolationCount;
            this.maxRiskLevel = maxRiskLevel;
            this.avgRiskLevel = avgRiskLevel;
        }

        // Getters
        public long getTotalConflicts() { return totalConflicts; }
        public long getSafeCount() { return safeCount; }
        public long getWhiteConflictCount() { return whiteConflictCount; }
        public long getRedConflictCount() { return redConflictCount; }
        public long getSeparationViolationCount() { return separationViolationCount; }
        public double getMaxRiskLevel() { return maxRiskLevel; }
        public double getAvgRiskLevel() { return avgRiskLevel; }

        @Override
        public String toString() {
            return String.format(
                "ConflictStatistics{total=%d, safe=%d, white=%d, red=%d, violations=%d, maxRisk=%.2f, avgRisk=%.2f}",
                totalConflicts, safeCount, whiteConflictCount, redConflictCount,
                separationViolationCount, maxRiskLevel, avgRiskLevel
            );
        }
    }
}
