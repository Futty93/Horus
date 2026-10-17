package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoAroundRequestDto(
        @NotBlank String airportIcao,
        @NotBlank String runwayDesignator,
        @NotNull @Min(1000) @Max(45000) Integer climbAltitudeFt) {
}
