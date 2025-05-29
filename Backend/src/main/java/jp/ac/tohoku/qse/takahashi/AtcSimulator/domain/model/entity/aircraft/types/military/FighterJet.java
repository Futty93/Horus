package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.military;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FixedWingFlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

/**
 * 戦闘機クラス
 * 高速飛行、高い機動性、9G旋回などの軍用戦闘機の特性を実装
 */
public class FighterJet extends AircraftBase {

    private final String squadron;    // 所属部隊
    private final String mission;     // 任務内容
    private final String homeBase;    // 基地

    /**
     * 戦闘機のデフォルト特性
     * 旅客機よりも高い性能を持つ
     */
    private static final AircraftCharacteristics DEFAULT_FIGHTER_CHARACTERISTICS =
        new AircraftCharacteristics(
            15.0,   // maxAcceleration (kts/s) - 戦闘機は高い加速度
            9.0,    // maxTurnRate (deg/s) - 9Gターンに対応
            15000.0, // maxClimbRate (ft/min) - 高い上昇率
            1500.0, // maxSpeed (kts) - マッハ2クラス
            200.0,  // minSpeed (kts) - 高い失速速度
            60000.0, // maxAltitude (ft) - 高高度飛行可能
            0.0,    // minAltitude (ft)
            AircraftCharacteristics.AircraftCategory.MILITARY_FIGHTER
        );

    /**
     * コンストラクタ
     */
    public FighterJet(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition,
                     AircraftVector aircraftVector, String squadron, String mission, String homeBase) {
        super(callsign, aircraftType, aircraftPosition, aircraftVector,
              new FixedWingFlightBehavior(), DEFAULT_FIGHTER_CHARACTERISTICS);

        this.squadron = squadron;
        this.mission = mission;
        this.homeBase = homeBase;
    }

    /**
     * カスタム特性を持つコンストラクタ
     */
    public FighterJet(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition,
                     AircraftVector aircraftVector, String squadron, String mission, String homeBase,
                     AircraftCharacteristics customCharacteristics) {
        super(callsign, aircraftType, aircraftPosition, aircraftVector,
              new FixedWingFlightBehavior(), customCharacteristics);

        this.squadron = squadron;
        this.mission = mission;
        this.homeBase = homeBase;
    }

    // 戦闘機特有のメソッド

    /**
     * 高G旋回が可能かどうかを判定
     */
    public boolean canPerformHighGTurn() {
        return characteristics.getMaxTurnRate() >= 6.0;
    }

    /**
     * 超音速飛行が可能かどうかを判定
     */
    public boolean canGoSupersonic() {
        return characteristics.getMaxSpeed() > 661.0; // マッハ1
    }

    /**
     * 戦闘機の戦術機動を実行
     * 緊急時の急激な方向転換
     */
    public void performTacticalManeuver(double emergencyHeading) {
        // 戦闘機は緊急時により急激な旋回が可能
        double tacticalTurnRate = characteristics.getMaxTurnRate() * 1.5;

        var emergencyHeadingObj = flightBehavior.calculateNextHeading(
            aircraftVector.heading.toDouble(),
            emergencyHeading,
            tacticalTurnRate
        );

        // 緊急機動では速度も調整
        var emergencySpeed = flightBehavior.calculateNextGroundSpeed(
            aircraftVector.groundSpeed.toDouble(),
            characteristics.getMaxSpeed() * 0.8, // 最高速度の80%
            characteristics.getMaxAcceleration() * 2.0 // 緊急加速
        );

        setAircraftVector(new AircraftVector(emergencyHeadingObj, emergencySpeed, aircraftVector.verticalSpeed));
    }

    // Getters
    public String getSquadron() { return squadron; }
    public String getMission() { return mission; }
    public String getHomeBase() { return homeBase; }

    /**
     * フロントエンドのレーダー表示用フォーマットで戦闘機情報を出力
     * 部隊・任務・基地情報を含む
     */
    @Override
    public String toRadarString() {
        return String.format("Aircraft{callsign=%s, position={latitude=%.6f, longitude=%.6f, altitude=%.0f}, " +
                "vector={heading=%.1f, groundSpeed=%.1f, verticalSpeed=%.0f}, " +
                "instructedVector={heading=%.1f, groundSpeed=%.1f, altitude=%.0f}, " +
                "type=%s, model=%s, squadron=%s, mission=%s, homeBase=%s}",
                getCallsign(),
                aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
                aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
                instructedVector.instructedHeading.toDouble(), instructedVector.instructedGroundSpeed.toDouble(), instructedVector.instructedAltitude.toDouble(),
                getAircraftCategory(), aircraftType, squadron, mission, homeBase);
    }

    @Override
    public String toString() {
        return String.format("FighterJet{" +
                "callsign=%s, position={lat=%.4f, lon=%.4f, alt=%.0f}, " +
                "vector={hdg=%.1f, spd=%.1f, vs=%.0f}, " +
                "squadron=%s, mission=%s, base=%s, " +
                "maxSpeed=%.0f kts, maxTurnRate=%.1f deg/s}",
                getCallsign(),
                aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
                aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
                squadron, mission, homeBase,
                characteristics.getMaxSpeed(), characteristics.getMaxTurnRate());
    }
}
