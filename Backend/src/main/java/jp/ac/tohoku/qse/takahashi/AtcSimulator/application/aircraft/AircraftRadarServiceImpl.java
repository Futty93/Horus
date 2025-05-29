package jp.ac.tohoku.qse.takahashi.AtcSimulator.application.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import org.springframework.stereotype.Service;

@Service
public class AircraftRadarServiceImpl implements AircraftRadarService {

    private final AircraftRepository aircraftRepository;

    public AircraftRadarServiceImpl(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public String getAircraftLocation(Callsign callsign) {
        Aircraft aircraft = aircraftRepository.findByCallsign(callsign);
        if (aircraft instanceof AircraftBase) {
            return ((AircraftBase) aircraft).toRadarString();
        }
        return aircraft.toString(); // フォールバック
    }

    public String getAllAircraftLocation() {
        StringBuilder sb = new StringBuilder();
        for (Aircraft aircraft : aircraftRepository.findAll()) {
            if (aircraft instanceof AircraftBase) {
                sb.append(((AircraftBase) aircraft).toRadarString());
            } else {
                sb.append(aircraft.toString()); // フォールバック
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
