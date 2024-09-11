package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.aircraft.AircraftRadarServiceImpl;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirSpace;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagementImpl;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario.ScenarioServiceImpl;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api.CreateAircraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AtcSimulatorApplicationConfig
{
    @Autowired
    AircraftRepository aircraftRepository;

    @Autowired
    AirSpace airSpace;

    @Bean
    public AircraftRadarService aircraftLocationService() {
        return new AircraftRadarServiceImpl(aircraftRepository);
    }

    @Bean
    public CreateAircraftService createAircraftService() {
        return new CreateAircraftService(scenarioService(), aircraftRepository);
    }

    @Bean
    public AirspaceManagement airspaceManagement() {
        return new AirspaceManagementImpl(aircraftRepository);
    }

    @Bean
    public ScenarioService scenarioService() {
        return new ScenarioServiceImpl(airspaceManagement());
    }
}
