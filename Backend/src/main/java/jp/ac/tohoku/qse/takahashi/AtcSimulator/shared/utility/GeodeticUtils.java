package jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * 航空機のコンフリクト検出と位置計算に必要な測地計算ユーティリティクラス
 * Haversine公式による球面三角法を使用した正確な距離・位置計算を提供
 * CommercialAircraftクラスの最適化に活用されます
 */
public final class GeodeticUtils {

    // 計算効率化のための定数キャッシュ
    private static final double HALF_PI = Math.PI / 2.0;
    private static final double TWO_PI = 2.0 * Math.PI;
    private static final double EARTH_RADIUS_NM = EARTH_RADIUS / NAUTICAL_MILES_TO_KM; // 海里での地球半径

    // 距離計算のしきい値（海里）- この距離未満では平面近似を使用
    private static final double PLANAR_APPROXIMATION_THRESHOLD = 50.0;

    // ユーティリティクラスのためプライベートコンストラクタ
    private GeodeticUtils() {
        throw new AssertionError("ユーティリティクラスのインスタンス化は禁止されています");
    }

    /**
     * 2つの航空機位置間の水平距離を計算（最適化版）
     * 短距離では平面近似、長距離ではHaversine公式を選択的に使用
     *
     * @param pos1 航空機1の位置
     * @param pos2 航空機2の位置
     * @return 水平距離（海里）
     */
    public static double calculateHorizontalDistance(AircraftPosition pos1, AircraftPosition pos2) {
        // 高速な事前チェック：距離が短い場合は平面近似を使用
        double approximateDistance = approximateHorizontalDistance(pos1, pos2);

        if (approximateDistance < PLANAR_APPROXIMATION_THRESHOLD) {
            return approximateDistance;
        }

        // 長距離の場合はHaversine公式を使用
        return calculateHaversineDistance(pos1, pos2);
    }

