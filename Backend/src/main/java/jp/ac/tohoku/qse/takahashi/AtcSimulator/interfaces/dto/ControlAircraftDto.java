package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ControlAircraftDto(
        @Min(0) @Max(360) int instructedHeading,
        @Min(0) int instructedAltitude,
        @Min(0) int instructedGroundSpeed) {
}
