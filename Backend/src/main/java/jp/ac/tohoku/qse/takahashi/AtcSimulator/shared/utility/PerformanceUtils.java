package jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * パフォーマンス最適化のためのユーティリティクラス
 * 計算結果のキャッシュ、メモリプール、三角関数の最適化機能を提供
 */
public final class PerformanceUtils {

    // 三角関数のキャッシュテーブル（0.001度刻み）
    private static final double[] SIN_CACHE = new double[SIN_COS_CACHE_SIZE];
    private static final double[] COS_CACHE = new double[SIN_COS_CACHE_SIZE];

    // 距離計算結果のキャッシュ（LRU的な実装）
    private static final Map<String, Double> DISTANCE_CACHE = new ConcurrentHashMap<>(POSITION_CACHE_SIZE);

    // 計算結果再利用のためのオブジェクトプール
    private static final ConcurrentLinkedQueue<double[]> VECTOR_POOL = new ConcurrentLinkedQueue<>();

    // キャッシュの統計情報
    private static volatile long cacheHits = 0;
    private static volatile long cacheMisses = 0;

    // 初期化フラグ
    private static volatile boolean initialized = false;

    // ユーティリティクラスのためプライベートコンストラクタ
    private PerformanceUtils() {
        throw new AssertionError("ユーティリティクラスのインスタンス化は禁止されています");
    }

    /**
     * 初期化処理（アプリケーション起動時に1回実行）
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        // 三角関数キャッシュの事前計算
        for (int i = 0; i < SIN_COS_CACHE_SIZE; i++) {
            double angle = i / SIN_COS_CACHE_PRECISION * DEGREES_TO_RADIANS;
            SIN_CACHE[i] = Math.sin(angle);
            COS_CACHE[i] = Math.cos(angle);
        }

        // ベクトルプールの初期化
        for (int i = 0; i < VECTOR_POOL_SIZE; i++) {
            VECTOR_POOL.offer(new double[3]); // [x, y, z]または[speed, heading, altitude]用
        }

        initialized = true;
    }

    /**
     * 高速な三角関数計算（sin）
     * キャッシュテーブルを使用して高速化
     *
     * @param degrees 角度（度）
     * @return sin値
     */
    public static double fastSin(double degrees) {
        if (!initialized) {
            initialize();
        }

        // 角度を0-360度の範囲に正規化
        double normalizedAngle = MathUtils.normalizeAngle(degrees);

        // キャッシュインデックスを計算
        int index = (int)(normalizedAngle * SIN_COS_CACHE_PRECISION);

        // 範囲チェック
        if (index >= 0 && index < SIN_COS_CACHE_SIZE) {
            return SIN_CACHE[index];
        }

        // キャッシュ範囲外の場合は標準のMath.sinを使用
        return Math.sin(Math.toRadians(normalizedAngle));
    }

    /**
     * 高速な三角関数計算（cos）
     * キャッシュテーブルを使用して高速化
     *
     * @param degrees 角度（度）
     * @return cos値
     */
    public static double fastCos(double degrees) {
        if (!initialized) {
            initialize();
        }

        // 角度を0-360度の範囲に正規化
        double normalizedAngle = MathUtils.normalizeAngle(degrees);

        // キャッシュインデックスを計算
        int index = (int)(normalizedAngle * SIN_COS_CACHE_PRECISION);

        // 範囲チェック
        if (index >= 0 && index < SIN_COS_CACHE_SIZE) {
            return COS_CACHE[index];
        }

        // キャッシュ範囲外の場合は標準のMath.cosを使用
        return Math.cos(Math.toRadians(normalizedAngle));
    }

    /**
     * 距離計算結果のキャッシュ機能付き計算
     *
     * @param pos1 位置1
     * @param pos2 位置2
     * @return キャッシュされた距離または新規計算結果
     */
    public static double getCachedDistance(AircraftPosition pos1, AircraftPosition pos2) {
        // キャッシュキーを生成（位置の精度を適度に丸める）
        String cacheKey = generateDistanceCacheKey(pos1, pos2);

        // キャッシュから検索
        Double cachedDistance = DISTANCE_CACHE.get(cacheKey);
        if (cachedDistance != null) {
            cacheHits++;
            return cachedDistance;
        }

        // キャッシュにない場合は計算
        cacheMisses++;
        double distance = GeodeticUtils.calculateHorizontalDistance(pos1, pos2);

        // キャッシュサイズ制限
        if (DISTANCE_CACHE.size() < POSITION_CACHE_SIZE) {
            DISTANCE_CACHE.put(cacheKey, distance);
        }

        return distance;
    }

