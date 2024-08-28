package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

import java.util.List;

public interface AircraftRepository {
    void add(Aircraft aircraft);

    Aircraft find(Callsign callsign);
    List<Aircraft> findAll();
    
}
