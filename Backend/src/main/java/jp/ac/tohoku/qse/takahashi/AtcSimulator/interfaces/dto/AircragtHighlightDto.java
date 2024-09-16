package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.NotNull;

public class AircragtHighlightDto {
    @NotNull
    public String callsign;

    @NotNull
    public int rank;

    public AircragtHighlightDto(String callsign, int rank) {
        this.callsign = callsign;
        this.rank = rank;
    }
}
