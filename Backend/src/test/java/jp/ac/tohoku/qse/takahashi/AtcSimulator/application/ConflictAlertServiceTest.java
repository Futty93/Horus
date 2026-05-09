package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.InvalidParameterException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict.ConflictDetector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistence.inMemory.AircraftRepositoryInMemory;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ConflictAlertDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ConflictStatisticsDto;

/**
 * Unit tests for ConflictAlertService.
 * Verifies DTO return types and conversion correctness.
 */
@ExtendWith(MockitoExtension.class)
class ConflictAlertServiceTest {

    private ConflictAlertService conflictAlertService;
    private AircraftRepository aircraftRepository;

    @Mock
    private ConflictDetector conflictDetector;

    @BeforeEach
    void setUp() {
        GlobalVariables.isSimulationRunning = false;
        aircraftRepository = new AircraftRepositoryInMemory();
        conflictAlertService = new ConflictAlertService(conflictDetector, aircraftRepository);
    }

    @Test
    @DisplayName("DTO-returning methods return empty when no aircraft")
    void dtoMethods_returnEmptyWhenNoAircraft() {
        when(conflictDetector.calculateAllConflicts(anyList())).thenReturn(Map.of());

        assertThat(conflictAlertService.getCriticalAlerts()).isEmpty();
        assertThat(conflictAlertService.getSeparationViolationAlerts()).isEmpty();
        assertThat(conflictAlertService.getAircraftConflicts("X")).isEmpty();

        ConflictStatisticsDto stats = conflictAlertService.getConflictStatistics();
        assertThat(stats.totalConflicts()).isZero();
        assertThat(stats.redConflictCount()).isZero();
    }

    @Test
    @DisplayName("ConflictAlertDto has valid structure when conflicts detected")
    void getAircraftConflicts_returnsValidDtoStructureWhenConflictExists() {
        RiskAssessment assessment = new RiskAssessment(
                50.0, 120.0, 3.0, 800.0, false);
        when(conflictDetector.calculateAllConflicts(anyList()))
                .thenReturn(Map.of(new ConflictDetector.ConflictPair("CF1", "CF2"), assessment));

        List<ConflictAlertDto> results = conflictAlertService.getAircraftConflicts("CF1");

        assertThat(results).hasSize(1);
        ConflictAlertDto first = results.get(0);
        assertThat(first.callsignA()).isEqualTo("CF1");
        assertThat(first.callsignB()).isEqualTo("CF2");
        assertThat(first.riskLevel()).isEqualTo(50.0);
        assertThat(first.alertLevel()).isEqualTo("WHITE_CONFLICT");
    }

