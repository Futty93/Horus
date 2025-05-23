package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConflictDetectorの包括的なテストクラス
 * 基本機能、境界値、パフォーマンス、エラーケースをテスト
 */
class ConflictDetectorTest {

    private ConflictDetector conflictDetector;
    private Aircraft testAircraft1;
    private Aircraft testAircraft2;

    @BeforeEach
    void setUp() {
        conflictDetector = new ConflictDetector();

        // テスト用航空機1（東京上空）
        testAircraft1 = createTestAircraft(
            "JAL001",
            35.6762, 139.6503, 35000,  // 成田空港上空
            90, 450, 0                  // 東向き、450ノット、水平飛行
        );

        // テスト用航空機2（東京近郊）
        testAircraft2 = createTestAircraft(
            "ANA002",
            35.7000, 139.6503, 35000,  // 若干北側
            90, 450, 0                  // 同方向
        );
    }

    @Nested
    @DisplayName("基本機能テスト")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("正常な2機間リスク計算")
        void testCalculateConflictRisk_Normal() {
            // 実行
            RiskAssessment result = conflictDetector.calculateConflictRisk(testAircraft1, testAircraft2);

            // 検証
            assertNotNull(result);
            assertTrue(result.getRiskLevel() >= 0.0 && result.getRiskLevel() <= 100.0);
            assertTrue(result.getTimeToClosest() >= 0.0);
            assertTrue(result.getClosestHorizontalDistance() >= 0.0);
            assertTrue(result.getClosestVerticalDistance() >= 0.0);
            assertNotNull(result.getAlertLevel());
        }

        @Test
        @DisplayName("全航空機ペアの危険度計算")
        void testCalculateAllConflicts_Normal() {
            // 準備
            List<Aircraft> aircraftList = Arrays.asList(testAircraft1, testAircraft2);

            // 実行
            Map<String, RiskAssessment> results = conflictDetector.calculateAllConflicts(aircraftList);

            // 検証
            assertNotNull(results);
            assertTrue(results.size() <= 1); // 最大1ペア
        }

