package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.GetAllAircraftLocationsWithRiskUseCase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.RouteSuggestionService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ScenarioServiceImpl;
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
    public RouteSuggestionService routeSuggestionService(
            AtsRouteFixPositionRepository atsRouteFixPositionRepository) {
        return new RouteSuggestionService(atsRouteFixPositionRepository);
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

}
