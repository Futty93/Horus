package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import org.springframework.scheduling.annotation.Scheduled;

// TODO: Airspaceを複数にする
public class AirspaceManagementImpl implements AirspaceManagement {
    private final AirSpace airSpace;

    public AirspaceManagementImpl(AirSpace airSpaceAggregates) {
        this.airSpace = airSpaceAggregates;
    }

    public void addAircraft(Aircraft aircraft) {
        airSpace.addAircraft(aircraft);
    }

    public void removeAircraft(Aircraft aircraft) {
        airSpace.removeAircraft(aircraft);
    }

    @Scheduled(fixedRate = 1000)
    public void NextStep() {
        airSpace.NextStep();
    }

}
