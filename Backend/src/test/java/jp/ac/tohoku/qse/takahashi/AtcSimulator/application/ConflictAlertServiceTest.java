package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict.ConflictDetector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.AlertLevel;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistence.inMemory.AircraftRepositoryInMemory;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ConflictAlertDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ConflictStatisticsDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

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
        assertThat(first.alertLevel()).isEqualTo(AlertLevel.fromRiskLevel(50.0).name());
    }
}
