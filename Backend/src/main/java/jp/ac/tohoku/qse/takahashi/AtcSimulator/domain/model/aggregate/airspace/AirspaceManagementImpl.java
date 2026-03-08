package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.FixPositionRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

// TODO: Airspaceを複数にする
public class AirspaceManagementImpl implements AirspaceManagement {

    private static final Logger logger = LoggerFactory.getLogger(AirspaceManagementImpl.class);

    private final AircraftRepository aircraftRepository;
    private final FixPositionRepository fixPositionRepository;

    public AirspaceManagementImpl(AircraftRepository aircraftRepository, FixPositionRepository fixPositionRepository) {
        this.aircraftRepository = aircraftRepository;
        this.fixPositionRepository = fixPositionRepository;
    }

    @Override
    public void addAircraft(Aircraft aircraft) {
        logger.debug("Aircraft added");
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
        return fixPositionRepository.findFixPositionByName(fixName);
    }

    @Override
    public void nextStep() {
        aircraftRepository.nextStep();
    }
}
