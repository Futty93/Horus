package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.helicopter;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.HelicopterFlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

/**
 * ヘリコプタークラス
 * 空中停止、垂直上昇、その場回転などのヘリコプター特有の能力を実装
 */
public class Helicopter extends AircraftBase {

    private final String operator;    // 運航会社・組織
    private final String purpose;     // 用途（救急、警察、報道、輸送など）
    private final String homeBase;    // 基地
    private boolean isHovering;       // ホバリング状態

    /**
     * ヘリコプターのデフォルト特性
     * 速度は低いが機動性が高い
     */
    private static final AircraftCharacteristics DEFAULT_HELICOPTER_CHARACTERISTICS =
        new AircraftCharacteristics(
            5.0,    // maxAcceleration (kts/s) - 中程度の加速度
            6.0,    // maxTurnRate (deg/s) - 高い旋回性能
            2000.0, // maxClimbRate (ft/min) - 優秀な上昇性能
            150.0,  // maxSpeed (kts) - 中程度の最高速度
            0.0,    // minSpeed (kts) - 完全停止可能
            10000.0, // maxAltitude (ft) - 中高度まで
            0.0,    // minAltitude (ft)
            AircraftCharacteristics.AircraftCategory.HELICOPTER
        );

    /**
     * コンストラクタ
     */
    public Helicopter(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition,
                     AircraftVector aircraftVector, String operator, String purpose, String homeBase) {
        super(callsign, aircraftType, aircraftPosition, aircraftVector,
              new HelicopterFlightBehavior(), DEFAULT_HELICOPTER_CHARACTERISTICS);

        this.operator = operator;
        this.purpose = purpose;
        this.homeBase = homeBase;
        this.isHovering = false;
    }

    /**
     * カスタム特性を持つコンストラクタ
     */
    public Helicopter(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition,
                     AircraftVector aircraftVector, String operator, String purpose, String homeBase,
                     AircraftCharacteristics customCharacteristics) {
        super(callsign, aircraftType, aircraftPosition, aircraftVector,
              new HelicopterFlightBehavior(), customCharacteristics);

        this.operator = operator;
        this.purpose = purpose;
        this.homeBase = homeBase;
        this.isHovering = false;
    }

    // ヘリコプター特有のメソッド

    /**
     * ホバリングを開始
     */
    public void startHovering() {
        isHovering = true;
        // 地上速度を0に設定してホバリング状態にする
        setAircraftVector(new AircraftVector(
            aircraftVector.heading,
            new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed(0.0),
            aircraftVector.verticalSpeed
        ));
    }

    /**
     * ホバリングを終了
     */
    public void stopHovering() {
        isHovering = false;
    }

    /**
     * 垂直上昇を実行
     */
    public void performVerticalClimb(double targetAltitude) {
        // 水平移動を停止して垂直上昇のみ
        startHovering();

        // 垂直速度を最大上昇率に設定
        var verticalSpeed = new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.VerticalSpeed(
            characteristics.getMaxClimbRate()
        );

        setAircraftVector(new AircraftVector(
            aircraftVector.heading,
            aircraftVector.groundSpeed,
            verticalSpeed
        ));

        // 指示高度を更新
        setInstructedVector(new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector(
            instructedVector.instructedHeading,
            new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude(targetAltitude),
            instructedVector.instructedGroundSpeed
        ));
    }

    /**
     * 垂直降下を実行
     */
    public void performVerticalDescend(double targetAltitude) {
        // 水平移動を停止して垂直降下のみ
        startHovering();

        // 垂直速度を最大降下率に設定
        var verticalSpeed = new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.VerticalSpeed(
            -characteristics.getMaxClimbRate()
        );

        setAircraftVector(new AircraftVector(
            aircraftVector.heading,
            aircraftVector.groundSpeed,
            verticalSpeed
        ));

        // 指示高度を更新
        setInstructedVector(new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector(
            instructedVector.instructedHeading,
            new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude(targetAltitude),
            instructedVector.instructedGroundSpeed
        ));
    }

    /**
     * その場回転を実行
     */
    public void performSpotTurn(double targetHeading) {
        // ホバリング状態でヘディングのみ変更
        startHovering();

        setInstructedVector(new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector(
            new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading(targetHeading),
            instructedVector.instructedAltitude,
            new jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed(0.0)
        ));
    }

    /**
     * 現在ホバリング中かどうかを判定
     */
    public boolean isCurrentlyHovering() {
        return isHovering && ((HelicopterFlightBehavior) flightBehavior).isHovering(aircraftVector);
    }

    // Getters
    public String getOperator() { return operator; }
    public String getPurpose() { return purpose; }
    public String getHomeBase() { return homeBase; }
    public boolean isHovering() { return isHovering; }

    /**
     * フロントエンドのレーダー表示用フォーマットでヘリコプター情報を出力
     * 運航者・用途・基地情報を含む
     */
    @Override
    public String toRadarString() {
        return String.format("Aircraft{callsign=%s, position={latitude=%.6f, longitude=%.6f, altitude=%.0f}, " +
                "vector={heading=%.1f, groundSpeed=%.1f, verticalSpeed=%.0f}, " +
                "instructedVector={heading=%.1f, groundSpeed=%.1f, altitude=%.0f}, " +
                "type=%s, model=%s, operator=%s, purpose=%s, homeBase=%s, hovering=%s}",
                getCallsign(),
                aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
                aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
                instructedVector.instructedHeading.toDouble(), instructedVector.instructedGroundSpeed.toDouble(), instructedVector.instructedAltitude.toDouble(),
                getAircraftCategory(), aircraftType, operator, purpose, homeBase, isCurrentlyHovering() ? "YES" : "NO");
    }

    @Override
    public String toString() {
        return String.format("Helicopter{" +
                "callsign=%s, position={lat=%.4f, lon=%.4f, alt=%.0f}, " +
                "vector={hdg=%.1f, spd=%.1f, vs=%.0f}, " +
                "operator=%s, purpose=%s, base=%s, hovering=%s}",
                getCallsign(),
                aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
                aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
                operator, purpose, homeBase, isCurrentlyHovering() ? "YES" : "NO");
    }
}
