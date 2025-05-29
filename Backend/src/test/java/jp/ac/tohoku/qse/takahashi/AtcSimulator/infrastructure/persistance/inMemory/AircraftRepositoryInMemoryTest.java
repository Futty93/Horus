package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistance.inMemory;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftConflictException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftRepositoryInMemoryTest {
    AircraftRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new AircraftRepositoryInMemory();

        // Add test aircraft to repository
        Aircraft aircraft1 = createTestAircraft("JAL001", 35.0, 139.0, 35000, 90, 450, 0);
        Aircraft aircraft2 = createTestAircraft("ANA002", 36.0, 140.0, 36000, 180, 500, 0);
        Aircraft aircraft3 = createTestAircraft("UAL003", 37.0, 141.0, 37000, 270, 400, 0);

        repository.add(aircraft1);
        repository.add(aircraft2);
        repository.add(aircraft3);
    }

    @Test
    void shouldFindAllAircrafts() {
        List<Aircraft> aircrafts = repository.findAll();
        assertEquals(3, aircrafts.size(), "There should be 3 aircrafts");
    }

    @Test
    void shouldFindAircraftByCallsign() {
        Callsign callsign = new Callsign("JAL001");
        Aircraft aircraft = repository.findByCallsign(callsign);
        assertNotNull(aircraft);
        assertEquals("JAL001", aircraft.getCallsign().toString());
    }

    @Test
    void shouldCheckAircraftExistence() {
        Callsign existingCallsign = new Callsign("JAL001");
        Callsign nonExistingCallsign = new Callsign("INVALID");

        assertTrue(repository.isAircraftExist(existingCallsign));
        assertFalse(repository.isAircraftExist(nonExistingCallsign));
    }

    @Test
    void shouldThrowExceptionForNonExistentAircraft() {
        Callsign nonExistingCallsign = new Callsign("INVALID");

        assertThrows(AircraftNotFoundException.class, () -> {
            repository.findByCallsign(nonExistingCallsign);
        });
    }

    @Test
    void shouldPreventDuplicateCallsigns() {
        Aircraft duplicateAircraft = createTestAircraft("JAL001", 40.0, 140.0, 40000, 0, 500, 0);

        assertThrows(AircraftConflictException.class, () -> {
            repository.add(duplicateAircraft);
        });
    }

    @Test
    void shouldRemoveAircraft() {
        Callsign callsign = new Callsign("JAL001");
        Aircraft aircraft = repository.findByCallsign(callsign);

        repository.remove(aircraft);

        assertFalse(repository.isAircraftExist(callsign));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentAircraft() {
        Aircraft nonExistentAircraft = createTestAircraft("INVALID", 40.0, 140.0, 40000, 0, 500, 0);

        assertThrows(AircraftNotFoundException.class, () -> {
            repository.remove(nonExistentAircraft);
        });
    }

    @Test
    void shouldClearRepository() {
        repository.clear();
        assertEquals(0, repository.findAll().size());
        assertFalse(repository.isAircraftExist(new Callsign("JAL001")));
    }

    @Test
    void shouldProvideRepositoryInfo() {
        String info = repository.getRepositoryInfo();
        assertTrue(info.contains("3 aircraft(s)"));
    }

    @Test
    void shouldHandleConcurrentAccess() throws InterruptedException {
        repository.clear();

        int numberOfThreads = 10;
        int aircraftPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 複数スレッドで同時に航空機を追加
        for (int t = 0; t < numberOfThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < aircraftPerThread; i++) {
                        String callsign = String.format("TEST%03d%03d", threadId, i);
                        Aircraft aircraft = createTestAircraft(callsign, 35.0, 139.0, 35000, 90, 450, 0);
                        repository.add(aircraft);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Test should complete within 30 seconds");
        executor.shutdown();

        // 全ての航空機が正しく追加されていることを確認
        assertEquals(numberOfThreads * aircraftPerThread, successCount.get());
        assertEquals(0, failureCount.get());
        assertEquals(numberOfThreads * aircraftPerThread, repository.findAll().size());
    }

    @Test
    void shouldPerformFastSearchOperations() {
        // 大量のデータでのパフォーマンステスト
        repository.clear();

        // 1000機の航空機を追加
        int aircraftCount = 1000;
        for (int i = 0; i < aircraftCount; i++) {
            String callsign = String.format("PERF%04d", i);
            Aircraft aircraft = createTestAircraft(callsign, 35.0, 139.0, 35000, 90, 450, 0);
            repository.add(aircraft);
        }

        // 検索パフォーマンステスト
        long startTime = System.nanoTime();
        for (int i = 0; i < aircraftCount; i++) {
            String callsign = String.format("PERF%04d", i);
            assertTrue(repository.isAircraftExist(new Callsign(callsign)));
        }
        long endTime = System.nanoTime();

        // 1000回の検索が100ms以内に完了することを確認（O(1)検索の恩恵）
        long duration = endTime - startTime;
        long durationMs = duration / 1_000_000;
        assertTrue(durationMs < 100, "1000 searches should complete within 100ms, actual: " + durationMs + "ms");
    }

    @Test
    void shouldExecuteNextStepSafely() throws InterruptedException {
        // NextStepの並行実行テスト
        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    repository.NextStep();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "NextStep should complete within 10 seconds");
        executor.shutdown();

        // データの整合性を確認
        assertEquals(3, repository.findAll().size());
    }

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
}
