package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict.ConflictDetector;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * コンフリクト検出機能のSpring Bean設定
 */
@Configuration
public class ConflictDetectionConfig {

    /**
     * ConflictDetectorドメインサービスのBean定義
     *
     * @return ConflictDetectorインスタンス
     */
    @Bean
    public ConflictDetector conflictDetector() {
        return new ConflictDetector();
    }

    /**
     * ConflictAlertServiceアプリケーションサービスのBean定義
     *
     * @param conflictDetector コンフリクト検出ドメインサービス
     * @param aircraftRepository 航空機リポジトリ
     * @return ConflictAlertServiceインスタンス
     */
    @Bean
    public ConflictAlertService conflictAlertService(ConflictDetector conflictDetector,
                                                   AircraftRepository aircraftRepository) {
        return new ConflictAlertService(conflictDetector, aircraftRepository);
    }
}
