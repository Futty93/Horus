package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.constants.AtcSimulatorConstants.TICK_INTERVAL_MS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.GetAllAircraftLocationsWithRiskUseCase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ScenarioServiceImpl;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagementImpl;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.FixPositionRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix.AtsRouteFixPositionRepository;

/**
 * Domain and application layer bean configuration.
 * Provides beans that were previously auto-discovered via @Service.
 */
@Configuration
@EnableScheduling
public class AtcSimulatorDomainConfig {

    @Bean
    public GetAllAircraftLocationsWithRiskUseCase getAllAircraftLocationsWithRiskUseCase(
            AircraftRepository aircraftRepository,
            ConflictAlertService conflictAlertService) {
        return new GetAllAircraftLocationsWithRiskUseCase(aircraftRepository, conflictAlertService);
    }

    @Bean
    public AtsRouteFixPositionRepository atsRouteFixPositionRepository() {
        return new AtsRouteFixPositionRepository();
    }

    @Bean
    public AirspaceManagement airspaceManagement(AircraftRepository aircraftRepository,
                                                 FixPositionRepository fixPositionRepository) {
        return new AirspaceManagementImpl(aircraftRepository, fixPositionRepository);
    }

    @Bean
    public ScenarioService scenarioService(AirspaceManagement airspaceManagement) {
        return new ScenarioServiceImpl(airspaceManagement);
    }

    @Bean
    public SimulationScheduler simulationScheduler(AirspaceManagement airspaceManagement) {
        return new SimulationScheduler(airspaceManagement);
    }

    /**
     * Scheduler that delegates to AirspaceManagement.
     * Extracted to avoid @Scheduled in config with constructor-injected dependency.
     */
    public static class SimulationScheduler {

        private final AirspaceManagement airspaceManagement;

        public SimulationScheduler(AirspaceManagement airspaceManagement) {
            this.airspaceManagement = airspaceManagement;
        }

        @Scheduled(fixedRate = TICK_INTERVAL_MS)
        public void tick() {
            if (!GlobalVariables.isSimulationRunning) {
                return;
            }
            airspaceManagement.nextStep();
        }
    }
}
