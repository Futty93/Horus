package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository {

    /**
     * 航空機を保存するメソッド。
     * @param aircraft 保存する航空機エンティティ
     */
    void save(Aircraft aircraft);

    /**
     * コールサインによって航空機を取得するメソッド。
     * @param callsign 取得する航空機のコールサイン
     * @return 航空機エンティティのオプショナル
     */
    Optional<Aircraft> findByCallsign(Callsign callsign);

    /**
     * すべての航空機を取得するメソッド。
     * @return すべての航空機エンティティのリスト
     */
    List<Aircraft> findAll();

    /**
     * コールサインによって航空機を削除するメソッド。
     * @param callsign 削除する航空機のコールサイン
     */
    void deleteByCallsign(Callsign callsign);

    /**
     * 航空機の情報を更新するメソッド。
     * @param aircraft 更新する航空機エンティティ
     */
    void update(Aircraft aircraft);
}