    @Test
    @DisplayName("getAircraftConflicts uses pair boundary matching instead of substring contains")
    void getAircraftConflicts_filtersByPairBoundary() {
        RiskAssessment related = new RiskAssessment(40.0, 90.0, 4.0, 1200.0, false);
        RiskAssessment unrelatedButContaining = new RiskAssessment(60.0, 80.0, 3.5, 900.0, false);
        when(conflictDetector.calculateAllConflicts(anyList()))
                .thenReturn(Map.of(
                        new ConflictDetector.ConflictPair("A", "B"), related,
                        new ConflictDetector.ConflictPair("JA1-A", "CF2"), unrelatedButContaining
                ));

        List<ConflictAlertDto> results = conflictAlertService.getAircraftConflicts("A");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).callsignA()).isEqualTo("A");
        assertThat(results.get(0).callsignB()).isEqualTo("B");
    }

    @Test
    @DisplayName("getAllConflictAlertsAsDto returns DTO list")
    void getAllConflictAlertsAsDto_returnsDtoList() {
        RiskAssessment a1 = new RiskAssessment(10.0, 200.0, 8.0, 2000.0, false);
        RiskAssessment a2 = new RiskAssessment(80.0, 30.0, 2.0, 500.0, true);
        when(conflictDetector.calculateAllConflicts(anyList()))
                .thenReturn(Map.of(
                        new ConflictDetector.ConflictPair("A", "B"), a1,
                        new ConflictDetector.ConflictPair("C", "D"), a2));

        List<ConflictAlertDto> result = conflictAlertService.getAllConflictAlertsAsDto();

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(alert -> {
            assertThat(alert.callsignA()).isEqualTo("A");
            assertThat(alert.callsignB()).isEqualTo("B");
            assertThat(alert.riskLevel()).isEqualTo(10.0);
        });
        assertThat(result).anySatisfy(alert -> {
            assertThat(alert.callsignA()).isEqualTo("C");
            assertThat(alert.callsignB()).isEqualTo("D");
            assertThat(alert.riskLevel()).isEqualTo(80.0);
            assertThat(alert.conflictPredicted()).isTrue();
        });
    }

    @Test
    @DisplayName("getFilteredConflictAlertsAsDto filters by level and returns DTO list")
    void getFilteredConflictAlertsAsDto_filtersByLevel() {
        RiskAssessment safe = new RiskAssessment(10.0, 200.0, 8.0, 2000.0, false);
        RiskAssessment white = new RiskAssessment(50.0, 120.0, 4.0, 1200.0, false);
        RiskAssessment red = new RiskAssessment(85.0, 45.0, 2.0, 600.0, true);
        when(conflictDetector.calculateAllConflicts(anyList()))
                .thenReturn(Map.of(
                        new ConflictDetector.ConflictPair("A", "B"), safe,
                        new ConflictDetector.ConflictPair("C", "D"), white,
                        new ConflictDetector.ConflictPair("E", "F"), red));

        List<ConflictAlertDto> result =
                conflictAlertService.getFilteredConflictAlertsAsDto("WHITE_CONFLICT");

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(alert -> {
            assertThat(alert.callsignA()).isEqualTo("C");
            assertThat(alert.callsignB()).isEqualTo("D");
        });
        assertThat(result).anySatisfy(alert -> {
            assertThat(alert.callsignA()).isEqualTo("E");
            assertThat(alert.callsignB()).isEqualTo("F");
        });
        assertThat(result).noneSatisfy(alert -> {
            assertThat(alert.callsignA()).isEqualTo("A");
            assertThat(alert.callsignB()).isEqualTo("B");
        });
    }

    @Test
    @DisplayName("getFilteredConflictAlertsAsDto throws on invalid level")
    void getFilteredConflictAlertsAsDto_throwsOnInvalidLevel() {
        Assertions.assertThrows(InvalidParameterException.class,
                () -> conflictAlertService.getFilteredConflictAlertsAsDto("INVALID"));
    }

    @Test
    @DisplayName("getConflictStatistics aggregates counts correctly")
    void getConflictStatistics_aggregatesCorrectly() {
        RiskAssessment safe = new RiskAssessment(10.0, 200.0, 8.0, 2000.0, false);
        RiskAssessment white1 = new RiskAssessment(50.0, 120.0, 4.0, 1200.0, false);
        RiskAssessment white2 = new RiskAssessment(60.0, 90.0, 3.5, 1100.0, true);
        RiskAssessment red = new RiskAssessment(85.0, 45.0, 2.0, 600.0, true);
        when(conflictDetector.calculateAllConflicts(anyList()))
                .thenReturn(Map.of(
                        new ConflictDetector.ConflictPair("A", "B"), safe,
                        new ConflictDetector.ConflictPair("C", "D"), white1,
                        new ConflictDetector.ConflictPair("E", "F"), white2,
                        new ConflictDetector.ConflictPair("G", "H"), red));

        ConflictStatisticsDto result = conflictAlertService.getConflictStatistics();

        assertThat(result.totalConflicts()).isEqualTo(4);
        assertThat(result.safeCount()).isEqualTo(1);
        assertThat(result.whiteConflictCount()).isEqualTo(2);
        assertThat(result.redConflictCount()).isEqualTo(1);
        assertThat(result.separationViolationCount()).isEqualTo(2);
        assertThat(result.maxRiskLevel()).isEqualTo(85.0);
        assertThat(result.avgRiskLevel()).isEqualTo((10.0 + 50.0 + 60.0 + 85.0) / 4.0);
    }
}
