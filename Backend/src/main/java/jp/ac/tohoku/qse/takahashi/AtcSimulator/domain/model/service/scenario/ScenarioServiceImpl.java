package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

public class ScenarioServiceImpl implements ScenarioService {

    private final AirspaceManagement airspaceManagement;
    private final AircraftRepository aircraftRepository;

    public ScenarioServiceImpl(AirspaceManagement airspaceManagement, AircraftRepository aircraftRepository) {
        this.airspaceManagement = airspaceManagement;
        this.aircraftRepository = aircraftRepository;
    }

    public void spawnAircraft(CreateAircraftDto aircraftDto) {
        CommercialAircraft aircraft = new CommercialAircraft(0, aircraftDto.altitude, 0, aircraftDto.latitude, aircraftDto.longitude, 0, aircraftDto.companyName, aircraftDto.flightNumber);
        airspaceManagement.addAircraft(aircraft);
        aircraftRepository.add(aircraft);
    }
}
