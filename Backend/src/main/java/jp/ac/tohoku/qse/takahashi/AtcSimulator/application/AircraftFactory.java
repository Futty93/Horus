package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

public final class AircraftFactory {

    private AircraftFactory() {
    }

    public static CommercialAircraft createCommercialAircraft(CreateAircraftDto dto) {
        Callsign callsign = new Callsign(dto.callsign);
        AircraftPosition aircraftPosition = new AircraftPosition(
                new Latitude(dto.latitude),
                new Longitude(dto.longitude),
                new Altitude(dto.altitude)
        );
        AircraftVector aircraftVector = new AircraftVector(
                new Heading(dto.heading),
                new GroundSpeed(dto.groundSpeed),
                new VerticalSpeed(dto.verticalSpeed)
        );
        AircraftType aircraftType = new AircraftType(dto.type);
        return new CommercialAircraft(
                callsign, aircraftType, aircraftPosition, aircraftVector,
                dto.originIata, dto.originIcao, dto.destinationIata, dto.destinationIcao, dto.eta
        );
    }
}
