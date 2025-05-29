package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistance.inMemory;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * インメモリによる航空機リポジトリの実装
 * パフォーマンス最適化：
 * - コールサインをキーとするMapによる高速検索（O(1)）
 * - スレッドセーフな実装（ConcurrentHashMap + ReadWriteLock）
 * - 効率的なデータ構造による大規模データ対応
 */
@Repository
public class AircraftRepositoryInMemory implements AircraftRepository {

    // コールサインをキーとするマップ（O(1)検索）
    private final ConcurrentHashMap<String, Aircraft> aircraftMap = new ConcurrentHashMap<>();

    // NextStepの同期制御用（全航空機の一括更新のため）
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public AircraftRepositoryInMemory() {
        // コメントアウトされた初期化コードを削除し、クリーンな実装とする
    }

    /**
     * 指定されたコールサインの航空機が存在するかチェック
     * O(1)の高速検索
     *
     * @param callsign 航空機のコールサイン
     * @return 航空機が存在する場合true、そうでなければfalse
     */
    @Override
    public boolean isAircraftExist(Callsign callsign) {
        return aircraftMap.containsKey(callsign.toString());
    }

    /**
     * 航空機をリポジトリに追加
     *
     * @param aircraft 追加する航空機
     * @throws IllegalArgumentException 同じコールサインの航空機が既に存在する場合
     */
    @Override
    public void add(Aircraft aircraft) {
        String callsignKey = aircraft.getCallsign().toString();

        // 重複チェック
        if (aircraftMap.containsKey(callsignKey)) {
            throw new IllegalArgumentException("Aircraft with callsign " + callsignKey + " already exists");
        }

        aircraftMap.put(callsignKey, aircraft);
    }

    /**
     * コールサインによる航空機検索
     * O(1)の高速検索
     *
     * @param callsign 検索するコールサイン
     * @return 見つかった航空機
     * @throws IllegalArgumentException 航空機が見つからない場合
     */
    @Override
    public Aircraft findByCallsign(Callsign callsign) {
        Aircraft aircraft = aircraftMap.get(callsign.toString());
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft not found: " + callsign.toString());
        }
        return aircraft;
    }

    /**
     * 全航空機のリストを取得
     *
     * @return 全航空機のリスト（新しいリストインスタンス）
     */
    @Override
    public List<Aircraft> findAll() {
        return new ArrayList<>(aircraftMap.values());
    }

    /**
     * 航空機をリポジトリから削除
     *
     * @param aircraft 削除する航空機
     * @throws IllegalArgumentException 航空機が見つからない場合
     */
    @Override
    public void remove(Aircraft aircraft) {
        String callsignKey = aircraft.getCallsign().toString();
        Aircraft removed = aircraftMap.remove(callsignKey);

        if (removed == null) {
            throw new IllegalArgumentException("Aircraft not found for removal: " + callsignKey);
        }
    }

    /**
     * 全航空機の次ステップ計算を実行
     * 同期制御により、計算の一貫性を保証
     *
     * Note: この操作は本来リポジトリの責務ではないが、
     * 既存の設計との互換性のため維持している
     */
    @Override
    public void NextStep() {
        lock.writeLock().lock();
        try {
            // 全航空機の状態を一括更新
            for (Aircraft aircraft : aircraftMap.values()) {
                aircraft.calculateNextAircraftVector();
                aircraft.calculateNextAircraftPosition();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * リポジトリの状態情報を取得（デバッグ・監視用）
     *
     * @return 航空機数を含む状態情報
     */
    public String getRepositoryInfo() {
        return String.format("AircraftRepositoryInMemory: %d aircraft(s) stored", aircraftMap.size());
    }

    /**
     * リポジトリをクリア（テスト用）
     */
    public void clear() {
        aircraftMap.clear();
    }
}
