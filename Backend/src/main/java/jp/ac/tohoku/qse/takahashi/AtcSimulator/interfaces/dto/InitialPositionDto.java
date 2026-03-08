package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.NotNull;

public record InitialPositionDto(
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull Integer altitude,
        @NotNull Integer heading,
        @NotNull Integer groundSpeed,
        @NotNull Integer verticalSpeed
) {
}
