package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.factory;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.characteristics.AircraftCharacteristics;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.helicopter.Helicopter;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.military.FighterJet;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

/**
 * 航空機作成ファクトリークラス
 * 異なる航空機タイプの作成を統一的にサポート
 */
public class AircraftFactory {

    /**
     * 商用旅客機を作成
     */
    public static CommercialAircraft createCommercialAircraft(
            String callsign, String aircraftTypeStr, AircraftPosition position, AircraftVector vector,
            String originIata, String originIcao, String destinationIata, String destinationIcao, String eta) {

        return new CommercialAircraft(
            new Callsign(callsign),
            new AircraftType(aircraftTypeStr),
            position,
            vector,
            originIata,
            originIcao,
            destinationIata,
            destinationIcao,
            eta
        );
    }

    /**
     * カスタム特性を持つ商用旅客機を作成
     */
    public static CommercialAircraft createCommercialAircraft(
            String callsign, String aircraftTypeStr, AircraftPosition position, AircraftVector vector,
            String originIata, String originIcao, String destinationIata, String destinationIcao, String eta,
            AircraftCharacteristics customCharacteristics) {

        return new CommercialAircraft(
            new Callsign(callsign),
            new AircraftType(aircraftTypeStr),
            position,
            vector,
            originIata,
            originIcao,
            destinationIata,
            destinationIcao,
            eta,
            customCharacteristics
        );
    }

    /**
     * 戦闘機を作成
     */
    public static FighterJet createFighterJet(
            String callsign, String aircraftTypeStr, AircraftPosition position, AircraftVector vector,
            String squadron, String mission, String homeBase) {

        return new FighterJet(
            new Callsign(callsign),
            new AircraftType(aircraftTypeStr),
            position,
            vector,
            squadron,
            mission,
            homeBase
        );
    }

    /**
     * カスタム特性を持つ戦闘機を作成
     */
    public static FighterJet createFighterJet(
            String callsign, String aircraftTypeStr, AircraftPosition position, AircraftVector vector,
            String squadron, String mission, String homeBase,
            AircraftCharacteristics customCharacteristics) {

        return new FighterJet(
            new Callsign(callsign),
            new AircraftType(aircraftTypeStr),
            position,
            vector,
            squadron,
            mission,
            homeBase,
            customCharacteristics
        );
    }

    /**
     * ヘリコプターを作成
     */
    public static Helicopter createHelicopter(
            String callsign, String aircraftTypeStr, AircraftPosition position, AircraftVector vector,
            String operator, String purpose, String homeBase) {

        return new Helicopter(
            new Callsign(callsign),
            new AircraftType(aircraftTypeStr),
            position,
            vector,
            operator,
            purpose,
            homeBase
        );
    }

    /**
     * カスタム特性を持つヘリコプターを作成
     */
    public static Helicopter createHelicopter(
            String callsign, String aircraftTypeStr, AircraftPosition position, AircraftVector vector,
            String operator, String purpose, String homeBase,
            AircraftCharacteristics customCharacteristics) {

        return new Helicopter(
            new Callsign(callsign),
            new AircraftType(aircraftTypeStr),
            position,
            vector,
            operator,
            purpose,
            homeBase,
            customCharacteristics
        );
    }

    /**
     * 航空機カテゴリに基づいて航空機を作成（デフォルト値使用）
     */
    public static Aircraft createAircraftByCategory(
            AircraftCharacteristics.AircraftCategory category,
            String callsign, String aircraftTypeStr, AircraftPosition position, AircraftVector vector) {

        switch (category) {
            case COMMERCIAL_PASSENGER:
            case COMMERCIAL_CARGO:
                return createCommercialAircraft(callsign, aircraftTypeStr, position, vector,
                    "NRT", "RJAA", "HND", "RJTT", "2024-01-01T12:00:00Z");

            case MILITARY_FIGHTER:
                return createFighterJet(callsign, aircraftTypeStr, position, vector,
                    "302SQ", "CAP", "Misawa AB");

            case HELICOPTER:
                return createHelicopter(callsign, aircraftTypeStr, position, vector,
                    "Japan Coast Guard", "SAR", "Tokyo Heliport");

            default:
                throw new IllegalArgumentException("Unsupported aircraft category: " + category);
        }
    }
}
