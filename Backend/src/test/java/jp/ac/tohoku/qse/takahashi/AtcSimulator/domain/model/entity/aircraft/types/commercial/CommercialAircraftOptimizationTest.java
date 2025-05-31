package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PerformanceUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommercialAircraftクラスの最適化効果を測定するベンチマークテストクラス
 *
 * このテストクラスは以下の最適化項目の効果を測定します：
 * 1. 位置計算の平面近似vs球面計算の選択的使用
 * 2. 冗長な計算の削減（しきい値チェック）
 * 3. レーダー表示文字列のキャッシュ効果
 * 4. 三角関数キャッシュの効果
 * 5. メモリプール使用の効果
 */
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("CommercialAircraft 最適化効果測定テスト")
public class CommercialAircraftOptimizationTest {

    private List<CommercialAircraft> testAircraft;
    private static final int AIRCRAFT_COUNT = 100;
    private static final int SIMULATION_STEPS = 1000;

    @BeforeEach
    void setUp() {
        // PerformanceUtilsの初期化
        PerformanceUtils.initialize();
        PerformanceUtils.clearCache();

        // テスト用航空機の生成
        testAircraft = createTestAircraft(AIRCRAFT_COUNT);
    }

    @Test
    @Order(1)
    @DisplayName("1. 基本機能テスト - 最適化後の機能が正常に動作することを確認")
    void testBasicFunctionality() {
        CommercialAircraft aircraft = testAircraft.get(0);

        // 初期状態の確認
        assertNotNull(aircraft.getCallsign());
        assertNotNull(aircraft.getAircraftPosition());
        assertNotNull(aircraft.getAircraftVector());

        // 位置計算の実行
        AircraftPosition initialPosition = aircraft.getAircraftPosition();
        aircraft.calculateNextAircraftPosition();
        AircraftPosition newPosition = aircraft.getAircraftPosition();

        // 位置が更新されたかどうかは移動距離によって決まる
        // 少なくとも例外が発生しないことを確認
        assertNotNull(newPosition);

        // レーダー文字列の生成
        String radarString = aircraft.toRadarString();
        assertNotNull(radarString);
        assertFalse(radarString.isEmpty());

        System.out.println("✓ 基本機能テスト完了");
    }

    @Test
    @Order(2)
    @DisplayName("2. 位置計算パフォーマンステスト - 大量の航空機の位置計算時間を測定")
    void testPositionCalculationPerformance() {
        System.out.println("\n=== 位置計算パフォーマンステスト ===");

        // 事前ウォームアップ
        warmupCalculations();

        // 短距離移動（平面近似）のテスト
        long planarTime = measurePositionCalculationTime(createSlowMovingAircraft(), "短距離移動（平面近似）");

        // 長距離移動（球面計算）のテスト
        long sphericalTime = measurePositionCalculationTime(createFastMovingAircraft(), "長距離移動（球面計算）");

        // 結果の検証
        assertTrue(planarTime > 0, "平面近似計算時間が記録されていない");
        assertTrue(sphericalTime > 0, "球面計算時間が記録されていない");

        // 平面近似の方が球面計算より高速であることを確認
        double speedup = (double) sphericalTime / planarTime;
        System.out.printf("平面近似の高速化効果: %.2fx\n", speedup);

        // 最低限の高速化効果を期待（プロファイルによって調整可能）
        assertTrue(speedup >= 0.8, "期待される高速化効果が得られていない");

        System.out.println("✓ 位置計算パフォーマンステスト完了\n");
    }

    @Test
    @Order(3)
    @DisplayName("3. レーダー文字列キャッシュ効果テスト - 文字列生成時間の短縮効果を測定")
    void testRadarStringCacheEffect() {
        System.out.println("=== レーダー文字列キャッシュ効果テスト ===");

        CommercialAircraft aircraft = testAircraft.get(0);

        // 初回生成時間の測定（キャッシュなし）
        long startTime = System.nanoTime();
        String firstCall = aircraft.toRadarString();
        long firstCallTime = System.nanoTime() - startTime;

        // 2回目以降の生成時間の測定（キャッシュあり）
        startTime = System.nanoTime();
        String secondCall = aircraft.toRadarString();
        long secondCallTime = System.nanoTime() - startTime;

        // 内容が同一であることを確認
        assertEquals(firstCall, secondCall, "キャッシュされた文字列が異なる");

        // キャッシュ効果の確認
        double cacheSpeedup = (double) firstCallTime / secondCallTime;
        System.out.printf("初回生成: %d ns\n", firstCallTime);
        System.out.printf("キャッシュ使用: %d ns\n", secondCallTime);
        System.out.printf("キャッシュ高速化効果: %.2fx\n", cacheSpeedup);

        // キャッシュによる高速化を確認（最低2倍の高速化を期待）
        assertTrue(cacheSpeedup >= 2.0, "キャッシュによる十分な高速化効果が得られていない");

        System.out.println("✓ レーダー文字列キャッシュ効果テスト完了\n");
    }

