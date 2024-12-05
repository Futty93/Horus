package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.FlightPlan;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.Fix;

import java.util.List;

public class FlightRoute {
    public List<Fix> fixes;

    public FlightRoute() {
        // init fixes
        this.fixes = List.of();
    }

    public void addFix(Fix fix){
        this.fixes.add(fix);
    }

}
