package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.valueObject.Position;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AirSpace {

    private Map<String, Aircraft> aircrafts; // 空域内の航空機を追跡するためのマップ

    public AirSpace() {
        this.aircrafts = new HashMap<>();
    }

    /**
     * 空域内に航空機を追加するメソッド
     *
     * @param aircraft 追加する航空機
     */
    public void addAircraft(Aircraft aircraft) {
        this.aircrafts.put(aircraft.getCallsign().getValue(), aircraft);
    }

    /**
     * 空域内から航空機を削除するメソッド
     *
     * @param callsign 削除する航空機のコールサイン
     */
    public void removeAircraft(String callsign) {
        this.aircrafts.remove(callsign);
    }

    /**
     * 指定されたコールサインに対応する航空機を取得するメソッド
     *
     * @param callsign 航空機のコールサイン
     * @return 対応する航空機
     */
    public Aircraft getAircraft(String callsign) {
        return this.aircrafts.get(callsign);
    }

    /**
     * 空域内のすべての航空機を取得するメソッド
     *
     * @return 空域内のすべての航空機のコレクション
     */
    public Collection<Aircraft> getAllAircrafts() {
        return this.aircrafts.values();
    }

    /**
     * 空域内の航空機の位置を更新するメソッド
     *
     * @param callsign 航空機のコールサイン
     * @param newPosition 新しい位置
     */
    public void updateAircraftPosition(String callsign, Position newPosition) {
        Aircraft aircraft = this.aircrafts.get(callsign);
        if (aircraft != null) {
            aircraft.setPosition(newPosition);
        }
    }

    /**
     * 空域内の航空機数を取得するメソッド
     *
     * @return 空域内の航空機数
     */
    public int getAircraftCount() {
        return this.aircrafts.size();
    }
}