        @Test
        @DisplayName("アラートレベル判定")
        void testDetermineAlertLevel() {
            // 安全レベル
            assertEquals(AlertLevel.SAFE, conflictDetector.determineAlertLevel(10.0));

            // 白コンフリクト
            assertEquals(AlertLevel.WHITE_CONFLICT, conflictDetector.determineAlertLevel(50.0));

            // 赤コンフリクト
            assertEquals(AlertLevel.RED_CONFLICT, conflictDetector.determineAlertLevel(80.0));
        }
    }

    @Nested
    @DisplayName("境界値テスト")
    class BoundaryValueTests {

        @Test
        @DisplayName("安全な管制間隔テスト")
        void testSafeSeparation() {
            // 十分な間隔での航空機
            Aircraft aircraft1 = createTestAircraft("SAFE1", 35.0, 139.0, 35000, 90, 400, 0);
            Aircraft aircraft2 = createTestAircraft("SAFE2", 35.1, 139.1, 36100, 90, 400, 0); // 6NM + 1100ft

            RiskAssessment result = conflictDetector.calculateConflictRisk(aircraft1, aircraft2);
            assertFalse(result.isSeparationViolation(), "十分な間隔があるため違反なし");
        }

        @Test
        @DisplayName("水平間隔違反テスト")
        void testHorizontalSeparationViolation() {
            // 水平間隔不足のシナリオ（正面衝突）
            Aircraft aircraft1 = createTestAircraft("H1", 35.0, 139.0, 35000, 90, 400, 0);
            Aircraft aircraft2 = createTestAircraft("H2", 35.0, 139.05, 36100, 270, 400, 0); // 約3NM、正面衝突

            RiskAssessment result = conflictDetector.calculateConflictRisk(aircraft1, aircraft2);
            assertTrue(result.isSeparationViolation(), "水平間隔不足のため違反");
        }

        @Test
        @DisplayName("垂直間隔違反テスト")
        void testVerticalSeparationViolation() {
            // 垂直間隔不足のシナリオ（同じ経路、高度差小）
            Aircraft aircraft1 = createTestAircraft("V1", 35.0, 139.0, 35000, 90, 400, 0);
            Aircraft aircraft2 = createTestAircraft("V2", 35.1, 139.0, 35500, 90, 400, 0); // 500ft差

            RiskAssessment result = conflictDetector.calculateConflictRisk(aircraft1, aircraft2);
            assertTrue(result.isSeparationViolation(), "垂直間隔不足のため違反");
        }

        @Test
        @DisplayName("危険度境界値テスト")
        void testRiskLevelBoundaries() {
            // 危険度0の境界（十分な距離）
            Aircraft safeAircraft1 = createTestAircraft("SAFE1", 35.0, 139.0, 35000, 0, 400, 0);
            Aircraft safeAircraft2 = createTestAircraft("SAFE2", 36.0, 140.0, 40000, 180, 400, 0);

            RiskAssessment safeResult = conflictDetector.calculateConflictRisk(safeAircraft1, safeAircraft2);
            assertEquals(0.0, safeResult.getRiskLevel(), 0.1);

            // 危険度100の境界（衝突コース）
            Aircraft dangerAircraft1 = createTestAircraft("DANGER1", 35.0, 139.0, 35000, 90, 400, 0);
            Aircraft dangerAircraft2 = createTestAircraft("DANGER2", 35.0, 139.1, 35000, 270, 400, 0);

            RiskAssessment dangerResult = conflictDetector.calculateConflictRisk(dangerAircraft1, dangerAircraft2);
            assertTrue(dangerResult.getRiskLevel() > 50.0);
        }
    }

    @Nested
    @DisplayName("パフォーマンステスト")
    class PerformanceTests {

        @Test
        @DisplayName("200機での処理時間測定")
        void testPerformanceWith200Aircraft() {
            // 準備：200機の航空機リストを生成
            List<Aircraft> largeAircraftList = generate200Aircraft();

            // 実行時間測定
            long startTime = System.currentTimeMillis();
            Map<String, RiskAssessment> results = conflictDetector.calculateAllConflicts(largeAircraftList);
            long endTime = System.currentTimeMillis();

            long executionTime = endTime - startTime;

            // 検証
            assertNotNull(results);
            assertTrue(executionTime < 5000, "処理時間が5秒を超えました: " + executionTime + "ms");

            // ペア数の確認（200機 → 最大19900ペア、フィルタリング後は少なくなる）
            assertTrue(results.size() <= 19900);

            System.out.println("200機処理時間: " + executionTime + "ms, 結果ペア数: " + results.size());
        }

        @Test
        @DisplayName("メモリ使用量テスト")
        void testMemoryUsage() {
            // GC実行
            System.gc();
            long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // 大量データ処理
            List<Aircraft> aircraftList = generate200Aircraft();
            Map<String, RiskAssessment> results = conflictDetector.calculateAllConflicts(aircraftList);

            // メモリ使用量確認
            long finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long memoryUsed = finalMemory - initialMemory;

            // 検証（500MB以下）
            assertTrue(memoryUsed < 500 * 1024 * 1024,
                "メモリ使用量が過大です: " + (memoryUsed / 1024 / 1024) + "MB");

            System.out.println("メモリ使用量: " + (memoryUsed / 1024 / 1024) + "MB");
        }
    }

    @Nested
    @DisplayName("エラーケーステスト")
    class ErrorHandlingTests {

        @Test
        @DisplayName("null航空機リストエラー")
        void testCalculateAllConflicts_NullList() {
            assertThrows(IllegalArgumentException.class,
                () -> conflictDetector.calculateAllConflicts(null));
        }

        @Test
        @DisplayName("null航空機エラー")
        void testCalculateConflictRisk_NullAircraft() {
            assertThrows(IllegalArgumentException.class,
                () -> conflictDetector.calculateConflictRisk(null, testAircraft2));

            assertThrows(IllegalArgumentException.class,
                () -> conflictDetector.calculateConflictRisk(testAircraft1, null));
        }

        @Test
        @DisplayName("空リスト処理")
        void testCalculateAllConflicts_EmptyList() {
            Map<String, RiskAssessment> results = conflictDetector.calculateAllConflicts(new ArrayList<>());

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("単一航空機処理")
        void testCalculateAllConflicts_SingleAircraft() {
            List<Aircraft> singleAircraft = Arrays.asList(testAircraft1);
            Map<String, RiskAssessment> results = conflictDetector.calculateAllConflicts(singleAircraft);

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("異常な座標データ処理")
        void testAbnormalCoordinates() {
            // 極端な座標の航空機
            Aircraft extremeAircraft = createTestAircraft(
                "EXTREME",
                90.0, 180.0, 60000,  // 北極、日付変更線
                0, 600, 0
            );

            // エラーなく処理されることを確認
            assertDoesNotThrow(() -> {
                RiskAssessment result = conflictDetector.calculateConflictRisk(testAircraft1, extremeAircraft);
                assertNotNull(result);
            });
        }
    }

    @Nested
    @DisplayName("特殊シナリオテスト")
    class SpecialScenarioTests {

        @Test
        @DisplayName("正面衝突コーステスト")
        void testHeadOnCollisionCourse() {
            Aircraft aircraft1 = createTestAircraft("HEAD1", 35.0, 139.0, 35000, 90, 400, 0);
            Aircraft aircraft2 = createTestAircraft("HEAD2", 35.0, 139.5, 35000, 270, 400, 0);

            RiskAssessment result = conflictDetector.calculateConflictRisk(aircraft1, aircraft2);

            assertTrue(result.getRiskLevel() > 70.0);
            assertEquals(AlertLevel.RED_CONFLICT, result.getAlertLevel());
            assertTrue(result.isConflictPredicted());
        }

        @Test
        @DisplayName("並行飛行テスト")
        void testParallelFlight() {
            Aircraft aircraft1 = createTestAircraft("PAR1", 35.0, 139.0, 35000, 90, 400, 0);
            Aircraft aircraft2 = createTestAircraft("PAR2", 35.1, 139.0, 36000, 90, 400, 0);

            RiskAssessment result = conflictDetector.calculateConflictRisk(aircraft1, aircraft2);

            assertTrue(result.getRiskLevel() < 30.0);
            assertEquals(AlertLevel.SAFE, result.getAlertLevel());
            assertFalse(result.isConflictPredicted());
        }

        @Test
        @DisplayName("垂直分離テスト")
        void testVerticalSeparation() {
            // 水平方向に十分離れた位置で垂直分離をテスト
            Aircraft aircraft1 = createTestAircraft("VERT1", 35.0, 139.0, 35000, 90, 400, 0);
            Aircraft aircraft2 = createTestAircraft("VERT2", 35.1, 139.0, 36500, 90, 400, 0); // 6NM離れた位置

            RiskAssessment result = conflictDetector.calculateConflictRisk(aircraft1, aircraft2);

            assertTrue(result.getClosestVerticalDistance() >= 1000.0);
            assertFalse(result.isConflictPredicted());
        }
    }

    // ヘルパーメソッド

    /**
     * テスト用航空機を作成
     */
    private Aircraft createTestAircraft(String callsign, double lat, double lon, double alt,
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
     * 200機の航空機リストを生成（パフォーマンステスト用）
     */
    private List<Aircraft> generate200Aircraft() {
        List<Aircraft> aircraftList = new ArrayList<>();
        Random random = new Random(12345); // 再現可能な結果のため固定シード

        IntStream.range(0, 200).forEach(i -> {
            // 東京近郊にランダム配置
            double lat = 35.0 + (random.nextDouble() - 0.5) * 2.0; // ±1度の範囲
            double lon = 139.0 + (random.nextDouble() - 0.5) * 2.0;
            double alt = 25000 + random.nextDouble() * 20000; // 25000-45000ft
            double heading = random.nextDouble() * 360;
            double groundSpeed = 300 + random.nextDouble() * 300; // 300-600kt
            double verticalSpeed = (random.nextDouble() - 0.5) * 4000; // ±2000ft/min

            Aircraft aircraft = createTestAircraft(
                "TEST" + String.format("%03d", i),
                lat, lon, alt, heading, groundSpeed, verticalSpeed
            );

            aircraftList.add(aircraft);
        });

        return aircraftList;
    }
}
