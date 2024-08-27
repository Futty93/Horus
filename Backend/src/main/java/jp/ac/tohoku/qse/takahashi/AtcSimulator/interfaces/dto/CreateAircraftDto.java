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
    private final String companyName;

    @NotNull
    private final String flightNumber;

    @NotNull
    private final double latitude;

    @NotNull
    private final double longitude;

    @NotNull
    private final int altitude;

    @NotNull
    private final int groundSpeed;

    @NotNull
    private final int verticalSpeed;

    @NotNull
    private final int heading;

    @NotNull
    private final String type;

    @NotNull
    private final String originIata;

    @NotNull
    private final String originIcao;

    @NotNull
    private final String destinationIata;

    @NotNull
    private final String destinationIcao;

    @NotNull
    private final String eta;

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

    public String getCompanyName() {
        return this.companyName;
    }

    public String getFlightNumber() {
        return this.flightNumber;
    }

    public CommercialAircraft createCommercialAircraft() {
        Callsign callsign = new Callsign(new Company(this.companyName), new FlightNumber(this.flightNumber));
        AircraftPosition aircraftPosition = new AircraftPosition(this.latitude, this.longitude, this.altitude);
        AircraftVector aircraftVector = new AircraftVector(this.heading, this.groundSpeed, this.verticalSpeed);
        AircraftType aircraftType = new AircraftType(this.type);
        return new CommercialAircraft(callsign, aircraftPosition, aircraftVector, aircraftType, this.originIata, this.originIcao, this.destinationIata, this.destinationIcao, this.eta);
    }
}
