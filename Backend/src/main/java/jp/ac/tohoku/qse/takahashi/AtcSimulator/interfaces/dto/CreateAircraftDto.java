package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Company;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.FlightNumber;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public class CreateAircraftDto {
    @NotNull
    public final String companyName;

    @NotNull
    public final String flightNumber;

    @NotNull
    public final double latitude;

    @NotNull
    public final double longitude;

    @NotNull
    public final int altitude;

    @NotNull
    public final int groundSpeed;

    @NotNull
    public final int verticalSpeed;

    @NotNull
    public final int heading;

    @NotNull
    public final String type;

    @NotNull
    public final String originIata;

    @NotNull
    public final String originIcao;

    @NotNull
    public final String destinationIata;

    @NotNull
    public final String destinationIcao;

    @NotNull
    public final String eta;

    public CreateAircraftDto(String companyName, String flightNumber, double latitude, double longitude, int altitude, int groundSpeed, int verticalSpeed, int heading, String type, String originIata, String originIcao, String destinationIata, String destinationIcao, String eta) {
        this.companyName = companyName;
        this.flightNumber = flightNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.groundSpeed = groundSpeed;
        this.verticalSpeed = verticalSpeed;
        this.heading = heading;
        this.type = type;
        this.originIata = originIata;
        this.originIcao = originIcao;
        this.destinationIata = destinationIata;
        this.destinationIcao = destinationIcao;
        this.eta = eta;
    }

    public CommercialAircraft createCommercialAircraft() {
        Callsign callsign = new Callsign(new Company(this.companyName), new FlightNumber(this.flightNumber));
        AircraftPosition aircraftPosition = new AircraftPosition(this.latitude, this.longitude, this.altitude);
        AircraftVector aircraftVector = new AircraftVector(this.heading, this.groundSpeed, this.verticalSpeed);
        AircraftType aircraftType = new AircraftType(this.type);
        return new CommercialAircraft(callsign, aircraftType, aircraftPosition, aircraftVector, this.originIata, this.originIcao, this.destinationIata, this.destinationIcao, this.eta);
    }
}
