package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.conflict;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * 航空機のコンフリクト検出に必要な測地計算ユーティリティクラス
 * Haversine公式による球面三角法を使用した正確な距離・位置計算を提供
 */
public final class GeodeticUtils {

    // ユーティリティクラスのためプライベートコンストラクタ
    private GeodeticUtils() {
        throw new AssertionError("ユーティリティクラスのインスタンス化は禁止されています");
    }

    /**
     * 2つの航空機位置間の水平距離を計算（Haversine公式使用）
     *
     * @param pos1 航空機1の位置
     * @param pos2 航空機2の位置
     * @return 水平距離（海里）
     */
    public static double calculateHorizontalDistance(AircraftPosition pos1, AircraftPosition pos2) {
        double lat1Rad = Math.toRadians(pos1.latitude.toDouble());
        double lat2Rad = Math.toRadians(pos2.latitude.toDouble());
        double deltaLatRad = Math.toRadians(pos2.latitude.toDouble() - pos1.latitude.toDouble());
        double deltaLonRad = Math.toRadians(pos2.longitude.toDouble() - pos1.longitude.toDouble());

        // Haversine公式
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = EARTH_RADIUS * c;

        // キロメートルから海里に変換
        return distanceKm / NAUTICAL_MILES_TO_KM;
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
     * 指定時間後の航空機位置を予測計算
     *
     * @param currentPosition 現在位置
     * @param vector 現在の速度ベクトル
     * @param timeInSeconds 予測時間（秒）
     * @return 予測位置
     */
    public static AircraftPosition predictPosition(AircraftPosition currentPosition,
                                                 AircraftVector vector,
                                                 double timeInSeconds) {
        // 地上速度をkm/hからkm/sに変換
        double groundSpeedKmPerSec = vector.groundSpeed.toDouble() * KNOTS_TO_KM_PER_HOUR / 3600.0;

        // 移動距離（キロメートル）
        double distanceTraveled = groundSpeedKmPerSec * timeInSeconds;

        // ヘディングをラジアンに変換（北を0度とする航空機の座標系）
        double headingRad = Math.toRadians(vector.heading.toDouble());

        // 現在位置の緯度・経度をラジアンに変換
        double currentLatRad = Math.toRadians(currentPosition.latitude.toDouble());
        double currentLonRad = Math.toRadians(currentPosition.longitude.toDouble());

        // 角距離（球面上での移動距離をラジアンで表現）
        double angularDistance = distanceTraveled / EARTH_RADIUS;

        // 新しい緯度を計算
        double newLatRad = Math.asin(
            Math.sin(currentLatRad) * Math.cos(angularDistance) +
            Math.cos(currentLatRad) * Math.sin(angularDistance) * Math.cos(headingRad)
        );

        // 新しい経度を計算
        double newLonRad = currentLonRad + Math.atan2(
            Math.sin(headingRad) * Math.sin(angularDistance) * Math.cos(currentLatRad),
            Math.cos(angularDistance) - Math.sin(currentLatRad) * Math.sin(newLatRad)
        );

        // 新しい高度を計算（垂直速度を考慮）
        double newAltitude = currentPosition.altitude.toDouble() +
                           (vector.verticalSpeed.toDouble() * timeInSeconds / 60.0);

        // 新しい位置を返す（値オブジェクトを使用）
        return new AircraftPosition(
            new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude(Math.toDegrees(newLatRad)),
            new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude(Math.toDegrees(newLonRad)),
            new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude(newAltitude)
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
        // ベクトル成分を計算（航空機座標系: 北=0度, 東=90度）
        double v1x = vector1.groundSpeed.toDouble() * Math.sin(Math.toRadians(vector1.heading.toDouble()));
        double v1y = vector1.groundSpeed.toDouble() * Math.cos(Math.toRadians(vector1.heading.toDouble()));

        double v2x = vector2.groundSpeed.toDouble() * Math.sin(Math.toRadians(vector2.heading.toDouble()));
        double v2y = vector2.groundSpeed.toDouble() * Math.cos(Math.toRadians(vector2.heading.toDouble()));

        // 相対速度ベクトル
        double relativeVx = v2x - v1x;
        double relativeVy = v2y - v1y;

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
        double deltaLat = pos2.latitude.toDouble() - pos1.latitude.toDouble();
        double deltaLon = pos2.longitude.toDouble() - pos1.longitude.toDouble();

        // 平均緯度での経度補正
        double avgLatRad = Math.toRadians((pos1.latitude.toDouble() + pos2.latitude.toDouble()) / 2);
        double correctedDeltaLon = deltaLon * Math.cos(avgLatRad);

        // 平面距離計算
        double distanceDeg = Math.sqrt(deltaLat * deltaLat + correctedDeltaLon * correctedDeltaLon);

        // 度からキロメートル、そして海里に変換
        double distanceKm = distanceDeg * EARTH_RADIUS * DEGREES_TO_RADIANS;
        return distanceKm / NAUTICAL_MILES_TO_KM;
    }

    /**
     * 角度の正規化（0-360度範囲）
     *
     * @param angle 角度（度）
     * @return 正規化された角度（0-360度）
     */
    public static double normalizeAngle(double angle) {
        return ((angle % 360) + 360) % 360;
    }
}
