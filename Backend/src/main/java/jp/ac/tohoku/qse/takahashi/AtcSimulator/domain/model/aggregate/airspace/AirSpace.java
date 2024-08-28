package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;

/**
 * 空域を表すインターフェース
 * 侵入禁止空域の判定や墜落等の処理を行う
 */
public interface AirSpace {
    public void addAircraft(Aircraft aircraft);
    public void removeAircraft(Aircraft aircraft);

    void NextStep();
}
