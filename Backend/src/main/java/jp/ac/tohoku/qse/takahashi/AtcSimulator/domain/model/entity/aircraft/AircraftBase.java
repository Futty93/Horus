package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.StringUtils;

import static jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalConstants.REFRESH_RATE;

/**
 * 航空機の基底クラス
 * Strategy パターンとComposition を活用して、異なる航空機タイプの実装を可能にする
 */
public abstract class AircraftBase implements Aircraft {
    protected final Callsign callsign;
    protected AircraftPosition aircraftPosition;
    protected AircraftVector aircraftVector;
    protected InstructedVector instructedVector;
    protected final AircraftType aircraftType;

    // Strategy パターンによる飛行動作の委譲
    protected final FlightBehavior flightBehavior;

    // 航空機の物理的特性
    protected final AircraftCharacteristics characteristics;

    public AircraftBase(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition,
                       AircraftVector aircraftVector, FlightBehavior flightBehavior,
                       AircraftCharacteristics characteristics) {
        this.callsign = callsign;
        this.aircraftType = aircraftType;
        this.aircraftPosition = aircraftPosition;
        this.aircraftVector = aircraftVector;
        this.flightBehavior = flightBehavior;
        this.characteristics = characteristics;
        this.instructedVector = new InstructedVector(aircraftVector.heading, aircraftPosition.altitude, aircraftVector.groundSpeed);
    }

    @Override
    public void calculateNextAircraftPosition() {
        this.aircraftPosition = flightBehavior.calculateNextPosition(
            this.aircraftPosition,
            this.aircraftVector,
            REFRESH_RATE
        );
    }

    @Override
    public void calculateNextAircraftVector() {
        // 次のヘディングを計算
        var nextHeading = flightBehavior.calculateNextHeading(
            this.aircraftVector.heading.toDouble(),
            this.instructedVector.instructedHeading.toDouble(),
            this.characteristics.getMaxTurnRate()
        );

        // 次の地上速度を計算
        var nextGroundSpeed = flightBehavior.calculateNextGroundSpeed(
            this.aircraftVector.groundSpeed.toDouble(),
            this.instructedVector.instructedGroundSpeed.toDouble(),
            this.characteristics.getMaxAcceleration()
        );

        // 次の垂直速度を計算
        var nextVerticalSpeed = flightBehavior.calculateNextVerticalSpeed(
            this.aircraftPosition.altitude.toDouble(),
            this.instructedVector.instructedAltitude.toDouble(),
            this.characteristics.getMaxClimbRate(),
            REFRESH_RATE
        );

        // 新しいAircraftVectorを設定
        this.aircraftVector = new AircraftVector(nextHeading, nextGroundSpeed, nextVerticalSpeed);
    }

    @Override
    public double calculateTurnAngle(FixPosition fixPosition) {
        return flightBehavior.calculateTurnAngle(
            this.aircraftPosition,
            this.aircraftVector.heading.toDouble(),
            fixPosition
        );
    }

    @Override
    public boolean isEqualCallsign(Callsign callsign) {
        return this.callsign.equals(callsign);
    }

    // Getters
    @Override
    public Callsign getCallsign() {
        return this.callsign;
    }

    @Override
    public AircraftPosition getAircraftPosition() {
        return this.aircraftPosition;
    }

    @Override
    public AircraftVector getAircraftVector() {
        return this.aircraftVector;
    }

    @Override
    public InstructedVector getInstructedVector() {
        return this.instructedVector;
    }

    @Override
    public AircraftType getAircraftType() {
        return this.aircraftType;
    }

    public AircraftCharacteristics getCharacteristics() {
        return this.characteristics;
    }

    public FlightBehavior getFlightBehavior() {
        return this.flightBehavior;
    }

    // Setters
    public void setAircraftPosition(final AircraftPosition newAircraftPosition) {
        this.aircraftPosition = newAircraftPosition;
    }

    public void setAircraftVector(final AircraftVector newAircraftVector) {
        this.aircraftVector = newAircraftVector;
    }

    public void setInstructedVector(final InstructedVector newInstructedVector) {
        this.instructedVector = newInstructedVector;
    }

    /**
     * 航空機の詳細情報を取得（デバッグ用）
     */
    public String getDetailedInfo() {
        return String.format("%s - %s (Category: %s, MaxSpeed: %.1f knots)",
            callsign, aircraftType, characteristics.getCategory().getDescription(), characteristics.getMaxSpeed());
    }

    /**
     * フロントエンドのレーダー表示用フォーマットで航空機情報を出力
     * 既存のフロントエンドとの互換性を保つため
     */
    public String toRadarString() {
        return StringUtils.formatAircraftBaseInfo(
            callsign.toString(),
            aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
            aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
            instructedVector.instructedHeading.toDouble(), instructedVector.instructedGroundSpeed.toDouble(), instructedVector.instructedAltitude.toDouble(),
            getAircraftCategory(), aircraftType.toString()
        ) + "}";
    }

    /**
     * 航空機カテゴリを取得（サブクラスでオーバーライド可能）
     */
    protected String getAircraftCategory() {
        return characteristics.getCategory().name();
    }
}
