package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.ConflictDetectionException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.InvalidParameterException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ConflictAlertController.class);

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
        logger.debug("全コンフリクトアラート取得要求");

        Map<String, RiskAssessment> conflicts = conflictAlertService.getAllConflictAlerts();

        logger.debug("全コンフリクトアラート取得完了: {}件", conflicts.size());
        return ResponseEntity.ok(conflicts);
    }

    /**
     * 指定アラートレベル以上のコンフリクトを取得
     *
     * @param level 最小アラートレベル (SAFE, WHITE_CONFLICT, RED_CONFLICT)
     * @return フィルタされたコンフリクト評価結果
     * @throws InvalidParameterException 無効なアラートレベルが指定された場合
     */
    @GetMapping("/filtered")
    public ResponseEntity<Map<String, RiskAssessment>> getFilteredConflicts(
            @RequestParam(defaultValue = "WHITE_CONFLICT") String level) {
        logger.debug("フィルタされたコンフリクト取得要求: レベル={}", level);

        try {
            AlertLevel alertLevel = AlertLevel.valueOf(level.toUpperCase());
            Map<String, RiskAssessment> conflicts = conflictAlertService.getFilteredConflictAlerts(alertLevel);

            logger.debug("フィルタされたコンフリクト取得完了: {}件", conflicts.size());
            return ResponseEntity.ok(conflicts);
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException("level", level, "有効な値: SAFE, WHITE_CONFLICT, RED_CONFLICT");
        }
    }

    /**
     * 緊急度の高いコンフリクト（赤コンフリクト）を取得
     *
     * @return 赤コンフリクトのリスト（時間順）
     */
    @GetMapping("/critical")
    public ResponseEntity<List<ConflictAlertService.ConflictAlert>> getCriticalAlerts() {
        logger.debug("緊急コンフリクトアラート取得要求");

        List<ConflictAlertService.ConflictAlert> criticalAlerts = conflictAlertService.getCriticalAlerts();

        logger.debug("緊急コンフリクトアラート取得完了: {}件", criticalAlerts.size());
        return ResponseEntity.ok(criticalAlerts);
    }

    /**
     * 管制間隔欠如が予測されるコンフリクトを取得
     *
     * @return 管制間隔欠如予測のあるコンフリクトのリスト
     */
    @GetMapping("/violations")
    public ResponseEntity<List<ConflictAlertService.ConflictAlert>> getSeparationViolations() {
        logger.debug("管制間隔欠如予測取得要求");

        List<ConflictAlertService.ConflictAlert> violations = conflictAlertService.getSeparationViolationAlerts();

        logger.debug("管制間隔欠如予測取得完了: {}件", violations.size());
        return ResponseEntity.ok(violations);
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
        logger.debug("航空機固有コンフリクト取得要求: {}", callsign);

        List<ConflictAlertService.ConflictAlert> aircraftConflicts =
            conflictAlertService.getAircraftConflicts(callsign);

        logger.debug("航空機固有コンフリクト取得完了: {} - {}件", callsign, aircraftConflicts.size());
        return ResponseEntity.ok(aircraftConflicts);
    }

    /**
     * コンフリクトアラートの統計情報を取得
     *
     * @return アラート統計情報
     */
    @GetMapping("/statistics")
    public ResponseEntity<ConflictAlertService.ConflictStatistics> getConflictStatistics() {
        logger.debug("コンフリクト統計情報取得要求");

        ConflictAlertService.ConflictStatistics statistics = conflictAlertService.getConflictStatistics();

        logger.debug("コンフリクト統計情報取得完了");
        return ResponseEntity.ok(statistics);
    }

    /**
     * システムヘルスチェック
     *
     * @return システム状態
     */
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> getHealthStatus() {
        logger.debug("ヘルスチェック要求");

        ConflictAlertService.ConflictStatistics statistics = conflictAlertService.getConflictStatistics();
        HealthStatus status = new HealthStatus(
            "OK",
            System.currentTimeMillis(),
            statistics.getTotalConflicts(),
            statistics.getRedConflictCount()
        );

        logger.debug("ヘルスチェック完了: ステータス={}", status.getStatus());
        return ResponseEntity.ok(status);
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
