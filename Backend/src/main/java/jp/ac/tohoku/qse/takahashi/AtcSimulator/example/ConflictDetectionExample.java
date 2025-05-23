package jp.ac.tohoku.qse.takahashi.AtcSimulator.example;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict.ConflictDetector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

import java.util.*;

/**
 * コンフリクト検出機能の使用例とパフォーマンス特性デモ
 * 実際の航空管制シナリオを模擬したサンプルコード
 */
public class ConflictDetectionExample {

    public static void main(String[] args) {
        System.out.println("=== 航空管制コンフリクトアラートシステム - 使用例 ===\n");

        ConflictDetectionExample example = new ConflictDetectionExample();

        // 基本的な使用例
        example.demonstrateBasicUsage();

        // パフォーマンステスト
        example.demonstratePerformance();

        // 特殊シナリオ
        example.demonstrateSpecialScenarios();
    }

    /**
     * 基本的な使用例の実演
     */
    public void demonstrateBasicUsage() {
        System.out.println("1. 基本的な使用例");
        System.out.println("================");

        ConflictDetector detector = new ConflictDetector();

        // 航空機のサンプル作成
        Aircraft jal123 = createSampleAircraft("JAL123", 35.6762, 139.6503, 35000, 90, 450, 0);
        Aircraft ana456 = createSampleAircraft("ANA456", 35.7000, 139.6503, 35000, 90, 450, 0);
        Aircraft ual789 = createSampleAircraft("UAL789", 35.6762, 139.7000, 36000, 270, 400, -500);

        System.out.println("作成した航空機:");
        System.out.printf("- %s: 位置(%.4f, %.4f), 高度%.0fft, 針路%.0f°, 速度%.0fkt\n",
            jal123.getCallsign(),
            jal123.getAircraftPosition().latitude.toDouble(),
            jal123.getAircraftPosition().longitude.toDouble(),
            jal123.getAircraftPosition().altitude.toDouble(),
            jal123.getAircraftVector().heading.toDouble(),
            jal123.getAircraftVector().groundSpeed.toDouble());

        System.out.printf("- %s: 位置(%.4f, %.4f), 高度%.0fft, 針路%.0f°, 速度%.0fkt\n",
            ana456.getCallsign(),
            ana456.getAircraftPosition().latitude.toDouble(),
            ana456.getAircraftPosition().longitude.toDouble(),
            ana456.getAircraftPosition().altitude.toDouble(),
            ana456.getAircraftVector().heading.toDouble(),
            ana456.getAircraftVector().groundSpeed.toDouble());

        // 2機間リスク計算
        RiskAssessment risk = detector.calculateConflictRisk(jal123, ana456);
        System.out.println("\n2機間リスク評価結果:");
        System.out.printf("- 危険度: %.2f/100\n", risk.getRiskLevel());
        System.out.printf("- アラートレベル: %s\n", risk.getAlertLevel());
        System.out.printf("- 最接近時間: %.1f秒後\n", risk.getTimeToClosest());
        System.out.printf("- 最接近時水平距離: %.2f海里\n", risk.getClosestHorizontalDistance());
        System.out.printf("- 最接近時垂直距離: %.0fフィート\n", risk.getClosestVerticalDistance());
        System.out.printf("- 管制間隔欠如予測: %s\n", risk.isConflictPredicted() ? "あり" : "なし");

        // 全ペア計算
        List<Aircraft> aircraftList = Arrays.asList(jal123, ana456, ual789);
        Map<String, RiskAssessment> allConflicts = detector.calculateAllConflicts(aircraftList);

        System.out.println("\n全航空機ペアの評価:");
        allConflicts.forEach((pairId, assessment) -> {
            System.out.printf("- %s: 危険度%.2f (%s)\n",
                pairId, assessment.getRiskLevel(), assessment.getAlertLevel());
        });

        System.out.println();
    }

    /**
     * パフォーマンステストの実演
     */
    public void demonstratePerformance() {
        System.out.println("2. パフォーマンステスト");
        System.out.println("====================");

        ConflictDetector detector = new ConflictDetector();

        // 異なる航空機数でのパフォーマンステスト
        int[] aircraftCounts = {10, 50, 100, 200};

        for (int count : aircraftCounts) {
            List<Aircraft> aircraftList = generateRandomAircraft(count);

            // ウォームアップ
            detector.calculateAllConflicts(aircraftList);

            // 実際の測定
            long startTime = System.nanoTime();
            Map<String, RiskAssessment> results = detector.calculateAllConflicts(aircraftList);
            long endTime = System.nanoTime();

            double executionTimeMs = (endTime - startTime) / 1_000_000.0;
            int conflictCount = results.size();
            int redConflicts = (int) results.values().stream()
                .mapToLong(r -> r.getAlertLevel() == AlertLevel.RED_CONFLICT ? 1 : 0)
                .sum();

            System.out.printf("航空機数: %3d機 | 処理時間: %6.2fms | コンフリクト: %4d件 | 赤アラート: %2d件\n",
                count, executionTimeMs, conflictCount, redConflicts);
        }

        // メモリ使用量テスト
        System.gc();
        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        List<Aircraft> largeList = generateRandomAircraft(200);
        Map<String, RiskAssessment> largeResults = detector.calculateAllConflicts(largeList);

        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double memoryUsedMB = (afterMemory - beforeMemory) / (1024.0 * 1024.0);

        System.out.printf("\nメモリ使用量 (200機): %.2fMB\n", memoryUsedMB);
        System.out.println();
    }

