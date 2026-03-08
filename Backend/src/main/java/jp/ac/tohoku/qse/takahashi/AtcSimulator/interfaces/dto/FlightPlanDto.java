package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FlightPlanDto(
        @NotBlank String callsign,
        String aircraftType,
        String departureAirport,
        String arrivalAirport,
        @NotNull Integer cruiseAltitude,
        @NotNull Integer cruiseSpeed,
        List<FlightPlanWaypointDto> route
) {
}
