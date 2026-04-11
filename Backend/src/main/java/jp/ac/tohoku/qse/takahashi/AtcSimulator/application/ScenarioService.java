package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;

public interface ScenarioService {
    void spawnAircraft(Aircraft aircraft);
    void instructAircraft(Callsign callsign, InstructedVector instructedVector);

    void setAtcClearance(Callsign callsign, InstructedVector clearance);

    void directFixAircraft(Callsign callsign, String fixName);

    void directToFix(Callsign callsign, String fixName, boolean resumeFlightPlan);

    void resumeNavigation(Callsign callsign);
}