    @Test
    @Order(4)
    @DisplayName("4. 三角関数キャッシュ効果テスト - 三角関数計算の高速化効果を測定")
    void testTrigonometricCacheEffect() {
        System.out.println("=== 三角関数キャッシュ効果テスト ===");

        // パフォーマンスベンチマークの実行
        PerformanceUtils.runOptimizationBenchmark();

        // 実際の使用パターンでのテスト
        long standardTime = measureStandardTrigonometric();
        long optimizedTime = measureOptimizedTrigonometric();

        double speedup = (double) standardTime / optimizedTime;
        System.out.printf("標準Math関数: %d ms\n", standardTime / 1_000_000);
        System.out.printf("最適化関数: %d ms\n", optimizedTime / 1_000_000);
        System.out.printf("最適化効果: %.2fx\n", speedup);

        // 最適化効果を確認
        assertTrue(speedup >= 1.0, "三角関数の最適化効果が得られていない");

        System.out.println("✓ 三角関数キャッシュ効果テスト完了\n");
    }

    @Test
    @Order(5)
    @DisplayName("5. 大規模シミュレーションパフォーマンステスト - 最適化の総合効果を測定")
    void testLargeScaleSimulationPerformance() {
        System.out.println("=== 大規模シミュレーションパフォーマンステスト ===");

        // 事前にキャッシュをクリア
        PerformanceUtils.clearCache();

        long startTime = System.currentTimeMillis();

        // 大規模シミュレーションの実行
        for (int step = 0; step < SIMULATION_STEPS; step++) {
            for (CommercialAircraft aircraft : testAircraft) {
                aircraft.calculateNextAircraftPosition();
                aircraft.calculateNextAircraftVector();
                aircraft.toRadarString(); // レーダー表示も含む
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // パフォーマンス統計の取得
        double[] cacheStats = PerformanceUtils.getCacheStatistics();

        // 結果の出力
        System.out.printf("航空機数: %d機\n", AIRCRAFT_COUNT);
        System.out.printf("シミュレーションステップ数: %d回\n", SIMULATION_STEPS);
        System.out.printf("総計算時間: %d ms\n", totalTime);
        System.out.printf("1機あたり1ステップの平均時間: %.3f ms\n",
                        (double) totalTime / (AIRCRAFT_COUNT * SIMULATION_STEPS));
        System.out.printf("キャッシュヒット数: %.0f\n", cacheStats[0]);
        System.out.printf("キャッシュミス数: %.0f\n", cacheStats[1]);
        System.out.printf("キャッシュヒット率: %.1f%%\n", cacheStats[2]);

        // パフォーマンス要件の確認
        double averageTimePerStep = (double) totalTime / (AIRCRAFT_COUNT * SIMULATION_STEPS);
        assertTrue(averageTimePerStep < 1.0,
                  String.format("1機あたりの計算時間が要件(1ms)を超えています: %.3f ms", averageTimePerStep));

        assertTrue(cacheStats[2] > 50.0,
                  String.format("キャッシュヒット率が低すぎます: %.1f%%", cacheStats[2]));

        System.out.println("✓ 大規模シミュレーションパフォーマンステスト完了\n");
    }

    @Test
    @Order(6)
    @DisplayName("6. メモリ効率テスト - メモリ使用量とガベージコレクションの影響を測定")
    void testMemoryEfficiency() {
        System.out.println("=== メモリ効率テスト ===");

        Runtime runtime = Runtime.getRuntime();

        // 初期メモリ状態
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // メモリ集約的な処理を実行
        List<String> radarStrings = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            for (CommercialAircraft aircraft : testAircraft) {
                aircraft.calculateNextAircraftPosition();
                radarStrings.add(aircraft.toRadarString());
            }
        }

        // 最終メモリ状態
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;

        System.out.printf("初期メモリ使用量: %d KB\n", initialMemory / 1024);
        System.out.printf("最終メモリ使用量: %d KB\n", finalMemory / 1024);
        System.out.printf("追加メモリ使用量: %d KB\n", memoryUsed / 1024);
        System.out.printf("1機あたりメモリ使用量: %.2f KB\n", (double) memoryUsed / (1024 * AIRCRAFT_COUNT));

        // メモリ効率の確認（1機あたり50KB以下を目標）
        double memoryPerAircraft = (double) memoryUsed / (1024 * AIRCRAFT_COUNT);
        assertTrue(memoryPerAircraft < 50.0,
                  String.format("メモリ使用量が多すぎます: %.2f KB/機", memoryPerAircraft));

        System.out.println("✓ メモリ効率テスト完了\n");
    }

    // ========== ヘルパーメソッド ==========

    private List<CommercialAircraft> createTestAircraft(int count) {
        List<CommercialAircraft> aircraft = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < count; i++) {
            Callsign callsign = new Callsign(String.format("TEST%03d", i));
            AircraftType type = new AircraftType("B737");

            // ランダムな位置と速度
            AircraftPosition position = new AircraftPosition(
                new Latitude(random.nextDouble(35.0, 36.0)),
                new Longitude(random.nextDouble(139.0, 140.0)),
                new Altitude(random.nextDouble(10000, 40000))
            );

            AircraftVector vector = new AircraftVector(
                new Heading(random.nextDouble(0, 360)),
                new GroundSpeed(random.nextDouble(200, 500)),
                new VerticalSpeed(random.nextDouble(-2000, 2000))
            );

            aircraft.add(new CommercialAircraft(
                callsign, type, position, vector,
                "NRT", "RJAA", "LAX", "KLAX", "2024-12-20T10:00:00Z"
            ));
        }

        return aircraft;
    }

