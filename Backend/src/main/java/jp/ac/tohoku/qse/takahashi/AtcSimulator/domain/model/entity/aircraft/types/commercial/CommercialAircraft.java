package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FixedWingFlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.GeodeticUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.MathUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PerformanceUtils;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.PositionUtils;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * 商用旅客機クラス（最適化版）
 * 一般的な旅客機の特性を実装
 * パフォーマンス最適化により大規模シミュレーションに対応
 */
public class CommercialAircraft extends AircraftBase {

    private final String originIata;
    private final String originIcao;
    private final String destinationIata;
    private final String destinationIcao;
    private final String eta; // Estimated Time of Arrival in ISO 8601 format

    // 最適化のためのキャッシュ変数
    private AircraftPosition lastCachedPosition;
    private AircraftVector lastCachedVector;
    private long lastPositionUpdateTime;
    private long lastVectorUpdateTime;

    // 計算効率化のための前回値保持
    private double previousDistanceTraveled = 0.0;
    private double previousHeadingChange = 0.0;

    // レーダー表示文字列のキャッシュ
    private String cachedRadarString;
    private long lastRadarStringUpdate = 0;

    /**
     * 商用旅客機のデフォルト特性（最適化済み）
     */
    private static final AircraftCharacteristics DEFAULT_COMMERCIAL_CHARACTERISTICS =
        new AircraftCharacteristics(
            3.0,   // maxAcceleration (kts/s)
            3.0,   // maxTurnRate (deg/s)
            1640.0, // maxClimbRate (ft/min)
            500.0, // maxSpeed (kts)
            140.0, // minSpeed (kts)
            42000.0, // maxAltitude (ft)
            0.0,   // minAltitude (ft)
            AircraftCharacteristics.AircraftCategory.COMMERCIAL_PASSENGER
        );

    /**
     * コンストラクタ
     */
    public CommercialAircraft(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition,
                            AircraftVector aircraftVector, String originIata, String originIcao,
                            String destinationIata, String destinationIcao, String eta) {
        super(callsign, aircraftType, aircraftPosition, aircraftVector,
              new FixedWingFlightBehavior(), DEFAULT_COMMERCIAL_CHARACTERISTICS);

        this.originIata = originIata;
        this.originIcao = originIcao;
        this.destinationIata = destinationIata;
        this.destinationIcao = destinationIcao;
        this.eta = eta;

        // 最適化用初期化
        this.lastCachedPosition = aircraftPosition;
        this.lastCachedVector = aircraftVector;
        this.lastPositionUpdateTime = System.currentTimeMillis();
        this.lastVectorUpdateTime = System.currentTimeMillis();

        // パフォーマンスユーティリティの初期化確認
        PerformanceUtils.initialize();
    }

    /**
     * カスタム特性を持つコンストラクタ
     */
    public CommercialAircraft(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition,
                            AircraftVector aircraftVector, String originIata, String originIcao,
                            String destinationIata, String destinationIcao, String eta,
                            AircraftCharacteristics customCharacteristics) {
        super(callsign, aircraftType, aircraftPosition, aircraftVector,
              new FixedWingFlightBehavior(), customCharacteristics);

        this.originIata = originIata;
        this.originIcao = originIcao;
        this.destinationIata = destinationIata;
        this.destinationIcao = destinationIcao;
        this.eta = eta;

        // 最適化用初期化
        this.lastCachedPosition = aircraftPosition;
        this.lastCachedVector = aircraftVector;
        this.lastPositionUpdateTime = System.currentTimeMillis();
        this.lastVectorUpdateTime = System.currentTimeMillis();

        // パフォーマンスユーティリティの初期化確認
        PerformanceUtils.initialize();
    }

    /**
     * 最適化された次位置計算
     * 必要以上の計算を避け、効率的な位置更新を実現
     */
    @Override
    public void calculateNextAircraftPosition() {
        long currentTime = System.currentTimeMillis();

        // 位置更新の必要性をチェック（しきい値による最適化）
        if (!shouldUpdatePosition()) {
            return;
        }

        // 移動距離の事前計算
        double refreshRateInSeconds = 1.0 / REFRESH_RATE;
        double groundSpeedKmPerSec = aircraftVector.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3600.0;
        double distanceTraveled = groundSpeedKmPerSec * refreshRateInSeconds;

        // 微小移動の場合は平面近似を使用（高速）
        AircraftPosition newPosition;
        if (distanceTraveled < CommercialAircraftConstants.PLANAR_APPROXIMATION_THRESHOLD) {
            newPosition = calculatePositionPlanar(refreshRateInSeconds);
        } else {
            // 長距離移動の場合は高精度計算
            newPosition = GeodeticUtils.predictPosition(aircraftPosition, aircraftVector, refreshRateInSeconds);
        }

        // 位置の変化量をチェックして更新
        if (isSignificantPositionChange(newPosition)) {
            this.aircraftPosition = newPosition;
            this.lastCachedPosition = newPosition;
            this.lastPositionUpdateTime = currentTime;
            this.previousDistanceTraveled = distanceTraveled;

            // レーダー表示文字列のキャッシュを無効化
            invalidateRadarStringCache();
        }
    }

