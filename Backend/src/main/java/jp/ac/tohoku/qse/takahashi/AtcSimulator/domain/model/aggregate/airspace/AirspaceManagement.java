package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.valueObject.Position;

import java.util.Collection;

public interface AirspaceManagement {

    /**
     * 空域に新しい航空機を追加します。
     * @param aircraft 追加する航空機
     */
    void addAircraftToAirspace(Aircraft aircraft);

    /**
     * 空域から航空機を削除します。
     * @param callsign 削除する航空機のコールサイン
     */
    void removeAircraftFromAirspace(String callsign);

    /**
     * 空域内のすべての航空機を取得します。
     * @return 空域内のすべての航空機のコレクション
     */
    Collection<Aircraft> getAllAircraftsInAirspace();

    /**
     * 航空機の位置を更新します。
     * @param callsign 更新する航空機のコールサイン
     * @param newPosition 更新する位置情報
     */
    void updateAircraftPositionInAirspace(String callsign, Position newPosition);

    /**
     * 空域内に存在する航空機の数を取得します。
     * @return 空域内に存在する航空機の数
     */
    int getAircraftCountInAirspace();

    /**
     * 指定された位置に航空機が重複しているかどうかを確認します。
     * @param position 確認する位置情報
     * @return 重複している場合は true、そうでない場合は false
     */
    boolean isAircraftOverlappingInAirspace(Position position);
}