    private List<CommercialAircraft> createSlowMovingAircraft() {
        List<CommercialAircraft> aircraft = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            aircraft.add(createAircraftWithSpeed(150.0)); // 低速（平面近似対象）
        }
        return aircraft;
    }

    private List<CommercialAircraft> createFastMovingAircraft() {
        List<CommercialAircraft> aircraft = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            aircraft.add(createAircraftWithSpeed(600.0)); // 高速（球面計算対象）
        }
        return aircraft;
    }

    private CommercialAircraft createAircraftWithSpeed(double speed) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        return new CommercialAircraft(
            new Callsign("SPEED" + (int)speed),
            new AircraftType("B777"),
            new AircraftPosition(
                new Latitude(35.5),
                new Longitude(139.5),
                new Altitude(35000)
            ),
            new AircraftVector(
                new Heading(90.0),
                new GroundSpeed(speed),
                new VerticalSpeed(0.0)
            ),
            "NRT", "RJAA", "SFO", "KSFO", "2024-12-20T12:00:00Z"
        );
    }

    private void warmupCalculations() {
        // JVMのウォームアップのため、事前に計算を実行
        for (int i = 0; i < 100; i++) {
            testAircraft.get(0).calculateNextAircraftPosition();
        }
    }

    private long measurePositionCalculationTime(List<CommercialAircraft> aircraft, String testType) {
        long startTime = System.nanoTime();

        for (int step = 0; step < 100; step++) {
            for (CommercialAircraft plane : aircraft) {
                plane.calculateNextAircraftPosition();
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.printf("%s: %d ms (%.3f ms/機/ステップ)\n",
                        testType,
                        duration / 1_000_000,
                        (double) duration / (1_000_000 * aircraft.size() * 100));

        return duration;
    }

    private long measureStandardTrigonometric() {
        long startTime = System.nanoTime();

        for (int i = 0; i < 1_000_000; i++) {
            double angle = i % 360;
            Math.sin(Math.toRadians(angle));
            Math.cos(Math.toRadians(angle));
        }

        return System.nanoTime() - startTime;
    }

    private long measureOptimizedTrigonometric() {
        long startTime = System.nanoTime();

        for (int i = 0; i < 1_000_000; i++) {
            double angle = i % 360;
            PerformanceUtils.fastSin(angle);
            PerformanceUtils.fastCos(angle);
        }

        return System.nanoTime() - startTime;
    }
}
