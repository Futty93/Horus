package jp.ac.tohoku.qse.takahashi.AtcSimulator.application.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

public class AircraftRadarServiceImpl implements AircraftRadarService {

    private final AircraftRepository aircraftRepository;

    public AircraftRadarServiceImpl(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public String getAircraftLocation(Callsign callsign) {
        return aircraftRepository.find(callsign).getLocation();
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
