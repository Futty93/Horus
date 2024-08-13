package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;

public interface AirspaceManagement {
    public void addAircraft(Aircraft aircraft);
    public void removeAircraft(Aircraft aircraft);

    public void NextStep();
}
