package jp.ac.tohoku.qse.takahashi.AtcSimulator.application.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the AircraftRadarService interface.
 */
public class AircraftRadarServiceImpl implements AircraftRadarService {

    private final AircraftRepository aircraftRepository;

    @Autowired
    public AircraftRadarServiceImpl(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public String getAircraftLocation(Callsign callsign) {
        return aircraftRepository.find(callsign).getAircraftPosition().toString();
    }

    public String getAllAircraftLocation() {
        StringBuilder sb = new StringBuilder();
        for (Aircraft aircraft : aircraftRepository.findAll()) {
            sb.append(aircraft.getAircraftPosition());
            sb.append("\n");
        }
        return false;
    }

    @Override
    public boolean addAircraft(Aircraft newAircraft) {
        if (aircraftRepository.findByCallsign(newAircraft.getCallsign()).isEmpty()) {
            aircraftRepository.save(newAircraft);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAircraft(String callsign) {
        Optional<Aircraft> existingAircraftOpt = aircraftRepository.findByCallsign(callsign);
        if (existingAircraftOpt.isPresent()) {
            aircraftRepository.delete(existingAircraftOpt.get());
            return true;
        }
        return false;
    }
}