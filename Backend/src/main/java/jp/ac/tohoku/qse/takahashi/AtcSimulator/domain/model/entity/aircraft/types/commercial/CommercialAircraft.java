package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.behavior.FixedWingFlightBehavior;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

/**
 * 商用旅客機クラス
 * 一般的な旅客機の特性を実装
 */
public class CommercialAircraft extends AircraftBase {

    private final String originIata;
    private final String originIcao;
    private final String destinationIata;
    private final String destinationIcao;
    private final String eta; // Estimated Time of Arrival in ISO 8601 format

    /**
     * 商用旅客機のデフォルト特性
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
    }

    // Getters for commercial-specific properties
    public String getOriginIata() { return originIata; }
    public String getOriginIcao() { return originIcao; }
    public String getDestinationIata() { return destinationIata; }
    public String getDestinationIcao() { return destinationIcao; }
    public String getEta() { return eta; }

    /**
     * フロントエンドのレーダー表示用フォーマットで商用航空機情報を出力
     * 出発地・到着地・ETA情報を含む
     */
    @Override
    public String toRadarString() {
        return String.format("Aircraft{callsign=%s, position={latitude=%.6f, longitude=%.6f, altitude=%.0f}, " +
                "vector={heading=%.1f, groundSpeed=%.1f, verticalSpeed=%.0f}, " +
                "instructedVector={heading=%.1f, groundSpeed=%.1f, altitude=%.0f}, " +
                "type=%s, model=%s, originIata=%s, originIcao=%s, destinationIata=%s, destinationIcao=%s, eta=%s}",
                getCallsign(),
                aircraftPosition.latitude.toDouble(), aircraftPosition.longitude.toDouble(), aircraftPosition.altitude.toDouble(),
                aircraftVector.heading.toDouble(), aircraftVector.groundSpeed.toDouble(), aircraftVector.verticalSpeed.toDouble(),
                instructedVector.instructedHeading.toDouble(), instructedVector.instructedGroundSpeed.toDouble(), instructedVector.instructedAltitude.toDouble(),
                getAircraftCategory(), aircraftType, originIata, originIcao, destinationIata, destinationIcao, eta);
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
}