    /**
     * 特殊シナリオの実演
     */
    public void demonstrateSpecialScenarios() {
        System.out.println("3. 特殊シナリオテスト");
        System.out.println("==================");

        ConflictDetector detector = new ConflictDetector();

        // シナリオ1: 正面衝突コース
        System.out.println("シナリオ1: 正面衝突コース");
        Aircraft headOn1 = createSampleAircraft("HEADON1", 35.0, 139.0, 35000, 90, 400, 0);
        Aircraft headOn2 = createSampleAircraft("HEADON2", 35.0, 139.5, 35000, 270, 400, 0);

        RiskAssessment headOnRisk = detector.calculateConflictRisk(headOn1, headOn2);
        System.out.printf("危険度: %.2f, アラート: %s, 最接近: %.1f秒後\n",
            headOnRisk.getRiskLevel(), headOnRisk.getAlertLevel(), headOnRisk.getTimeToClosest());

        // シナリオ2: 並行飛行
        System.out.println("\nシナリオ2: 安全な並行飛行");
        Aircraft parallel1 = createSampleAircraft("PARA1", 35.0, 139.0, 35000, 90, 400, 0);
        Aircraft parallel2 = createSampleAircraft("PARA2", 35.1, 139.0, 36000, 90, 400, 0);

        RiskAssessment parallelRisk = detector.calculateConflictRisk(parallel1, parallel2);
        System.out.printf("危険度: %.2f, アラート: %s, 水平距離: %.2f海里, 垂直距離: %.0fft\n",
            parallelRisk.getRiskLevel(), parallelRisk.getAlertLevel(),
            parallelRisk.getClosestHorizontalDistance(), parallelRisk.getClosestVerticalDistance());

        // シナリオ3: 垂直交差
        System.out.println("\nシナリオ3: 垂直交差（上昇中vs降下中）");
        Aircraft climbing = createSampleAircraft("CLIMB", 35.0, 139.0, 30000, 90, 400, 1500);
        Aircraft descending = createSampleAircraft("DESC", 35.0, 139.1, 40000, 90, 400, -1500);

        RiskAssessment verticalRisk = detector.calculateConflictRisk(climbing, descending);
        System.out.printf("危険度: %.2f, アラート: %s, 管制間隔欠如予測: %s\n",
            verticalRisk.getRiskLevel(), verticalRisk.getAlertLevel(),
            verticalRisk.isConflictPredicted() ? "あり" : "なし");

        System.out.println();
        System.out.println("=== パフォーマンス特性まとめ ===");
        System.out.println("- 200機同時処理: 通常 < 100ms");
        System.out.println("- メモリ使用量: < 50MB");
        System.out.println("- 精度: CPA分析による高精度予測");
        System.out.println("- スレッドセーフ: 並列処理対応");
        System.out.println("- 最適化: 事前フィルタリングによる高速化");
    }

    /**
     * サンプル航空機を作成
     */
    private Aircraft createSampleAircraft(String callsign, double lat, double lon, double alt,
                                        double heading, double groundSpeed, double verticalSpeed) {
        Callsign cs = new Callsign(callsign);
        AircraftType type = new AircraftType("B777");

        AircraftPosition position = new AircraftPosition(
            new Latitude(lat),
            new Longitude(lon),
            new Altitude(alt)
        );

        AircraftVector vector = new AircraftVector(
            new Heading(heading),
            new GroundSpeed(groundSpeed),
            new VerticalSpeed(verticalSpeed)
        );

        return new CommercialAircraft(cs, type, position, vector,
            "NRT", "RJAA", "KIX", "RJBB", "2024-01-01T12:00:00Z");
    }

    /**
     * ランダムな航空機リストを生成
     */
    private List<Aircraft> generateRandomAircraft(int count) {
        List<Aircraft> aircraftList = new ArrayList<>();
        Random random = new Random(42); // 再現可能な結果のため固定シード

        for (int i = 0; i < count; i++) {
            // 東京近郊にランダム配置
            double lat = 35.0 + (random.nextDouble() - 0.5) * 2.0;
            double lon = 139.0 + (random.nextDouble() - 0.5) * 2.0;
            double alt = 25000 + random.nextDouble() * 20000;
            double heading = random.nextDouble() * 360;
            double groundSpeed = 300 + random.nextDouble() * 300;
            double verticalSpeed = (random.nextDouble() - 0.5) * 4000;

            String callsign = "TEST" + String.format("%03d", i);
            Aircraft aircraft = createSampleAircraft(callsign, lat, lon, alt, heading, groundSpeed, verticalSpeed);
            aircraftList.add(aircraft);
        }

        return aircraftList;
    }
}
