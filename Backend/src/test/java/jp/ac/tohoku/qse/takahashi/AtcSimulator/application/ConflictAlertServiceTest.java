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
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.RiskAssessmentDto;

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
                .thenReturn(Map.of("CF1-CF2", assessment));

        CreateAircraftDto dto1 = new CreateAircraftDto(
                "CF1", 35.0, 139.0, 35000, 450, 0, 90,
                "B738", "HND", "RJTT", "NRT", "RJAA", "2024-12-13T14:30:00Z"
        );
        CreateAircraftDto dto2 = new CreateAircraftDto(
                "CF2", 35.001, 139.001, 35000, 450, 0, 270,
                "B738", "HND", "RJTT", "NRT", "RJAA", "2024-12-13T14:30:00Z"
        );
        aircraftRepository.add(AircraftFactory.createCommercialAircraft(dto1));
        aircraftRepository.add(AircraftFactory.createCommercialAircraft(dto2));

        List<ConflictAlertDto> results = conflictAlertService.getAircraftConflicts("CF1");

        assertThat(results).hasSize(1);
        ConflictAlertDto first = results.get(0);
        assertThat(first.pairId()).isEqualTo("CF1-CF2");
        assertThat(first.pairId().split("-")).hasSize(2);
        assertThat(first.riskLevel()).isEqualTo(50.0);
        assertThat(first.alertLevel()).isEqualTo("WHITE_CONFLICT");
    }

    @Test
    @DisplayName("getAllConflictAlertsAsDto returns DTO map")
    void getAllConflictAlertsAsDto_returnsDtoMap() {
        RiskAssessment a1 = new RiskAssessment(10.0, 200.0, 8.0, 2000.0, false);
        RiskAssessment a2 = new RiskAssessment(80.0, 30.0, 2.0, 500.0, true);
        when(conflictDetector.calculateAllConflicts(anyList()))
                .thenReturn(Map.of("A-B", a1, "C-D", a2));

        Map<String, RiskAssessmentDto> result = conflictAlertService.getAllConflictAlertsAsDto();

        assertThat(result).hasSize(2);
        assertThat(result.get("A-B").riskLevel()).isEqualTo(10.0);
        assertThat(result.get("C-D").riskLevel()).isEqualTo(80.0);
        assertThat(result.get("C-D").conflictPredicted()).isTrue();
    }

    @Test
    @DisplayName("getFilteredConflictAlertsAsDto filters by level and returns DTO map")
    void getFilteredConflictAlertsAsDto_filtersByLevel() {
        RiskAssessment safe = new RiskAssessment(10.0, 200.0, 8.0, 2000.0, false);
        RiskAssessment white = new RiskAssessment(50.0, 120.0, 4.0, 1200.0, false);
        RiskAssessment red = new RiskAssessment(85.0, 45.0, 2.0, 600.0, true);
        when(conflictDetector.calculateAllConflicts(anyList()))
                .thenReturn(Map.of("A-B", safe, "C-D", white, "E-F", red));

        Map<String, RiskAssessmentDto> result =
                conflictAlertService.getFilteredConflictAlertsAsDto("WHITE_CONFLICT");

        assertThat(result).hasSize(2);
        assertThat(result).containsKey("C-D");
        assertThat(result).containsKey("E-F");
        assertThat(result).doesNotContainKey("A-B");
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
                .thenReturn(Map.of("A-B", safe, "C-D", white1, "E-F", white2, "G-H", red));

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
