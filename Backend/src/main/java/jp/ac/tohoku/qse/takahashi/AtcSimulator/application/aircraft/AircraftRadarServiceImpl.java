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

    @Override
    public List<Aircraft> getAllAircraft() {
        return aircraftRepository.findAll();
    }

    @Override
    public Aircraft getAircraftByCallsign(String callsign) {
        return aircraftRepository.findByCallsign(callsign).orElse(null);
    }

    @Override
    public boolean updateAircraft(String callsign, Aircraft updatedAircraft) {
        Optional<Aircraft> existingAircraftOpt = aircraftRepository.findByCallsign(callsign);
        if (existingAircraftOpt.isPresent()) {
            Aircraft existingAircraft = existingAircraftOpt.get();
            existingAircraft.updateWith(updatedAircraft);
            aircraftRepository.save(existingAircraft);
            return true;
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