package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistance.inMemory;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftConflictException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * - 統一された例外処理による安定性向上
 */
@Repository
public class AircraftRepositoryInMemory implements AircraftRepository {

    private static final Logger logger = LoggerFactory.getLogger(AircraftRepositoryInMemory.class);

    // コールサインをキーとするマップ（O(1)検索）
    private final ConcurrentHashMap<String, Aircraft> aircraftMap = new ConcurrentHashMap<>();

    // NextStepの同期制御用（全航空機の一括更新のため）
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public AircraftRepositoryInMemory() {
        logger.info("AircraftRepositoryInMemory初期化完了");
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
        if (callsign == null) {
            logger.warn("isAircraftExist: nullコールサインが渡されました");
            return false;
        }
        return aircraftMap.containsKey(callsign.toString());
    }

    /**
     * 航空機をリポジトリに追加
     *
     * @param aircraft 追加する航空機
     * @throws AircraftConflictException 同じコールサインの航空機が既に存在する場合
     * @throws IllegalArgumentException aircraftがnullの場合
     */
    @Override
    public void add(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("追加する航空機がnullです");
        }

        String callsignKey = aircraft.getCallsign().toString();

        // 重複チェック
        if (aircraftMap.containsKey(callsignKey)) {
            logger.warn("航空機追加失敗: コールサイン '{}' は既に存在します", callsignKey);
            throw new AircraftConflictException(callsignKey);
        }

        aircraftMap.put(callsignKey, aircraft);
        logger.debug("航空機追加成功: {}", callsignKey);
    }

    /**
     * コールサインによる航空機検索
     * O(1)の高速検索
     *
     * @param callsign 検索するコールサイン
     * @return 見つかった航空機
     * @throws AircraftNotFoundException 航空機が見つからない場合
     * @throws IllegalArgumentException callsignがnullの場合
     */
    @Override
    public Aircraft findByCallsign(Callsign callsign) {
        if (callsign == null) {
            throw new IllegalArgumentException("検索するコールサインがnullです");
        }

        String callsignKey = callsign.toString();
        Aircraft aircraft = aircraftMap.get(callsignKey);

        if (aircraft == null) {
            logger.warn("航空機検索失敗: コールサイン '{}' が見つかりません", callsignKey);
            throw new AircraftNotFoundException(callsignKey);
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
        List<Aircraft> aircraftList = new ArrayList<>(aircraftMap.values());
        logger.debug("全航空機取得: {}機", aircraftList.size());
        return aircraftList;
    }

    /**
     * 航空機をリポジトリから削除
     *
     * @param aircraft 削除する航空機
     * @throws AircraftNotFoundException 航空機が見つからない場合
     * @throws IllegalArgumentException aircraftがnullの場合
     */
    @Override
    public void remove(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("削除する航空機がnullです");
        }

        String callsignKey = aircraft.getCallsign().toString();
        Aircraft removed = aircraftMap.remove(callsignKey);

        if (removed == null) {
            logger.warn("航空機削除失敗: コールサイン '{}' が見つかりません", callsignKey);
            throw new AircraftNotFoundException(callsignKey);
        }

        logger.debug("航空機削除成功: {}", callsignKey);
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
            logger.debug("全航空機の次ステップ計算開始: {}機", aircraftMap.size());

            // 全航空機の状態を一括更新
            int processedCount = 0;
            for (Aircraft aircraft : aircraftMap.values()) {
                try {
                    aircraft.calculateNextAircraftVector();
                    aircraft.calculateNextAircraftPosition();
                    processedCount++;
                } catch (Exception e) {
                    logger.error("航空機 '{}' の次ステップ計算でエラー: {}",
                               aircraft.getCallsign().toString(), e.getMessage(), e);
                }
            }

            logger.debug("全航空機の次ステップ計算完了: {}/{}機処理", processedCount, aircraftMap.size());
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
        int beforeSize = aircraftMap.size();
        aircraftMap.clear();
        logger.info("リポジトリクリア完了: {}機削除", beforeSize);
    }
}
