package jp.ac.tohoku.qse.takahashi.AtcSimulator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftFactory;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.SimulationTiming;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

/**
 * Measures wall-clock time of one {@link AirspaceManagement#nextStep(double)} with many aircraft.
 * Aircraft count matches {@code Backend/README.md} conflict capacity (200). Not a formal load test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class NextStepTwoHundredAircraftPerformanceTest {

    private static final Logger log =
            LoggerFactory.getLogger(NextStepTwoHundredAircraftPerformanceTest.class);

    private static final int AIRCRAFT_COUNT = 200;
    private static final int WARMUP_STEPS = 3;
    private static final int TIMED_ITERATIONS = 25;

    /**
     * Upper bound for average tick time (200 aircraft). README cites ~100ms for conflict-scale work;
     * {@code nextStep} alone is far lighter—local runs are often sub-1ms—so 100ms leaves large CI headroom.
     */
    private static final double MAX_AVG_STEP_MS = 100;

    @Autowired
    private AirspaceManagement airspaceManagement;

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
    @DisplayName("nextStep with 200 commercial aircraft: report avg ms per tick")
    void nextStep_twoHundredAircraft_reportsTiming() {
        for (int i = 0; i < WARMUP_STEPS; i++) {
            airspaceManagement.nextStep(SimulationTiming.SIM_DELTA_SECONDS);
        }

        long totalNanos = 0;
        for (int i = 0; i < TIMED_ITERATIONS; i++) {
            long t0 = System.nanoTime();
            airspaceManagement.nextStep(SimulationTiming.SIM_DELTA_SECONDS);
            totalNanos += System.nanoTime() - t0;
        }

        double avgMs = totalNanos / (double) TIMED_ITERATIONS / 1_000_000.0;
        log.info(
                "nextStep {} aircraft: {} warmup steps, {} timed iterations, avg={} ms/tick",
                AIRCRAFT_COUNT,
                WARMUP_STEPS,
                TIMED_ITERATIONS,
                String.format("%.3f", avgMs));

        assertThat(avgMs)
                .withFailMessage(
                        "avg nextStep time %.3f ms exceeded ceiling %.0f ms (tune MAX_AVG_STEP_MS for your CI)",
                        avgMs,
                        MAX_AVG_STEP_MS)
                .isLessThan(MAX_AVG_STEP_MS);
    }
}
