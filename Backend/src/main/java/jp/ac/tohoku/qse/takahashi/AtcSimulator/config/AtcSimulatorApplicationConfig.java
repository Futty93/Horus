package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PerformanceUtils;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * AtcSimulatorアプリケーションの設定クラス
 * 最適化機能の初期化を含む
 */
@Configuration
public class AtcSimulatorApplicationConfig {

    /**
     * アプリケーション起動時の初期化処理
     * パフォーマンス最適化機能を有効化
     */
    @PostConstruct
    public void initializeOptimizations() {
        // PerformanceUtilsの初期化
        PerformanceUtils.initialize();

        // 開発環境でのベンチマーク実行（本番では無効化可能）
        if (isDebugMode()) {
            PerformanceUtils.runOptimizationBenchmark();
        }

        System.out.println("AtcSimulator optimization initialized successfully.");
    }

    /**
     * デバッグモードの判定
     * システムプロパティまたは環境変数で制御
     */
    private boolean isDebugMode() {
        return "true".equalsIgnoreCase(System.getProperty("atcsimulator.debug")) ||
               "true".equalsIgnoreCase(System.getenv("ATCSIMULATOR_DEBUG"));
    }
}
