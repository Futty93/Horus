package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import java.util.List;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

public interface AircraftRepository {
    boolean isAircraftExist(Callsign callsign);
    void add(Aircraft aircraft);
    void remove(Aircraft aircraft);

    Aircraft findByCallsign(Callsign callsign);
    List<Aircraft> findAll();

    void nextStep();

    void clear();
}