    /**
     * 最適化された次ベクトル計算
     * 変化量がしきい値以下の場合は更新をスキップ
     * 高度同期問題を解決するための改善版
     */
    @Override
    public void calculateNextAircraftVector() {
        long currentTime = System.currentTimeMillis();

        // ベクトル更新の必要性をチェック
        if (!shouldUpdateVector()) {
            return;
        }

        // 既存の計算ロジックを呼び出し（FlightBehaviorに委譲）
        super.calculateNextAircraftVector();

        // 変化量をチェックして更新
        if (isSignificantVectorChange()) {
            this.lastCachedVector = this.aircraftVector;
            this.lastVectorUpdateTime = currentTime;

            // レーダー表示文字列のキャッシュを無効化
            invalidateRadarStringCache();
        }
    }

    /**
     * 最適化された旋回角計算
     * キャッシュされた距離計算結果を活用
     */
    @Override
    public double calculateTurnAngle(FixPosition fixPosition) {
        // GeodeticUtilsの最適化された距離計算を活用
        return PositionUtils.calculateBearingToTarget(
            this.aircraftPosition,
            fixPosition.latitude.toDouble(),
            fixPosition.longitude.toDouble()
        );
    }

    /**
     * 高速な平面近似による位置計算
     */
    private AircraftPosition calculatePositionPlanar(double refreshRateInSeconds) {
        return PositionUtils.calculateNextPosition(
            aircraftPosition,
            aircraftVector.groundSpeed.toDouble(),
            aircraftVector.heading.toDouble(),
            aircraftVector.verticalSpeed.toDouble(),
            refreshRateInSeconds
        );
    }

    /**
     * 位置更新の必要性判定
     * パフォーマンス向上のためのしきい値チェック
     */
    private boolean shouldUpdatePosition() {
        // 速度がしきい値以下の場合は更新をスキップ
        if (aircraftVector.groundSpeed.toDouble() < SPEED_UPDATE_THRESHOLD) {
            return false;
        }

        // 前回更新からの経過時間をチェック
        long timeSinceLastUpdate = System.currentTimeMillis() - lastPositionUpdateTime;
        return timeSinceLastUpdate >= (1000 / REFRESH_RATE); // リフレッシュレートに基づく最小間隔
    }

    /**
     * ベクトル更新の必要性判定
     * 高度同期問題を解決するため、航空機ベクトルの垂直速度を基準に判定
     */
    private boolean shouldUpdateVector() {
        // 指示ベクトルとの差がしきい値以下の場合は更新をスキップ
        double headingDiff = Math.abs(aircraftVector.heading.toDouble() - instructedVector.instructedHeading.toDouble());
        double speedDiff = Math.abs(aircraftVector.groundSpeed.toDouble() - instructedVector.instructedGroundSpeed.toDouble());

        // 高度判定を修正: 現在高度ではなく、垂直速度ベースで判定
        // 現在高度が指示高度に近い場合（5フィート以内）かつ垂直速度が小さい場合は更新をスキップ
        double currentAltitude = aircraftPosition.altitude.toDouble();
        double targetAltitude = instructedVector.instructedAltitude.toDouble();
        double altitudeDiff = Math.abs(currentAltitude - targetAltitude);
        double currentVerticalSpeed = Math.abs(aircraftVector.verticalSpeed.toDouble());

        // 高度に関する更新判定の改善：
        // 1. 高度差が5フィート以内で垂直速度が50ft/min以下の場合は更新不要
        // 2. それ以外で高度差がしきい値を超える場合は更新必要
        // 3. 垂直速度が大きい場合（上昇/降下中）は更新必要
        boolean altitudeUpdateNeeded;
        if (altitudeDiff <= 5.0 && currentVerticalSpeed <= 50.0) {
            // 高度が安定している場合は更新不要
            altitudeUpdateNeeded = false;
        } else if (altitudeDiff > ALTITUDE_UPDATE_THRESHOLD) {
            // 高度差が大きい場合は更新必要
            altitudeUpdateNeeded = true;
        } else if (currentVerticalSpeed > SPEED_UPDATE_THRESHOLD * 100) { // 垂直速度は ft/min 単位
            // 大きな垂直速度で移動中の場合は更新必要
            altitudeUpdateNeeded = true;
        } else {
            // その他の場合は更新不要
            altitudeUpdateNeeded = false;
        }

        return headingDiff > HEADING_UPDATE_THRESHOLD ||
               speedDiff > SPEED_UPDATE_THRESHOLD ||
               altitudeUpdateNeeded;
    }

