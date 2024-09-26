package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.AtsRouteRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.REFRESH_RATE;

// TODO: Airspaceを複数にする
@Configuration
@EnableScheduling
public class AirspaceManagementImpl implements AirspaceManagement {
    AircraftRepository aircraftRepository;

    private int step = 0;
    public AirspaceManagementImpl(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
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

    @Scheduled(fixedRate = 1000 / REFRESH_RATE)
    public void NextStep() {
        System.out.println("Next step called  " + step++ + " times at AirspaceManagementImpl");
        System.out.println(GlobalVariables.callsignExtructStatus.toString());
        aircraftRepository.NextStep();
    }
}
