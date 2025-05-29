package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

/**
 * 航空機の飛行動作を定義するStrategy インターフェース
 * 異なる航空機タイプに応じた飛行特性を実装するために使用
 */
public interface FlightBehavior {

    /**
     * 次の位置を計算する
     *
     * @param currentPosition 現在位置
     * @param vector 現在の速度ベクトル
     * @param refreshRate リフレッシュレート
     * @return 計算された次の位置
     */
    AircraftPosition calculateNextPosition(AircraftPosition currentPosition, AircraftVector vector, double refreshRate);

    /**
     * 次のヘディングを計算する
     *
     * @param currentHeading 現在のヘディング
     * @param targetHeading 目標ヘディング
     * @param maxTurnRate 最大旋回速度
     * @return 計算された次のヘディング
     */
    Heading calculateNextHeading(double currentHeading, double targetHeading, double maxTurnRate);

    /**
     * 次の地上速度を計算する
     *
     * @param currentGroundSpeed 現在の地上速度
     * @param targetGroundSpeed 目標地上速度
     * @param maxAcceleration 最大加速度
     * @return 計算された次の地上速度
     */
    GroundSpeed calculateNextGroundSpeed(double currentGroundSpeed, double targetGroundSpeed, double maxAcceleration);

    /**
     * 次の垂直速度を計算する
     *
     * @param currentAltitude 現在高度
     * @param targetAltitude 目標高度
     * @param maxClimbRate 最大上昇/降下速度
     * @param refreshRate リフレッシュレート
     * @return 計算された次の垂直速度
     */
    VerticalSpeed calculateNextVerticalSpeed(double currentAltitude, double targetAltitude, double maxClimbRate, double refreshRate);

    /**
     * 指定された位置への旋回角度を計算する
     *
     * @param currentPosition 現在位置
     * @param currentHeading 現在ヘディング
     * @param targetFix 目標位置
     * @return 計算された旋回角度
     */
    double calculateTurnAngle(AircraftPosition currentPosition, double currentHeading, FixPosition targetFix);
}