    /**
     * 位置の有意な変化をチェック
     */
    private boolean isSignificantPositionChange(AircraftPosition newPosition) {
        if (lastCachedPosition == null) {
            return true;
        }

        double distance = GeodeticUtils.approximateHorizontalDistance(lastCachedPosition, newPosition);
        return distance > POSITION_UPDATE_THRESHOLD;
    }

    /**
     * ベクトルの有意な変化をチェック
     */
    private boolean isSignificantVectorChange() {
        if (lastCachedVector == null) {
            return true;
        }

        double headingChange = Math.abs(aircraftVector.heading.toDouble() - lastCachedVector.heading.toDouble());
        double speedChange = Math.abs(aircraftVector.groundSpeed.toDouble() - lastCachedVector.groundSpeed.toDouble());
        double verticalSpeedChange = Math.abs(aircraftVector.verticalSpeed.toDouble() - lastCachedVector.verticalSpeed.toDouble());

        return headingChange > HEADING_UPDATE_THRESHOLD ||
               speedChange > SPEED_UPDATE_THRESHOLD ||
               verticalSpeedChange > SPEED_UPDATE_THRESHOLD;
    }

    /**
     * レーダー表示文字列キャッシュの無効化
     */
    private void invalidateRadarStringCache() {
        cachedRadarString = null;
        lastRadarStringUpdate = 0;
    }

    // Getters for commercial-specific properties
    public String getOriginIata() { return originIata; }
    public String getOriginIcao() { return originIcao; }
    public String getDestinationIata() { return destinationIata; }
    public String getDestinationIcao() { return destinationIcao; }
    public String getEta() { return eta; }

    /**
     * フロントエンドのレーダー表示用フォーマットで商用航空機情報を出力（最適化版）
     * 出発地・到着地・ETA情報を含む
     * レスポンス時間短縮のためキャッシュ機能を使用
     */
    @Override
    public String toRadarString() {
        long currentTime = System.currentTimeMillis();

        // キャッシュされた文字列が有効かチェック（100ms間隔）
        if (cachedRadarString != null && (currentTime - lastRadarStringUpdate) < 100) {
            return cachedRadarString;
        }

        // 文字列を再生成してキャッシュ
        cachedRadarString = String.format("Aircraft{callsign=%s, position={latitude=%.6f, longitude=%.6f, altitude=%.0f}, " +
                "vector={heading=%.1f, groundSpeed=%.1f, verticalSpeed=%.0f}, " +
                "instructedVector={heading=%.1f, groundSpeed=%.1f, altitude=%.0f}, " +
                "type=%s, model=%s, originIata=%s, originIcao=%s, destinationIata=%s, destinationIcao=%s, eta=%s}",
                getCallsign(),
                aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
                aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
                instructedVector.instructedHeading.toDouble(), instructedVector.instructedGroundSpeed.toDouble(), instructedVector.instructedAltitude.toDouble(),
                getAircraftCategory(), aircraftType, originIata, originIcao, destinationIata, destinationIcao, eta);

        lastRadarStringUpdate = currentTime;
        return cachedRadarString;
    }

    @Override
    public String toString() {
        return String.format("CommercialAircraft{" +
                "callsign=%s, position={lat=%.4f, lon=%.4f, alt=%.0f}, " +
                "vector={hdg=%.1f, spd=%.1f, vs=%.0f}, " +
                "route=%s→%s, eta=%s, characteristics=%s}",
                getCallsign(),
                aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
                aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
                originIata, destinationIata, eta, characteristics.getCategory().getDescription());
    }

    /**
     * パフォーマンス統計の取得（デバッグ用）
     * 最適化効果の測定に使用
     */
    public String getPerformanceStats() {
        double[] cacheStats = PerformanceUtils.getCacheStatistics();
        return String.format("Performance Stats for %s: Cache Hits=%.0f, Misses=%.0f, Hit Rate=%.1f%%",
                getCallsign(), cacheStats[0], cacheStats[1], cacheStats[2]);
    }
}

// 定数定義（CommercialAircraft固有の最適化しきい値）
// この値は実際の運用データに基づいて調整可能
final class CommercialAircraftConstants {
    /** 平面近似を使用する距離のしきい値（キロメートル） */
    static final double PLANAR_APPROXIMATION_THRESHOLD = 50.0 * NAUTICAL_MILES_TO_KM;

    /** 文字列キャッシュの有効期間（ミリ秒） */
    static final long RADAR_STRING_CACHE_DURATION = 100;

    /** 位置更新の最小間隔（ミリ秒） */
    static final long MIN_POSITION_UPDATE_INTERVAL = 1000 / REFRESH_RATE;

    private CommercialAircraftConstants() {
        throw new AssertionError("定数クラスのインスタンス化は禁止されています");
    }
}
