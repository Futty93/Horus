package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PerformanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * AtcSimulatorアプリケーションの設定クラス
 * 最適化機能の初期化を含む
 */
@Configuration
public class AtcSimulatorApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(AtcSimulatorApplicationConfig.class);

    @PostConstruct
    public void initializeOptimizations() {
        PerformanceUtils.initialize();

        if (isDebugMode()) {
            PerformanceUtils.runOptimizationBenchmark();
        }

        logger.info("AtcSimulator optimization initialized successfully.");
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
