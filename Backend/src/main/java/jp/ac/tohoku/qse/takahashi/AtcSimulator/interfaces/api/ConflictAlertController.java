package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * コンフリクトアラート機能のREST APIコントローラー
 * 航空管制におけるコンフリクト検出とアラート管理のエンドポイントを提供
 */
@RestController
@RequestMapping("/api/conflict")
@CrossOrigin(origins = "*")
public class ConflictAlertController {

    private final ConflictAlertService conflictAlertService;

    /**
     * コンストラクタ
     *
     * @param conflictAlertService コンフリクトアラートサービス
     */
    public ConflictAlertController(ConflictAlertService conflictAlertService) {
        this.conflictAlertService = conflictAlertService;
    }

    /**
     * 全てのコンフリクトアラートを取得
     *
     * @return 全コンフリクト評価結果
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, RiskAssessment>> getAllConflicts() {
        try {
            Map<String, RiskAssessment> conflicts = conflictAlertService.getAllConflictAlerts();
            return ResponseEntity.ok(conflicts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 指定アラートレベル以上のコンフリクトを取得
     *
     * @param level 最小アラートレベル (SAFE, WHITE_CONFLICT, RED_CONFLICT)
     * @return フィルタされたコンフリクト評価結果
     */
    @GetMapping("/filtered")
    public ResponseEntity<Map<String, RiskAssessment>> getFilteredConflicts(
            @RequestParam(defaultValue = "WHITE_CONFLICT") String level) {
        try {
            AlertLevel alertLevel = AlertLevel.valueOf(level.toUpperCase());
            Map<String, RiskAssessment> conflicts = conflictAlertService.getFilteredConflictAlerts(alertLevel);
            return ResponseEntity.ok(conflicts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 緊急度の高いコンフリクト（赤コンフリクト）を取得
     *
     * @return 赤コンフリクトのリスト（時間順）
     */
    @GetMapping("/critical")
    public ResponseEntity<List<ConflictAlertService.ConflictAlert>> getCriticalAlerts() {
        try {
            List<ConflictAlertService.ConflictAlert> criticalAlerts = conflictAlertService.getCriticalAlerts();
            return ResponseEntity.ok(criticalAlerts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 管制間隔欠如が予測されるコンフリクトを取得
     *
     * @return 管制間隔欠如予測のあるコンフリクトのリスト
     */
    @GetMapping("/violations")
    public ResponseEntity<List<ConflictAlertService.ConflictAlert>> getSeparationViolations() {
        try {
            List<ConflictAlertService.ConflictAlert> violations = conflictAlertService.getSeparationViolationAlerts();
            return ResponseEntity.ok(violations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 特定航空機に関連するコンフリクトを取得
     *
     * @param callsign 航空機コールサイン
     * @return 指定航空機に関連するコンフリクトのリスト
     */
    @GetMapping("/aircraft/{callsign}")
    public ResponseEntity<List<ConflictAlertService.ConflictAlert>> getAircraftConflicts(
            @PathVariable String callsign) {
        try {
            List<ConflictAlertService.ConflictAlert> aircraftConflicts =
                conflictAlertService.getAircraftConflicts(callsign);
            return ResponseEntity.ok(aircraftConflicts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * コンフリクトアラートの統計情報を取得
     *
     * @return アラート統計情報
     */
    @GetMapping("/statistics")
    public ResponseEntity<ConflictAlertService.ConflictStatistics> getConflictStatistics() {
        try {
            ConflictAlertService.ConflictStatistics statistics = conflictAlertService.getConflictStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * システムヘルスチェック
     *
     * @return システム状態
     */
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> getHealthStatus() {
        try {
            ConflictAlertService.ConflictStatistics statistics = conflictAlertService.getConflictStatistics();
            HealthStatus status = new HealthStatus(
                "OK",
                System.currentTimeMillis(),
                statistics.getTotalConflicts(),
                statistics.getRedConflictCount()
            );
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            HealthStatus status = new HealthStatus(
                "ERROR",
                System.currentTimeMillis(),
                0,
                0
            );
            return ResponseEntity.internalServerError().body(status);
        }
    }

    /**
     * ヘルスステータス情報を表すクラス
     */
    public static class HealthStatus {
        private final String status;
        private final long timestamp;
        private final long totalConflicts;
        private final long criticalConflicts;

        public HealthStatus(String status, long timestamp, long totalConflicts, long criticalConflicts) {
            this.status = status;
            this.timestamp = timestamp;
            this.totalConflicts = totalConflicts;
            this.criticalConflicts = criticalConflicts;
        }

        public String getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
        public long getTotalConflicts() { return totalConflicts; }
        public long getCriticalConflicts() { return criticalConflicts; }
    }
}