    /**
     * Haversine公式による正確な距離計算
     */
    private static double calculateHaversineDistance(AircraftPosition pos1, AircraftPosition pos2) {
        double lat1Rad = Math.toRadians(pos1.latitude.toDouble());
        double lat2Rad = Math.toRadians(pos2.latitude.toDouble());
        double deltaLatRad = Math.toRadians(pos2.latitude.toDouble() - pos1.latitude.toDouble());
        double deltaLonRad = Math.toRadians(pos2.longitude.toDouble() - pos1.longitude.toDouble());

        // Haversine公式（最適化版）
        double sinDeltaLat = Math.sin(deltaLatRad * 0.5);
        double sinDeltaLon = Math.sin(deltaLonRad * 0.5);

        double a = sinDeltaLat * sinDeltaLat +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) * sinDeltaLon * sinDeltaLon;

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 直接海里で計算
        return EARTH_RADIUS_NM * c;
    }

    /**
     * 2つの航空機位置間の垂直距離を計算
     *
     * @param pos1 航空機1の位置
     * @param pos2 航空機2の位置
     * @return 垂直距離（フィート）
     */
    public static double calculateVerticalDistance(AircraftPosition pos1, AircraftPosition pos2) {
        return Math.abs(pos1.altitude.toDouble() - pos2.altitude.toDouble());
    }

    /**
     * 指定時間後の航空機位置を予測計算（最適化版）
     * 短距離移動では平面近似、長距離移動では球面計算を使用
     *
     * @param currentPosition 現在位置
     * @param vector 現在の速度ベクトル
     * @param timeInSeconds 予測時間（秒）
     * @return 予測位置
     */
    public static AircraftPosition predictPosition(AircraftPosition currentPosition,
                                                 AircraftVector vector,
                                                 double timeInSeconds) {
        // 移動距離を計算（キロメートル）
        double groundSpeedKmPerSec = vector.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3600.0;
        double distanceTraveled = groundSpeedKmPerSec * timeInSeconds;

        // 短距離移動の場合は平面近似を使用（高速）
        if (distanceTraveled < PLANAR_APPROXIMATION_THRESHOLD * NAUTICAL_MILES_TO_KM) {
            return predictPositionPlanar(currentPosition, vector, timeInSeconds);
        }

        // 長距離移動の場合は球面計算を使用（高精度）
        return predictPositionSpherical(currentPosition, vector, timeInSeconds);
    }

    /**
     * 平面近似による位置予測（高速版）
     */
    private static AircraftPosition predictPositionPlanar(AircraftPosition currentPosition,
                                                        AircraftVector vector,
                                                        double timeInSeconds) {
        // PositionUtilsを活用
        return PositionUtils.calculateNextPosition(
            currentPosition,
            vector.groundSpeed.toDouble(),
            vector.heading.toDouble(),
            vector.verticalSpeed.toDouble(),
            timeInSeconds
        );
    }

    /**
     * 球面計算による位置予測（高精度版）
     */
    private static AircraftPosition predictPositionSpherical(AircraftPosition currentPosition,
                                                           AircraftVector vector,
                                                           double timeInSeconds) {
        // 地上速度をkm/sに変換
        double groundSpeedKmPerSec = vector.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3600.0;
        double distanceTraveled = groundSpeedKmPerSec * timeInSeconds;

        // ヘディングをラジアンに変換（北を0度とする航空機の座標系）
        double headingRad = MathUtils.toRadians(vector.heading.toDouble());

        // 現在位置の緯度・経度をラジアンに変換
        double currentLatRad = MathUtils.toRadians(currentPosition.latitude.toDouble());
        double currentLonRad = MathUtils.toRadians(currentPosition.longitude.toDouble());

        // 角距離（球面上での移動距離をラジアンで表現）
        double angularDistance = distanceTraveled / EARTH_RADIUS;

        // 三角関数の事前計算（最適化）
        double sinCurrentLat = Math.sin(currentLatRad);
        double cosCurrentLat = Math.cos(currentLatRad);
        double sinAngularDist = Math.sin(angularDistance);
        double cosAngularDist = Math.cos(angularDistance);
        double cosHeading = Math.cos(headingRad);
        double sinHeading = Math.sin(headingRad);

        // 新しい緯度を計算
        double newLatRad = Math.asin(
            sinCurrentLat * cosAngularDist + cosCurrentLat * sinAngularDist * cosHeading
        );

        // 新しい経度を計算
        double newLonRad = currentLonRad + Math.atan2(
            sinHeading * sinAngularDist * cosCurrentLat,
            cosAngularDist - sinCurrentLat * Math.sin(newLatRad)
        );

        // 新しい高度を計算（垂直速度を考慮）
        double newAltitude = currentPosition.altitude.toDouble() +
                           (vector.verticalSpeed.toDouble() * timeInSeconds / 60.0);

        // 新しい位置を返す
        return new AircraftPosition(
            new Latitude(MathUtils.toDegrees(newLatRad)),
            new Longitude(MathUtils.toDegrees(newLonRad)),
            new Altitude(newAltitude)
        );
    }

    /**
     * 2つの航空機の相対速度ベクトルの大きさを計算
     *
     * @param vector1 航空機1の速度ベクトル
     * @param vector2 航空機2の速度ベクトル
     * @return 相対速度の大きさ（ノット）
     */
    public static double calculateRelativeSpeed(AircraftVector vector1, AircraftVector vector2) {
        // ベクトル成分を一度に計算（最適化）
        double[] v1Components = PositionUtils.decomposeVelocityVector(
            vector1.groundSpeed.toDouble(), vector1.heading.toDouble());
        double[] v2Components = PositionUtils.decomposeVelocityVector(
            vector2.groundSpeed.toDouble(), vector2.heading.toDouble());

        // 相対速度ベクトル
        double relativeVx = v2Components[0] - v1Components[0];
        double relativeVy = v2Components[1] - v1Components[1];

        // 相対速度の大きさ
        return Math.sqrt(relativeVx * relativeVx + relativeVy * relativeVy);
    }

    /**
     * 高速な距離近似計算（事前フィルタリング用）
     * 短距離では平面近似を使用してパフォーマンスを向上
     *
     * @param pos1 位置1
     * @param pos2 位置2
     * @return 近似距離（海里）
     */
    public static double approximateHorizontalDistance(AircraftPosition pos1, AircraftPosition pos2) {
        // PositionUtilsの最適化された平面近似を活用
        double distanceKm = PositionUtils.calculateApproximateDistance(
            pos1.latitude.toDouble(), pos1.longitude.toDouble(),
            pos2.latitude.toDouble(), pos2.longitude.toDouble()
        );

        // キロメートルから海里に変換
        return distanceKm / NAUTICAL_MILES_TO_KM;
    }

    /**
     * 3次元距離を計算（水平距離と垂直距離の合成）
     *
     * @param pos1 航空機1の位置
     * @param pos2 航空機2の位置
     * @return 3次元距離（海里）
     */
    public static double calculate3DDistance(AircraftPosition pos1, AircraftPosition pos2) {
        double horizontalDistanceNm = calculateHorizontalDistance(pos1, pos2);
        double verticalDistanceFt = calculateVerticalDistance(pos1, pos2);

        // 垂直距離を海里に変換（1海里 ≈ 6076フィート）
        double verticalDistanceNm = verticalDistanceFt / 6076.0;

        return Math.sqrt(horizontalDistanceNm * horizontalDistanceNm + verticalDistanceNm * verticalDistanceNm);
    }

    /**
     * 角度の正規化（0-360度範囲）
     *
     * @param angle 角度（度）
     * @return 正規化された角度（0-360度）
     */
    public static double normalizeAngle(double angle) {
        return MathUtils.normalizeAngle(angle);
    }
}
