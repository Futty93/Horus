package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

import java.util.List;

public interface AircraftRepository {
    boolean isAircraftExist(Callsign callsign);
    void add(Aircraft aircraft);
    void remove(Aircraft aircraft);

    Aircraft findByCallsign(Callsign callsign);
    List<Aircraft> findAll();

    void NextStep();
    
}
