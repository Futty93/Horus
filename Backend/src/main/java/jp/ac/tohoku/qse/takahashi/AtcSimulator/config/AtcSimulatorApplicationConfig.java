package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftLocationService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.aircraft.AircraftLocationServiceImpl;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aircraft.AircraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AtcSimulatorApplicationConfig
{
    @Autowired
    AircraftRepository aircraftRepository;
    @Bean
    public AircraftLocationService aircraftLocationService() {
        return new AircraftLocationServiceImpl(aircraftRepository);
    }
}
