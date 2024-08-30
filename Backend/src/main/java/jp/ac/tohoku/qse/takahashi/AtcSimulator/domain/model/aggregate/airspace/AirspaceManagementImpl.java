package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistance.inMemory.AircraftRepositoryInMemory;
import org.springframework.scheduling.annotation.Scheduled;

// TODO: Airspaceを複数にする
public class AirspaceManagementImpl implements AirspaceManagement {

    AircraftRepository aircraftRepository;

    public AirspaceManagementImpl(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    @Override
    public void addAircraft(Aircraft aircraft) {
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

    @Scheduled(fixedRate = 1000)
    public void NextStep() {
        aircraftRepository.NextStep();
    }
}
