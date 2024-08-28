package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;

/**
 * 空域の操作を表すインターフェース
 * 空域に航空機を追加・削除の処理を行う
 */
public interface AirspaceManagement {
    public void addAircraft(Aircraft aircraft);
    public void removeAircraft(Aircraft aircraft);

    public void NextStep();

}
