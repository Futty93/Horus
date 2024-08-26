package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface AirSpace {
    /**
     * 空域内に航空機を追加するメソッド
     *
     * @param aircraft 追加する航空機
     */
    void addAircraft(Aircraft aircraft);
    
    /**
     * 空域内から航空機を削除するメソッド
     *
     * @param callsign 削除する航空機のコールサイン
     */
    void removeAircraft(String callsign);

    /**
     * 指定されたコールサインに対応する航空機を取得するメソッド
     *
     * @param callsign 航空機のコールサイン
     * @return 対応する航空機
     */
    Aircraft getAircraft(String callsign);

    /**
     * 空域内のすべての航空機を取得するメソッド
     *
     * @return 空域内のすべての航空機のコレクション
     */
    Collection<Aircraft> getAllAircrafts();

    /**
     * 空域内の航空機の位置を更新するメソッド
     *
     * @param callsign 航空機のコールサイン
     * @param newPosition 新しい位置
     */
    void updateAircraftPosition(String callsign, AircraftPosition newPosition);

    /**
     * 空域内の航空機数を取得するメソッド
     *
     * @return 空域内の航空機数
     */
    int getAircraftCount();
}