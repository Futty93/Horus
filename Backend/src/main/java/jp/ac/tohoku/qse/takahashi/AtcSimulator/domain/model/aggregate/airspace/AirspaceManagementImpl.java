package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.AtsRouteRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Optional;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.REFRESH_RATE;

// TODO: Airspaceを複数にする
@Configuration
@EnableScheduling
public class AirspaceManagementImpl implements AirspaceManagement {
    AircraftRepository aircraftRepository;
    AtsRouteRepository atsRouteRepository;

    private int step = 0;
    public AirspaceManagementImpl(AircraftRepository aircraftRepository, AtsRouteRepository atsRouteRepository) {
        this.aircraftRepository = aircraftRepository;
        this.atsRouteRepository = atsRouteRepository;
    }

    @Override
    public void addAircraft(Aircraft aircraft) {
        System.out.println("Aircraft added");
        aircraftRepository.add(aircraft);
    }

    @Override
    public void removeAircraft(Aircraft aircraft) {
        aircraftRepository.remove(aircraft);
    }

    @Override
    public Aircraft findAircraftByCallsign(Callsign callsign) {
        return aircraftRepository.findByCallsign(callsign);
    }

    @Override
    public Optional<FixPosition> getFixPosition(String fixName) {
        return atsRouteRepository.findFixPositionByName(fixName);
    }

    @Scheduled(fixedRate = 1000 / REFRESH_RATE)
    public void NextStep() {
        if (!GlobalVariables.isSimulationRunning) {
            return;
        }
        System.out.println("Next step called  " + step++ + " times at AirspaceManagementImpl");
        aircraftRepository.NextStep();
    }
}