    /**
     * ベクトル計算用の配列をプールから取得
     * メモリアロケーションを削減
     *
     * @return 再利用可能な配列
     */
    public static double[] borrowVector() {
        double[] vector = VECTOR_POOL.poll();
        if (vector == null) {
            // プールが空の場合は新規作成
            vector = new double[3];
        }
        return vector;
    }

    /**
     * ベクトル配列をプールに返却
     * メモリ効率の向上
     *
     * @param vector 使用済み配列
     */
    public static void returnVector(double[] vector) {
        if (vector != null && vector.length >= 3) {
            // 配列をクリア
            vector[0] = 0.0;
            vector[1] = 0.0;
            vector[2] = 0.0;

            // プールに返却（サイズ制限有り）
            if (VECTOR_POOL.size() < VECTOR_POOL_SIZE) {
                VECTOR_POOL.offer(vector);
            }
        }
    }

    /**
     * 最適化された平方根計算
     * 小さな値に対してはテイラー展開を使用
     *
     * @param x 入力値
     * @return 平方根
     */
    public static double fastSqrt(double x) {
        if (x < 0) {
            return Double.NaN;
        }
        if (x == 0 || x == 1) {
            return x;
        }

        // 小さな値に対してはテイラー展開で近似
        if (x < 0.01) {
            // sqrt(x) ≈ sqrt(a) + (x-a)/(2*sqrt(a)) where a is close to x
            double a = 0.01;
            double sqrtA = Math.sqrt(a);
            return sqrtA + (x - a) / (2 * sqrtA);
        }

        // 通常の値は標準のMath.sqrtを使用
        return Math.sqrt(x);
    }

    /**
     * 距離キャッシュキーの生成
     * 位置精度を適度に丸めてキャッシュ効率を向上
     */
    private static String generateDistanceCacheKey(AircraftPosition pos1, AircraftPosition pos2) {
        // 精度を落として丸める（0.001度 ≈ 約100m）
        double lat1 = Math.round(pos1.latitude.toDouble() * 1000.0) / 1000.0;
        double lon1 = Math.round(pos1.longitude.toDouble() * 1000.0) / 1000.0;
        double lat2 = Math.round(pos2.latitude.toDouble() * 1000.0) / 1000.0;
        double lon2 = Math.round(pos2.longitude.toDouble() * 1000.0) / 1000.0;

        // 順序を正規化（pos1とpos2の順序に依存しないキー）
        if (lat1 > lat2 || (lat1 == lat2 && lon1 > lon2)) {
            return String.format("%.3f,%.3f-%.3f,%.3f", lat2, lon2, lat1, lon1);
        } else {
            return String.format("%.3f,%.3f-%.3f,%.3f", lat1, lon1, lat2, lon2);
        }
    }

    /**
     * キャッシュの統計情報を取得
     *
     * @return [ヒット数, ミス数, ヒット率%]
     */
    public static double[] getCacheStatistics() {
        long totalRequests = cacheHits + cacheMisses;
        double hitRate = totalRequests > 0 ? (double)cacheHits / totalRequests * 100.0 : 0.0;
        return new double[]{cacheHits, cacheMisses, hitRate};
    }

    /**
     * キャッシュクリア（テスト用）
     */
    public static void clearCache() {
        DISTANCE_CACHE.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }

    /**
     * 最適化の効果を確認するためのベンチマーク
     * アプリケーション起動時の初期化処理で使用
     */
    public static void runOptimizationBenchmark() {
        if (!initialized) {
            initialize();
        }

        System.out.println("=== Performance Optimization Benchmark ===");

        // 三角関数のベンチマーク
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            fastSin(i % 360);
        }
        long fastTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            Math.sin(Math.toRadians(i % 360));
        }
        long standardTime = System.nanoTime() - startTime;

        System.out.printf("Sin calculation: Fast=%dms, Standard=%dms, Speedup=%.2fx%n",
                fastTime / 1_000_000, standardTime / 1_000_000,
                (double)standardTime / fastTime);

        System.out.println("============================================");
    }
}
