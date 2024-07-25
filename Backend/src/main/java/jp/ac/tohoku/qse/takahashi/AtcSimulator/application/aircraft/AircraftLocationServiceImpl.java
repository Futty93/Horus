package jp.ac.tohoku.qse.takahashi.AtcSimulator.application.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftLocationService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aircraft.AircraftRepository;

public class AircraftLocationServiceImpl implements AircraftLocationService {

    private final AircraftRepository aircraftRepository;

    public AircraftLocationServiceImpl(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public String getAircraftLocation(int id) {
        return aircraftRepository.find(id).getLocation();
    }

    public String getAllAircraftLocation() {
        StringBuilder sb = new StringBuilder();
        for (Aircraft aircraft : aircraftRepository.findAll()) {
            sb.append(aircraft.getLocation());
            sb.append("\n");
        }
        return sb.toString();
    }
}
