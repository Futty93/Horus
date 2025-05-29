package jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.*;

/**
 * 位置計算の共通ユーティリティクラス
 * 航空機の位置計算で重複する処理を集約
 */
public final class PositionUtils {

    // ユーティリティクラスのためプライベートコンストラクタ
    private PositionUtils() {
        throw new AssertionError("ユーティリティクラスのインスタンス化は禁止されています");
    }

    /**
     * 航空機の次の位置を計算（簡素版）
     * 固定翼機とヘリコプターで共通の基本計算ロジック
     *
     * @param currentPos 現在位置
     * @param groundSpeed 地上速度（ノット）
     * @param heading ヘディング（度）
     * @param verticalSpeed 垂直速度（ft/min）
     * @param refreshRateInSeconds リフレッシュレート（秒）
     * @return 次の位置
     */
    public static AircraftPosition calculateNextPosition(AircraftPosition currentPos,
                                                       double groundSpeed,
                                                       double heading,
                                                       double verticalSpeed,
                                                       double refreshRateInSeconds) {
        // 地上速度をkm/hに変換
        double groundSpeedKmPerHour = groundSpeed * KNOTS_TO_KM_PER_HOUR;

        // 移動距離を計算（km）
        double distanceTraveled = groundSpeedKmPerHour * (refreshRateInSeconds / 3600.0);

        // ヘディングをラジアンに変換
        double headingRad = MathUtils.toRadians(heading);

        // 新しい緯度を計算
        double deltaLat = distanceTraveled / EARTH_RADIUS; // ラジアン
        Latitude newLat = new Latitude(currentPos.latitude.toDouble() +
            MathUtils.toDegrees(deltaLat * Math.cos(headingRad)));

        // 新しい経度を計算（地球の曲率を考慮）
        double deltaLon = -distanceTraveled / (EARTH_RADIUS * Math.cos(MathUtils.toRadians(currentPos.longitude.toDouble())));
        Longitude newLon = new Longitude(currentPos.longitude.toDouble() +
            MathUtils.toDegrees(deltaLon * Math.sin(headingRad)));

        // 新しい高度を計算
        Altitude newAlt = new Altitude(currentPos.altitude.toDouble() +
            (verticalSpeed * refreshRateInSeconds / 60.0));

        return new AircraftPosition(newLat, newLon, newAlt);
    }

    /**
     * ヘディング差を正規化して旋回方向を決定
     *
     * @param currentHeading 現在のヘディング
     * @param targetHeading 目標ヘディング
     * @return 正規化されたヘディング差（-180から180度）
     */
    public static double calculateHeadingDifference(double currentHeading, double targetHeading) {
        return MathUtils.normalizeAngleTo180(targetHeading - currentHeading);
    }

    /**
     * 次のヘディングを計算
     *
     * @param currentHeading 現在のヘディング
     * @param targetHeading 目標ヘディング
     * @param maxTurnRate 最大旋回速度（度/秒）
     * @return 新しいヘディング
     */
    public static Heading calculateNextHeading(double currentHeading, double targetHeading, double maxTurnRate) {
        double headingDifference = calculateHeadingDifference(currentHeading, targetHeading);

        // 旋回方向と量を決定
        double turnAmount = Math.signum(headingDifference) * Math.min(maxTurnRate, Math.abs(headingDifference));
        double nextHeading = MathUtils.normalizeAngle(currentHeading + turnAmount);

        return new Heading(nextHeading);
    }

    /**
     * 次の地上速度を計算
     *
     * @param currentGroundSpeed 現在の地上速度
     * @param targetGroundSpeed 目標地上速度
     * @param maxAcceleration 最大加速度（ノット/秒）
     * @return 新しい地上速度
     */
    public static GroundSpeed calculateNextGroundSpeed(double currentGroundSpeed,
                                                     double targetGroundSpeed,
                                                     double maxAcceleration) {
        double speedDifference = targetGroundSpeed - currentGroundSpeed;
        double accelerationAmount = Math.signum(speedDifference) *
                                  Math.min(maxAcceleration, Math.abs(speedDifference));

        return new GroundSpeed(currentGroundSpeed + accelerationAmount);
    }

    /**
     * 次の垂直速度を計算
     *
     * @param currentAltitude 現在の高度
     * @param targetAltitude 目標高度
     * @param maxClimbRate 最大上昇率（ft/min）
     * @param refreshRate リフレッシュレート
     * @return 新しい垂直速度
     */
    public static VerticalSpeed calculateNextVerticalSpeed(double currentAltitude,
                                                         double targetAltitude,
                                                         double maxClimbRate,
                                                         double refreshRate) {
        double altitudeDifference = targetAltitude - currentAltitude;

        if (MathUtils.approximately(altitudeDifference, 0.0)) {
            return new VerticalSpeed(0.0);
        }

        double maxRatePerSecond = maxClimbRate / (60.0 * refreshRate);
        double climbRate = Math.signum(altitudeDifference) *
                          Math.min(maxRatePerSecond, Math.abs(altitudeDifference));

        return new VerticalSpeed(climbRate * 60); // ft/minに変換
    }

    /**
     * 目標地点への方位を計算
     *
     * @param currentPos 現在位置
     * @param targetLat 目標緯度
     * @param targetLon 目標経度
     * @return 目標方位（度）
     */
    public static double calculateBearingToTarget(AircraftPosition currentPos,
                                                double targetLat,
                                                double targetLon) {
        // 相対位置差を計算
        double deltaX = targetLon - currentPos.longitude.toDouble();
        double deltaY = targetLat - currentPos.latitude.toDouble();

        // 方位を計算（北基準、度単位）
        double bearing = MathUtils.toDegrees(Math.atan2(deltaX, deltaY));
        return MathUtils.normalizeAngle(bearing);
    }

    /**
     * 2点間の距離を計算（平面近似）
     *
     * @param lat1 地点1の緯度
     * @param lon1 地点1の経度
     * @param lat2 地点2の緯度
     * @param lon2 地点2の経度
     * @return 距離（キロメートル）
     */
    public static double calculateApproximateDistance(double lat1, double lon1, double lat2, double lon2) {
        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        // 平均緯度での経度補正
        double avgLatRad = MathUtils.toRadians((lat1 + lat2) / 2);
        double correctedDeltaLon = deltaLon * Math.cos(avgLatRad);

        // 平面距離計算
        double distanceDeg = Math.sqrt(deltaLat * deltaLat + correctedDeltaLon * correctedDeltaLon);

        // 度からキロメートルに変換
        return distanceDeg * EARTH_RADIUS * DEGREES_TO_RADIANS;
    }

    /**
     * 速度ベクトルをx,y成分に分解
     *
     * @param groundSpeed 地上速度（ノット）
     * @param heading ヘディング（度）
     * @return [x成分, y成分]（ノット単位）
     */
    public static double[] decomposeVelocityVector(double groundSpeed, double heading) {
        double headingRad = MathUtils.toRadians(heading);
        double vx = groundSpeed * Math.sin(headingRad); // 東方向成分
        double vy = groundSpeed * Math.cos(headingRad); // 北方向成分
        return new double[]{vx, vy};
    }
}
