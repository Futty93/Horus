package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ConflictAlertDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ConflictStatisticsDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.HealthStatusDto;

/**
 * コンフリクトアラート機能のREST APIコントローラー
 * 航空管制におけるコンフリクト検出とアラート管理のエンドポイントを提供
 */
@RestController
@RequestMapping("/api/conflict")
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
    public ResponseEntity<List<ConflictAlertDto>> getAllConflicts() {
        logger.debug("全コンフリクトアラート取得要求");

        List<ConflictAlertDto> conflicts = conflictAlertService.getAllConflictAlertsAsDto();

        logger.debug("全コンフリクトアラート取得完了: {}件", conflicts.size());
        return ResponseEntity.ok(conflicts);
    }

    /**
     * 指定アラートレベル以上のコンフリクトを取得
     *
     * @param level 最小アラートレベル (SAFE, WHITE_CONFLICT, RED_CONFLICT)
     * @return フィルタされたコンフリクト評価結果
     * @throws InvalidParameterException 無効なアラートレベルが指定された場合（Service から伝播）
     */
    @GetMapping("/filtered")
    public ResponseEntity<List<ConflictAlertDto>> getFilteredConflicts(
            @RequestParam(defaultValue = "WHITE_CONFLICT") String level) {
        logger.debug("フィルタされたコンフリクト取得要求: レベル={}", level);

        List<ConflictAlertDto> conflicts = conflictAlertService.getFilteredConflictAlertsAsDto(level);

        logger.debug("フィルタされたコンフリクト取得完了: {}件", conflicts.size());
        return ResponseEntity.ok(conflicts);
    }

    /**
     * 緊急度の高いコンフリクト（赤コンフリクト）を取得
     *
     * @return 赤コンフリクトのリスト（時間順）
     */
    @GetMapping("/critical")
    public ResponseEntity<List<ConflictAlertDto>> getCriticalAlerts() {
        logger.debug("緊急コンフリクトアラート取得要求");

        List<ConflictAlertDto> criticalAlerts = conflictAlertService.getCriticalAlerts();

        logger.debug("緊急コンフリクトアラート取得完了: {}件", criticalAlerts.size());
        return ResponseEntity.ok(criticalAlerts);
    }

    /**
     * 管制間隔欠如が予測されるコンフリクトを取得
     *
     * @return 管制間隔欠如予測のあるコンフリクトのリスト
     */
    @GetMapping("/violations")
    public ResponseEntity<List<ConflictAlertDto>> getSeparationViolations() {
        logger.debug("管制間隔欠如予測取得要求");

        List<ConflictAlertDto> violations = conflictAlertService.getSeparationViolationAlerts();

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
    public ResponseEntity<List<ConflictAlertDto>> getAircraftConflicts(
            @PathVariable String callsign) {
        logger.debug("航空機固有コンフリクト取得要求: {}", callsign);

        List<ConflictAlertDto> aircraftConflicts = conflictAlertService.getAircraftConflicts(callsign);

        logger.debug("航空機固有コンフリクト取得完了: {} - {}件", callsign, aircraftConflicts.size());
        return ResponseEntity.ok(aircraftConflicts);
    }

    /**
     * コンフリクトアラートの統計情報を取得
     *
     * @return アラート統計情報
     */
    @GetMapping("/statistics")
    public ResponseEntity<ConflictStatisticsDto> getConflictStatistics() {
        logger.debug("コンフリクト統計情報取得要求");

        ConflictStatisticsDto statistics = conflictAlertService.getConflictStatistics();

        logger.debug("コンフリクト統計情報取得完了");
        return ResponseEntity.ok(statistics);
    }

    /**
     * システムヘルスチェック
     *
     * @return システム状態
     */
    @GetMapping("/health")
    public ResponseEntity<HealthStatusDto> getHealthStatus() {
        logger.debug("ヘルスチェック要求");

        ConflictStatisticsDto statistics = conflictAlertService.getConflictStatistics();
        HealthStatusDto status = new HealthStatusDto(
            "OK",
            System.currentTimeMillis(),
            statistics.totalConflicts(),
            statistics.redConflictCount()
        );

        logger.debug("ヘルスチェック完了: ステータス={}", status.status());
        return ResponseEntity.ok(status);
    }
}
