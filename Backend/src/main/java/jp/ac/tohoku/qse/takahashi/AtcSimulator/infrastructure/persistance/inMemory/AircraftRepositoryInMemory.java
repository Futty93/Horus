package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistance.inMemory;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AircraftRepositoryInMemory implements AircraftRepository {

    private final ConcurrentMap<String, Aircraft> aircraftMap = new ConcurrentHashMap<>();

    @Override
    public List<Aircraft> findAll() {
        return new ArrayList<>(aircraftMap.values());
    }

    @Override
    public Optional<Aircraft> findById(String callsign) {
        return Optional.ofNullable(aircraftMap.get(callsign));
    }

    @Override
    public void save(Aircraft aircraft) {
        aircraftMap.put(aircraft.getCallsign(), aircraft);
    }

    @Override
    public void deleteById(String callsign) {
        aircraftMap.remove(callsign);
    }
}