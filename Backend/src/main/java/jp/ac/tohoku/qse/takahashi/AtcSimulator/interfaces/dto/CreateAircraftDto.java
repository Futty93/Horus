package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.NotNull;

public class CreateAircraftDto {
    @NotNull
    public final String callsign;

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

    public CreateAircraftDto(String callsign, double latitude, double longitude, int altitude, int groundSpeed, int verticalSpeed, int heading, String type, String originIata, String originIcao, String destinationIata, String destinationIcao, String eta) {
        this.callsign = callsign;
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
}
