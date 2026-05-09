package jp.ac.tohoku.qse.takahashi.AtcSimulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftFactory;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict.ConflictDetector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

/**
 * Measures wall-clock time of {@link ConflictAlertService#getAllConflictAlerts()} (full fleet, same path
 * as conflict APIs). Uses the same 200-aircraft layout as {@link NextStepTwoHundredAircraftPerformanceTest}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ConflictDetectionTwoHundredAircraftPerformanceTest {

    private static final Logger log =
            LoggerFactory.getLogger(ConflictDetectionTwoHundredAircraftPerformanceTest.class);

    private static final int AIRCRAFT_COUNT = 200;
    private static final int WARMUP_ITERATIONS = 3;
    private static final int TIMED_ITERATIONS = 25;

    /** {@code Backend/README.md}: 200 機コンフリクト「通常 &lt; 100ms」。 */
    private static final double MAX_AVG_MS = 100;

    @Autowired
    private ConflictAlertService conflictAlertService;

    @Autowired
    private AircraftRepository aircraftRepository;

    @BeforeEach
    void setUp() {
        GlobalVariables.isSimulationRunning = false;
        aircraftRepository.clear();
        for (int i = 0; i < AIRCRAFT_COUNT; i++) {
            String cs = "PF" + String.format("%03d", i);
            double lat = 35.0 + i * 0.02;
            double lon = 139.0 + i * 0.02;
            CreateAircraftDto dto = new CreateAircraftDto(
                    cs, lat, lon, 10_000 + (i % 20) * 500, 280, 0, 90,
                    "B738", "HND", "RJTT", "NRT", "RJAA", "2024-12-13T14:30:00Z");
            aircraftRepository.add(AircraftFactory.createCommercialAircraft(dto));
        }
        assertThat(aircraftRepository.findAll()).hasSize(AIRCRAFT_COUNT);
    }

    @AfterEach
    void tearDown() {
        GlobalVariables.isSimulationRunning = false;
        aircraftRepository.clear();
    }

    @Test
    @DisplayName("getAllConflictAlerts with 200 aircraft: report avg ms per run")
    void conflictDetection_twoHundredAircraft_reportsTiming() {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            conflictAlertService.getAllConflictAlerts();
        }

        long totalNanos = 0;
        for (int i = 0; i < TIMED_ITERATIONS; i++) {
            long t0 = System.nanoTime();
            Map<ConflictDetector.ConflictPair, RiskAssessment> conflicts =
                    conflictAlertService.getAllConflictAlerts();
            totalNanos += System.nanoTime() - t0;
            if (i == 0) {
                assertThat(conflicts).isNotNull();
            }
        }

        double avgMs = totalNanos / (double) TIMED_ITERATIONS / 1_000_000.0;
        log.info(
                "getAllConflictAlerts {} aircraft: {} warmup, {} timed iterations, avg={} ms/run",
                AIRCRAFT_COUNT,
                WARMUP_ITERATIONS,
                TIMED_ITERATIONS,
                String.format("%.3f", avgMs));

        assertThat(avgMs)
                .withFailMessage(
                        "avg conflict detection time %.3f ms exceeded ceiling %.0f ms (tune MAX_AVG_MS for your CI)",
                        avgMs,
                        MAX_AVG_MS)
                .isLessThan(MAX_AVG_MS);
    }
}
